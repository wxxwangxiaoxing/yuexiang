package com.yuexiang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexiang.blog.constants.BlogConstants;
import com.yuexiang.blog.domain.entity.Blog;
import com.yuexiang.blog.domain.vo.BlogListItemVO;
import com.yuexiang.blog.domain.vo.BlogDetailVO;
import com.yuexiang.blog.mapper.BlogMapper;
import com.yuexiang.blog.service.BlogService;
import com.yuexiang.blog.support.BlogReadSupport;
import com.yuexiang.common.exception.ForbiddenException;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.common.pojo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogMapper blogMapper;
    private final BlogReadSupport blogReadSupport;

    @Override
    public BlogDetailVO getBlogDetail(Long blogId, Long currentUserId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1) {
            throw new NotFoundException("笔记不存在");
        }
        if (blog.getStatus() == BlogConstants.BLOG_STATUS_BLOCKED) {
            throw new ForbiddenException("笔记已屏蔽");
        }
        if (blog.getStatus() != BlogConstants.BLOG_STATUS_PUBLISHED) {
            throw new NotFoundException("笔记不存在");
        }

        BlogDetailVO vo = new BlogDetailVO();
        vo.setId(blog.getId());
        vo.setTitle(blog.getTitle());
        vo.setContent(blog.getContent());
        vo.setLikeCount(blog.getLikeCount());
        vo.setCommentCount(blog.getCommentCount());
        vo.setFavoriteCount(blog.getFavoriteCount());
        vo.setCreateTime(blog.getCreateTime());

        vo.setImages(blogReadSupport.parseImages(blog.getImages()));
        vo.setUser(blogReadSupport.buildUserVO(blog.getUserId(), currentUserId));
        vo.setShop(blogReadSupport.buildShopVO(blog.getShopId()));
        vo.setTags(blogReadSupport.buildTagVOs(blogId));

        if (currentUserId != null) {
            vo.setIsLiked(blogReadSupport.checkIsLiked(blogId, currentUserId));
            vo.setIsFavorited(blogReadSupport.checkIsFavorited(blogId, currentUserId));
            blogReadSupport.asyncRecordBrowseHistory(blogId, currentUserId);
        } else {
            vo.setIsLiked(false);
            vo.setIsFavorited(false);
        }

        return vo;
    }

    @Override
    public PageResult<BlogListItemVO> getBlogList(Long currentUserId, Integer pageNo, Integer pageSize) {
        int pn = (pageNo == null || pageNo <= 0) ? 1 : pageNo;
        int ps = (pageSize == null || pageSize <= 0) ? 10 : pageSize;

        // 只返回已发布、未删除、未屏蔽的笔记
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blog::getStatus, BlogConstants.BLOG_STATUS_PUBLISHED)
                .eq(Blog::getDeleted, 0)
                .orderByDesc(Blog::getCreateTime);

        Page<Blog> pageParam = new Page<>(pn, ps);
        IPage<Blog> pageResult = blogMapper.selectPage(pageParam, wrapper);

        List<BlogListItemVO> list = pageResult.getRecords().stream()
                .map(blog -> {
                    BlogListItemVO vo = new BlogListItemVO();
                    vo.setId(blog.getId());
                    vo.setTitle(blog.getTitle());

                    List<String> images = blogReadSupport.parseImages(blog.getImages());
                    vo.setCover(images.isEmpty() ? null : images.get(0));

                    // 这里沿用 getBlogDetail 里的构造风格（简化用户信息）
                    vo.setAuthor("用户" + blog.getUserId());
                    vo.setAvatar(null);

                    vo.setLikes(blog.getLikeCount());
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public void incrementLikeCount(Long blogId) {
        blogMapper.updateLikeCount(blogId, 1);
    }

    @Override
    public void decrementLikeCount(Long blogId) {
        blogMapper.updateLikeCount(blogId, -1);
    }
}
