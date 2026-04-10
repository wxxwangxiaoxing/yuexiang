package com.yuexiang.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuexiang.user.domain.entity.UserMessage;
import com.yuexiang.user.domain.vo.MessageVO;
import com.yuexiang.user.mapper.UserMessageMapper;
import com.yuexiang.user.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMessageServiceImpl implements UserMessageService {

    private final UserMessageMapper userMessageMapper;

    @Override
    public List<MessageVO> getMessages(Long userId, Integer type) {
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId);
        if (type != null) {
            wrapper.eq(UserMessage::getType, type);
        }
        wrapper.orderByDesc(UserMessage::getCreateTime);
        wrapper.last("LIMIT 50");

        List<UserMessage> messages = userMessageMapper.selectList(wrapper);
        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public int markAllRead(Long userId) {
        LambdaUpdateWrapper<UserMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId);
        wrapper.eq(UserMessage::getIsRead, 0);
        wrapper.set(UserMessage::getIsRead, 1);
        return userMessageMapper.update(null, wrapper);
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId);
        wrapper.eq(UserMessage::getIsRead, 0);
        return Math.toIntExact(userMessageMapper.selectCount(wrapper));
    }

    private MessageVO convertToVO(UserMessage message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setType(message.getType());
        vo.setTypeDesc(getTypeDesc(message.getType()));
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setBizId(message.getBizId());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime() != null
                ? message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null);
        return vo;
    }

    private String getTypeDesc(Integer type) {
        return switch (type) {
            case 1 -> "系统通知";
            case 2 -> "点赞收藏";
            case 3 -> "新增关注";
            case 4 -> "评论和@";
            default -> "其他";
        };
    }
}
