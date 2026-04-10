package com.yuexiang.user.api;

import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.user.domain.vo.*;

public interface UserCenterService {

    PageResult<MyVoucherVO> getMyVouchers(Long userId, Integer status, Integer pageNo, Integer pageSize);

    PageResult<MyOrderVO> getMyOrders(Long userId, Integer status, Integer pageNo, Integer pageSize);

    PageResult<MyBlogVO> getMyBlogs(Long userId, Integer status, Integer pageNo, Integer pageSize);

    PageResult<MyFavoriteVO> getMyFavorites(Long userId, Integer type, Integer pageNo, Integer pageSize);

    BrowseHistoryPageVO getBrowseHistory(Long userId, Integer type, Integer pageNo, Integer pageSize);

    int clearBrowseHistory(Long userId, Integer type);

    PageResult<AiRecordVO> getAiRecords(Long userId, Integer pageNo, Integer pageSize);
}
