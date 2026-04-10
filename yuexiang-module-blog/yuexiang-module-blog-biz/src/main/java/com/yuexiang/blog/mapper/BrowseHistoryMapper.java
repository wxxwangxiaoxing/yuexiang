package com.yuexiang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.blog.domain.entity.BrowseHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BrowseHistoryMapper extends BaseMapper<BrowseHistory> {

    boolean insertOrUpdate(BrowseHistory browseHistory);
}
