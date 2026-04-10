package com.yuexiang.user.service.impl;

import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.common.utils.PageUtil;
import com.yuexiang.user.constant.UserCenterConstants;
import com.yuexiang.user.api.UserCenterService;
import com.yuexiang.user.assembler.BrowseHistoryPageAssembler;
import com.yuexiang.user.domain.vo.*;
import com.yuexiang.user.mapper.UserCenterMapper;
import com.yuexiang.user.support.UserCenterSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCenterServiceImpl implements UserCenterService {

    private final UserCenterMapper userCenterMapper;
    private final UserCenterSupport userCenterSupport;
    private final BrowseHistoryPageAssembler browseHistoryPageAssembler;

    // ======================== 公开接口 ========================

    @Override
    public PageResult<MyVoucherVO> getMyVouchers(Long userId, Integer status,
                                                  Integer pageNo, Integer pageSize) {
        int offset = PageUtil.offset(pageNo, pageSize);
        List<MyVoucherVO> records = userCenterMapper.selectMyVouchers(userId, status, offset, pageSize);
        Long total = userCenterMapper.countMyVouchers(userId, status);

        LocalDateTime now = LocalDateTime.now();
        records.forEach(vo -> userCenterSupport.enrichVoucher(vo, now));

        return new PageResult<>(records, total);
    }

    @Override
    public PageResult<MyOrderVO> getMyOrders(Long userId, Integer status,
                                              Integer pageNo, Integer pageSize) {
        int offset = PageUtil.offset(pageNo, pageSize);
        List<MyOrderVO> records = userCenterMapper.selectMyOrders(userId, status, offset, pageSize);
        Long total = userCenterMapper.countMyOrders(userId, status);

        records.forEach(userCenterSupport::enrichOrder);

        return new PageResult<>(records, total);
    }

    @Override
    public PageResult<MyBlogVO> getMyBlogs(Long userId, Integer status,
                                            Integer pageNo, Integer pageSize) {
        int offset = PageUtil.offset(pageNo, pageSize);
        List<MyBlogVO> records = userCenterMapper.selectMyBlogs(userId, status, offset, pageSize);
        Long total = userCenterMapper.countMyBlogs(userId, status);

        records.forEach(userCenterSupport::enrichBlog);

        return new PageResult<>(records, total);
    }

    @Override
    public PageResult<MyFavoriteVO> getMyFavorites(Long userId, Integer type,
                                                    Integer pageNo, Integer pageSize) {
        int offset = PageUtil.offset(pageNo, pageSize);
        List<MyFavoriteVO> records = userCenterMapper.selectMyFavorites(userId, type, offset, pageSize);
        Long total = userCenterMapper.countMyFavorites(userId, type);

        records.forEach(userCenterSupport::enrichFavorite);

        return new PageResult<>(records, total);
    }

    @Override
    public BrowseHistoryPageVO getBrowseHistory(Long userId, Integer type,
                                                 Integer pageNo, Integer pageSize) {
        int offset = PageUtil.offset(pageNo, pageSize);
        List<BrowseHistoryItemVO> records = userCenterMapper.selectBrowseHistory(userId, type, offset, pageSize);
        Long total = userCenterMapper.countBrowseHistory(userId, type);

        records.forEach(userCenterSupport::enrichBrowseHistory);

        List<BrowseHistoryGroupVO> groups = userCenterSupport.groupByDate(records);
        return browseHistoryPageAssembler.assemble(total, groups);
    }

    @Override
    public int clearBrowseHistory(Long userId, Integer type) {
        return userCenterMapper.clearBrowseHistory(userId, type);
    }

    @Override
    public PageResult<AiRecordVO> getAiRecords(Long userId, Integer pageNo, Integer pageSize) {
        int offset = PageUtil.offset(pageNo, pageSize);
        List<AiRecordVO> records = userCenterMapper.selectAiRecords(userId, offset, pageSize);
        Long total = userCenterMapper.countAiRecords(userId);
        return new PageResult<>(records, total);
    }
}
