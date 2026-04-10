package com.yuexiang.user.service;

import com.yuexiang.user.domain.vo.MessageVO;

import java.util.List;

public interface UserMessageService {

    List<MessageVO> getMessages(Long userId, Integer type);

    int markAllRead(Long userId);

    int getUnreadCount(Long userId);
}
