package com.caiya.companion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caiya.companion.common.ErrorCode;
import com.caiya.companion.constant.UserConstant;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.mapper.UserMapper;
import com.caiya.companion.mapper.UserTagMapper;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.domain.UserTag;
import com.caiya.companion.model.vo.TagVO;
import com.caiya.companion.model.vo.UserVO;
import com.caiya.companion.service.UserService;
import com.caiya.companion.service.UserTagService;
import com.caiya.companion.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    private UserTagMapper userTagMapper;
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
    public UserVO userLogin(String userAccount, String password, HttpServletRequest request) {
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
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        // 存储登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userVO);

        return userVO;
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

    @Override
    public boolean isAdmin(UserVO loginUser) {
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
        UserVO currentUser = (UserVO) request.getSession().getAttribute(USER_LOGIN_STATE);
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
        UserVO loginUser = (UserVO) request.getSession().getAttribute(USER_LOGIN_STATE);
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
    public List<UserVO> matchUsers(Integer num, HttpServletRequest request) {
        // 获取所有用户信息
        List<User> originUserList = userMapper.selectList(new QueryWrapper<>());
        // 获取登录用户信息作为推荐
        User loginUser = getLoginUser(request);
        // 存储 拥有标签数据 的用户
        List<UserVO> userVOList = new ArrayList<>();
        // 处理用户数据，统一查询用户的标签，并设置到字段中
        for (User user : originUserList) {
            // 根据用户id查询出他所属的标签（已脱敏）
            List<TagVO> userTagVOList = userTagService.getTagByUserId(user.getId());
            // 用户信息脱敏
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            // 将标签数组存到字段中
            userVO.setUserTags(userTagVOList);
            userVOList.add(userVO);
        }

        // 如果用户未登录，则随机取出用户
        if (loginUser == null)
            return getRandomElements(userVOList, num);
        // 用户列表的下标 => 相似度
        List<Pair<UserVO, Long>> indexUserDistanceList = new ArrayList<>();
        // 当前登录用户所属的标签名称数组
        List<String> loginUserTagNameList = userTagService.getTagByUserId(loginUser.getId()).stream().map(TagVO::getTagName).collect(Collectors.toList());
        // 依次计算所有用户和当前用户的相似度
        for (UserVO matchUser : userVOList) {
            // 待匹配用户所属的标签名称数组
            List<String> matchUserTagNameList = matchUser.getUserTags().stream().map(TagVO::getTagName).collect(Collectors.toList());

            // 标签为空 || 剔除自己
            if (CollectionUtils.isEmpty(matchUserTagNameList) || Objects.equals(matchUser.getId(), loginUser.getId()))
                continue;
            // 计算分数
            long distance = AlgorithmUtils.minDistance(loginUserTagNameList, matchUserTagNameList);
            indexUserDistanceList.add(new Pair<>(matchUser, distance));
        }
        // 根据编辑距离排序，距离越小，用户相似度越高
        List<Pair<UserVO, Long>> topUserPairList = indexUserDistanceList.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 取出key：用户
        return topUserPairList.stream().map(Pair::getKey).collect(Collectors.toList());
    }

//    /**
//     * 推荐匹配用户列表
//     *
//     * @param num
//     * @param request
//     * @return
//     */
//    @Override
//    public List<User> matchUsers(Integer num, HttpServletRequest request) {
//        // 获取所有用户信息
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//        userQueryWrapper.isNotNull("tags");
//        // 原始用户数据列表
//        List<User> originUserList = this.list(userQueryWrapper);
//        // 获取登录用户信息作为推荐
//        User loginUser = getLoginUser(request);
//        // 1. 如果用户未登录，则随机取出用户
//        if (loginUser == null) {
//            return getRandomElements(originUserList, num);
//        }
//
//        // 2. 如果用户已登录，则根据编辑距离算法进行推荐
//        // 获取登录用户的标签
//        String loginUserTags = loginUser.getTags();
//        Gson gson = new Gson();
//        // 将获取到标签转为java数组对象，因为从登录用户取出的是字符串类型的，需要转一下，方便后面操作
//        List<String> loginUserTagList = gson.fromJson(loginUserTags, new TypeToken<List<String>>() {
//        }.getType());
//        // 用户列表的下标 => 相似度
//        List<Pair<User, Long>> indexUserDistanceList = new ArrayList<>();
//        // 依次计算所有用户和当前用户的相似度
//        for (int i = 0; i < originUserList.size(); i++) {
//            User user = originUserList.get(i);
//            String userTags = user.getTags();
//            List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>() {
//            }.getType());
//            // 标签为空 || 剔除自己
//            if (CollectionUtils.isEmpty(userTagsList) || Objects.equals(user.getId(), loginUser.getId())) continue;
//            // 计算分数
//            long distance = AlgorithmUtils.minDistance(loginUserTagList, userTagsList);
//            indexUserDistanceList.add(new Pair<>(user, distance));
//        }
//        // 根据编辑距离排序，距离越小，用户相似度越高
//        List<Pair<User, Long>> topUserPairList = indexUserDistanceList.stream()
//                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
//                .limit(num)
//                .collect(Collectors.toList());
//        // 取出key：用户
//        List<User> result = topUserPairList.stream().map(Pair::getKey).collect(Collectors.toList());
//        return result;
//    }

    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    @Override
    public UserVO getCurrentUser(HttpServletRequest request) {
        UserVO currentUser = (UserVO) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) return null;
        long userId = currentUser.getId();
        // 查询用户信息
        User user = this.getById(userId);
        if (user == null) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户数据查询错误");
        // 获取用户所有的标签列表
        List<TagVO> tagVOList = userTagService.getTagByUserId(userId);
        // 按照标签权重进行排序
        tagVOList = tagVOList.stream().sorted(Comparator.comparingInt(TagVO::getWeight).reversed()).collect(Collectors.toList());
        // 用户信息脱敏
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setUserTags(tagVOList);
        return userVO;
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagIdList 标签id列表
     * @return 拥有这些标签的用户
     */
    @Override
    public List<UserVO> searchUserByTagIds(List<Integer> tagIdList) {
        // 判断列表是否为空
        if (CollectionUtils.isEmpty(tagIdList)) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        // 查询关联关系，获取用户这些标签的用户
        QueryWrapper<UserTag> userTagQueryWrapper = new QueryWrapper<>();
        userTagQueryWrapper.in("tagId", tagIdList);
        List<UserTag> userTags = userTagService.list(userTagQueryWrapper);
        // 结果集
        List<UserVO> res = new ArrayList<>();
        // 去重，因为遍历所有用户标签关系，如果一个用户同事存在多个标签，会重复添加到结果中
        Set<Long> userIdSet = new HashSet<>();
        for (UserTag userTag : userTags) {
            // 用户信息
            // 获取用户id，查询用户信息
            Long userId = userTag.getUserId();
            // 如果该用户已存在结果集中，则跳过，避免出现重复用户
            if (userIdSet.contains(userId)) continue;
            User user = userMapper.selectById(userId);
            // 根据用户id查询用户身上所有的标签，返回脱敏的标签列表
            List<TagVO> tagVOList = userTagService.getTagByUserId(userId);
            // 信息脱敏
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVO.setUserTags(tagVOList);
            res.add(userVO);
            userIdSet.add(userId);
        }

        return res;
    }

    /**
     * 随机取出指定条数的用户数据
     *
     * @param list 待随机的用户数组
     * @param num  指定条数
     * @return 随机不重复的制定条数的用户数据数组
     */
    public static List<UserVO> getRandomElements(List<UserVO> list, int num) {
        Random rand = new Random();
        List<UserVO> result = new ArrayList<>();

        while (result.size() < num) {
            int index = rand.nextInt(list.size());
            if (!result.contains(list.get(index))) { // 防止重复
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




