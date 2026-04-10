package com.yuexiang.user.mapper;

import com.yuexiang.user.domain.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCenterMapper {

    List<MyVoucherVO> selectMyVouchers(@Param("userId") Long userId, @Param("status") Integer status,
                                        @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countMyVouchers(@Param("userId") Long userId, @Param("status") Integer status);

    List<MyOrderVO> selectMyOrders(@Param("userId") Long userId, @Param("status") Integer status,
                                    @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countMyOrders(@Param("userId") Long userId, @Param("status") Integer status);

    List<MyBlogVO> selectMyBlogs(@Param("userId") Long userId, @Param("status") Integer status,
                                  @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countMyBlogs(@Param("userId") Long userId, @Param("status") Integer status);

    List<MyFavoriteVO> selectMyFavorites(@Param("userId") Long userId, @Param("type") Integer type,
                                          @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countMyFavorites(@Param("userId") Long userId, @Param("type") Integer type);

    List<BrowseHistoryItemVO> selectBrowseHistory(@Param("userId") Long userId, @Param("type") Integer type,
                                                   @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countBrowseHistory(@Param("userId") Long userId, @Param("type") Integer type);

    int clearBrowseHistory(@Param("userId") Long userId, @Param("type") Integer type);

    List<AiRecordVO> selectAiRecords(@Param("userId") Long userId,
                                      @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countAiRecords(@Param("userId") Long userId);
}
