package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.common.ResultUtils;
import com.caiya.companion.constant.UserConstant;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.mapper.UserMapper;
import com.caiya.companion.model.domain.Tag;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.domain.UserTag;
import com.caiya.companion.model.vo.TagVO;
import com.caiya.companion.model.vo.UserTagVO;
import com.caiya.companion.model.vo.UserVO;
import com.caiya.companion.service.TagService;
import com.caiya.companion.service.UserService;
import com.caiya.companion.service.UserTagService;
import com.caiya.companion.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.OpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.caiya.companion.constant.UserConstant.ADMIN_ROLE;
import static com.caiya.companion.constant.UserConstant.USER_LOGIN_STATE;

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
    @Resource
    private UserTagService userTagService;
    @Resource
    private TagService tagService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
        safetyUser.setProfile(originUser.getProfile());
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
            // 这段代码会先创建一个Optional对象，如果tmpTagNameListSet不为空，则包含tmpTagNameListSet，反之则包含null
            // 如果Optional包含的是null，则会走orElse为Optional设置一个默认值，然后返回新的Set<String>实例
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
     * 判断是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 从session中获取用户信息
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 判断用户是否为空
        if (user == null) throw new BusinessException(ErrorCode.NO_AUTH);
        return user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 判断是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        // 判断用户是否为空
        if (loginUser == null) throw new BusinessException(ErrorCode.NO_AUTH);
        return loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 修改用户数据，只有管理员 或 用户只能修改自己的信息
     *
     * @param user    待修改的用户信息
     * @param request
     * @return 受影响行数
     */
    @Override
    public int updateUser(User user, HttpServletRequest request) {
        // 判断用户id是否正确
        if (user.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户信息
        User loginUser = getLoginUser(request);
        // 根据id查询出要修改的用户信息
        User oldUser = userMapper.selectById(user.getId());
        // 1. 判断当前登录用户是否为管理员 || 用户修改是否是自己的信息
        if (isAdmin(loginUser) || loginUser.getId().equals(oldUser.getId()))
            // 2. 触发修改
            return userMapper.updateById(user);
        // 以上都不符合则报错
        throw new BusinessException(ErrorCode.NO_AUTH);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) throw new BusinessException(ErrorCode.NULL_ERROR);
        long userId = currentUser.getId();
        User dbUser = userMapper.selectById(userId);
        return getSafetyUser(dbUser);
    }

    /**
     * 推荐用户分页
     *
     * @param pageNum  当前页
     * @param pageSize 一页多少条
     * @param request
     * @return
     */
    @Override
    public Page<User> recommendUsers(long pageNum, long pageSize, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        String redisKey = "companion:user:recommend:%s";
        // 判断用户是否登录，如果登录则根据登录用户id来存取缓存
        if (loginUser != null) {
            redisKey = String.format(redisKey, loginUser.getId());
        } else {
            // 不登录则默认存取公共缓存
            redisKey = String.format(redisKey, 0);
        }
        // 去读Redis缓存
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) opsForValue.get(redisKey);
        // 如果redis中是有缓存，则直接读取缓存并返回
        if (userPage != null) {
            return userPage;
        }
        // 如果没有缓存，则直接查询数据库，将结果存储到redis中，且返回结果
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> page = new Page<>(pageNum, pageSize);
        userPage = this.page(page, queryWrapper);
        // 存入redis
        try {
            opsForValue.set(redisKey, userPage, 1, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("set redis key error：", e);
        }
        return userPage;
    }

    /**
     * 推荐匹配用户列表
     *
     * @param num
     * @param request
     * @return
     */
    @Override
    public List<User> matchUsers(Integer num, HttpServletRequest request) {
        // 获取所有用户信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.isNotNull("tags");
        // 原始用户数据列表
        List<User> originUserList = this.list(userQueryWrapper);
        // 获取登录用户信息作为推荐
        User loginUser = getLoginUser(request);
        // 1. 如果用户未登录，则随机取出用户
        if (loginUser == null) {
            return getRandomElements(originUserList, num);
        }

        // 2. 如果用户已登录，则根据编辑距离算法进行推荐
        // 获取登录用户的标签
        String loginUserTags = loginUser.getTags();
        Gson gson = new Gson();
        // 将获取到标签转为java数组对象，因为从登录用户取出的是字符串类型的，需要转一下，方便后面操作
        List<String> loginUserTagList = gson.fromJson(loginUserTags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> indexUserDistanceList = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < originUserList.size(); i++) {
            User user = originUserList.get(i);
            String userTags = user.getTags();
            List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 标签为空 || 剔除自己
            if (CollectionUtils.isEmpty(userTagsList) || Objects.equals(user.getId(), loginUser.getId())) continue;
            // 计算分数
            long distance = AlgorithmUtils.minDistance(loginUserTagList, userTagsList);
            indexUserDistanceList.add(new Pair<>(user, distance));
        }
        // 根据编辑距离排序，距离越小，用户相似度越高
        List<Pair<User, Long>> topUserPairList = indexUserDistanceList.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 取出key：用户
        List<User> result = topUserPairList.stream().map(Pair::getKey).collect(Collectors.toList());
        return result;
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    @Override
    public UserVO getCurrentUser(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) return null;
        long userId = currentUser.getId();
        // 查询用户信息
        User user = this.getById(userId);
        if (user == null) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户数据查询错误");
        // 获取用户所有的标签列表
        QueryWrapper<UserTag> userTagQueryWrapper = new QueryWrapper<>();
        userTagQueryWrapper.eq("userId", userId);
        List<UserTag> findUserTags = userTagService.list(userTagQueryWrapper);
        // 标签信息处理：将标签信息脱敏
        // 存储脱敏后的标签信息
        List<UserTagVO> userTags = new ArrayList<>();
        // 遍历用户自己有的标签关系数组
        for (UserTag userTag :findUserTags) {
            // 获取标签具体信息
            Long tagId = userTag.getTagId();
            Tag tag = tagService.getById(tagId);
            // 信息脱敏
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(tag, tagVO);
            UserTagVO userTagVO = new UserTagVO();
            userTagVO.setTag(tagVO);
            userTagVO.setWeight(userTag.getWeight());
            userTags.add(userTagVO);
        }
        // 按照标签权重进行排序
        userTags = userTags.stream().sorted(Comparator.comparingInt(UserTagVO::getWeight).reversed()).collect(Collectors.toList());
        // 用户信息脱敏
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setUserTags(userTags);
        return userVO;
    }

    /**
     * 随机取出指定条数的用户数据
     *
     * @param list
     * @param num
     * @return
     */
    public static List<User> getRandomElements(List<User> list, int num) {
        Random rand = new Random();
        List<User> result = new ArrayList<>();

        while (result.size() < num) {
            int index = rand.nextInt(list.size());
            if (!result.contains(list.get(index))) { // 防止重复（如果需要的话）
                result.add(list.get(index));
            }
        }
        return result;
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




