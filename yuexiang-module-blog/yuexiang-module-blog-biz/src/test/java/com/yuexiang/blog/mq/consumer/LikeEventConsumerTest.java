package com.yuexiang.blog.mq.consumer;

import com.yuexiang.blog.domain.event.LikeEvent;
import com.yuexiang.user.mapper.UserInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeEventConsumerTest {

    @Mock
    private UserInfoMapper userInfoMapper;

    @InjectMocks
    private LikeEventConsumer consumer;

    @BeforeEach
    void setUp() throws Exception {
        Field bufferField = LikeEventConsumer.class.getDeclaredField("deltaBuffer");
        bufferField.setAccessible(true);
        bufferField.set(consumer, new ConcurrentHashMap<>());
    }

    @Test
    @DisplayName("消息聚合 - 点赞事件")
    void onMessage_LikeEvent() {
        LikeEvent event = LikeEvent.builder()
                .authorId(1L)
                .delta(1)
                .type("LIKE")
                .build();

        consumer.onMessage(event);

        verify(userInfoMapper, never()).updateLikeCount(any(), any());
    }

    @Test
    @DisplayName("消息聚合 - 多次点赞合并")
    void onMessage_MultipleLikes() {
        LikeEvent event1 = LikeEvent.builder().authorId(1L).delta(1).build();
        LikeEvent event2 = LikeEvent.builder().authorId(1L).delta(1).build();
        LikeEvent event3 = LikeEvent.builder().authorId(1L).delta(-1).build();

        consumer.onMessage(event1);
        consumer.onMessage(event2);
        consumer.onMessage(event3);

        verify(userInfoMapper, never()).updateLikeCount(any(), any());
    }

    @Test
    @DisplayName("flush缓冲区 - 成功更新")
    void flushDeltaBuffer_Success() {
        when(userInfoMapper.updateLikeCount(anyLong(), anyInt())).thenReturn(1);

        LikeEvent event = LikeEvent.builder().authorId(1L).delta(3).build();
        consumer.onMessage(event);

        consumer.flushDeltaBuffer();

        verify(userInfoMapper).updateLikeCount(1L, 3);
    }

    @Test
    @DisplayName("flush缓冲区 - 失败后数据写回")
    void flushDeltaBuffer_Fail_DataRetained() {
        when(userInfoMapper.updateLikeCount(anyLong(), anyInt()))
                .thenThrow(new RuntimeException("DB error"));

        LikeEvent event = LikeEvent.builder().authorId(1L).delta(1).build();
        consumer.onMessage(event);

        consumer.flushDeltaBuffer();

        verify(userInfoMapper).updateLikeCount(1L, 1);
    }

    @Test
    @DisplayName("flush缓冲区 - 空缓冲区不更新")
    void flushDeltaBuffer_EmptyBuffer() {
        consumer.flushDeltaBuffer();

        verify(userInfoMapper, never()).updateLikeCount(any(), any());
    }
}
