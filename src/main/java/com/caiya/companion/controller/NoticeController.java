package com.caiya.companion.controller;

import com.caiya.companion.common.*;
import com.caiya.companion.exception.BusinessException;
import com.caiya.companion.model.domain.User;
import com.caiya.companion.model.request.NoticeAddRequest;
import com.caiya.companion.model.request.NoticeUpdateRequest;
import com.caiya.companion.model.vo.NoticeVO;
import com.caiya.companion.service.NoticeService;
import com.caiya.companion.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author caiya
 * @description 公告控制层
 * @create 2024-08-18 19::43
 */
@RequestMapping("/notice")
@RestController
public class NoticeController {

    @Resource
    private NoticeService noticeService;
    @Resource
    private UserService userService;

    /**
     * 添加公告
     * @param noticeAddRequest 请求参数
     * @param request 登录信息
     * @return 成功插入的id
     */
    @PostMapping()
    private BaseResponse<Long> addNotice(@RequestBody NoticeAddRequest noticeAddRequest, HttpServletRequest request) {
        if (noticeAddRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        Long res = noticeService.addNotice(noticeAddRequest, loginUser);
        return ResultUtils.success(res);
    }


    /**
     * 删除公告
     * @param noticeId 公告id
     * @param request 登录信息
     * @return 成功回调，true删除成，false删除失败
     */
    @DeleteMapping("/{id}")
    private BaseResponse<Boolean> deleteNotice(@PathVariable("id") Long noticeId, HttpServletRequest request) {
        if (noticeId == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        Boolean res = noticeService.deleteNotice(noticeId);
        return ResultUtils.success(res);
    }

    /**
     * 修改公告
     * @param noticeUpdateRequest 请求参数
     * @param request 登录信息
     * @return 成功回调，true删除成，false删除失败
     */
    @PutMapping()
    private BaseResponse<Boolean> updateNotice(@RequestBody NoticeUpdateRequest noticeUpdateRequest, HttpServletRequest request) {
        if (noticeUpdateRequest == null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        User loginUser = userService.getLoginUser(request);
        Boolean res = noticeService.updateNotice(noticeUpdateRequest);
        return ResultUtils.success(res);
    }

    /**
     * 公告分页
     * @param request 分页参数
     * @return 公告分页数据
     */
    @PostMapping("/page")
    private BaseResponse<PageResponse<List<NoticeVO>>> noticePage(@RequestBody PageRequest request) {
        PageResponse<List<NoticeVO>> res = noticeService.noticePage(request);
        return ResultUtils.success(res);
    }
}
