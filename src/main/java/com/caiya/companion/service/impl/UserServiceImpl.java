package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.constant.UserConstant;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.mapper.UserMapper;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.OpenOption;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-05-04 20:42:09
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值
     */
    public static final String SALT = "caiya";

    @Override
    public long userRegister(String userAccount, String password, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, password, checkPassword))
            throw new BusinessException(ErrorCode.NULL_ERROR);
        // 校验长度
        if (userAccount.length() < 4) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        if (password.length() < 8 || checkPassword.length() < 8) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        // 账号字符特殊校验
        String validPattern = "[\\n`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*()——+|{}【】‘；：”“’。， 、？]";
        if (Pattern.compile(validPattern).matcher(userAccount).find())
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        // 判断用户名重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        long count = this.count(userQueryWrapper);
        if (count > 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        // 插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) throw new BusinessException(ErrorCode.SYSTEM_ERROR);

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String password, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, password)) throw new BusinessException(ErrorCode.NULL_ERROR);
        // 校验长度
        if (userAccount.length() < 4) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        if (password.length() < 8) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        // 账号字符特殊校验
        String validPattern = "[\\n`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*()——+|{}【】‘；：”“’。， 、？]";
        if (Pattern.compile(validPattern).matcher(userAccount).find())
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        // 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        // 查询用户
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("password", encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);
        if (user == null) throw new BusinessException(ErrorCode.NULL_ERROR);

        // 脱敏
        User safetyUser = getSafetyUser(user);

        // 存储登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户数据脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) return null;
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签列表查询用户（内存过滤）
     *
     * @param tagNameList 标签列表
     * @return 匹配的用户数组
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        // 判断列表是否为空
        if (CollectionUtils.isEmpty(tagNameList)) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        // 方式二：内存查询
        // 1. 查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 判断用户是否有该标签
        userList = userList.stream().filter(user -> { // 遍历所有用户
            String tagsStr = user.getTags(); // 获取该用户的标签列表
            // 将用户标签列表转为java对象
            // gson（参数1：要转为java对象的数据，转为具体java对象的类型，因为java不支持直接获取类型，所以改为TypeToken获取具体的java对象类型）
            Set<String> tmpTagNameListSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            // 因为有的用户的标签列表可能为空，这会导致在用用户的标签列表去判断前端传入的标签列表中的标签时会报错空指针，如果需要给用户的标签列表进行一个非空设置
            /**
             * 这段代码会先创建一个Optional对象，如果tmpTagNameListSet不为空，则包含tmpTagNameListSet，反之则包含null
             * 如果Optional包含的是null，则会走orElse为Optional设置一个默认值，然后返回新的Set<String>实例
             */
            tmpTagNameListSet = Optional.ofNullable(tmpTagNameListSet).orElse(new HashSet<>());
            // 遍历要查询的标签列表，然后判断该用户的标签列表是否包含该标签，如果不包含，直接返回false，也就不匹配该用户
            for (String tagName : tagNameList) {
                if (!tmpTagNameListSet.contains(tagName)) return false;
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
        return userList;
    }

    /**
     * 根据标签列表查询用户（SQL版）
     * Deprecated：过期的，保证不会被调用
     *
     * @param tagNameList 标签列表
     * @return
     */
    @Deprecated
    private List<User> searchUserByTagsSql(List<String> tagNameList) {
        // 方式一：SQL查询
        // 拼接查询条件，模糊查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




