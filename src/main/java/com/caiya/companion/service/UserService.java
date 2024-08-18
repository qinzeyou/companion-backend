package com.caiya.companion.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caiya.companion.common.BaseResponse;
import com.caiya.companion.common.PageRequest;
import com.caiya.companion.common.PageResponse;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Administrator
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-05-04 20:38:32
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 账号
     * @param password 密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String password);

    /**
     * 登录
     * @param userAccount 账号
     * @param password 密码
     * @param request http请求
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(String userAccount, String password, HttpServletRequest request);

    /**
     * 用户数据脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);


    /**
     * 判断是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 判断是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(UserVO loginUser);

    /**
     * 修改用户数据
     *
     * @param user
     * @param request
     * @return
     */
    int updateUser(User user, HttpServletRequest request);

    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 推荐用户分页
     *
     * @param pageNum 当前页
     * @param pageSize 一页多少条
     * @param request
     * @return
     */
    PageResponse<List<UserVO>>  recommendUsers(long pageNum, long pageSize, HttpServletRequest request);


    /**
     * 处理mybatis-plus分页，将用户脱敏，然后存储到自定义分页结果
     * @param findUserPage 分页数据
     * @return 自定义分页结果，存储脱敏后的用户数据
     */
    PageResponse<List<UserVO>> getListPageResponse(Page<User> findUserPage);


    /**
     * 推荐匹配用户列表
     *
     * @param num 匹配用户的数量
     * @param user 匹配用户
     * @return 匹配的用户列表
     */
    List<UserVO> matchUsers(Integer num, User user);

    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    UserVO getCurrentUser(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagIdList 标签id列表
     * @return 拥有这些标签的用户
     */
    List<UserVO> searchUserByTagIds(List<Integer> tagIdList);

    /**
     * 根据用户id获取用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    UserVO getUserInfoById(Long userId);
}
