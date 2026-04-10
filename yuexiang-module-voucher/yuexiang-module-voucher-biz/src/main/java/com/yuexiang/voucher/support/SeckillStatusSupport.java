package com.yuexiang.voucher.support;

import com.yuexiang.voucher.domain.entity.SeckillSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class SeckillStatusSupport {

    public int getSessionStatus(SeckillSession session, long now) {
        long begin = toTimestamp(session.getBeginTime());
        long end = toTimestamp(session.getEndTime());
        if (now < begin) {
            return 0;
        }
        if (now > end) {
            return 2;
        }
        return 1;
    }

    public int getSeckillStatus(LocalDateTime beginTime, LocalDateTime endTime, long now, Integer remainStock) {
        long begin = toTimestamp(beginTime);
        long end = toTimestamp(endTime);
        if (now < begin) {
            return 0;
        }
        if (now > end) {
            return 2;
        }
        if (remainStock != null && remainStock <= 0) {
            return 3;
        }
        return 1;
    }

    public String getSessionStatusDesc(int status) {
        return switch (status) {
            case 0 -> "即将开始";
            case 1 -> "抢购中";
            case 2 -> "已结束";
            default -> "未知";
        };
    }

    public String getSeckillStatusDesc(int status) {
        return switch (status) {
            case 0 -> "未开始";
            case 1 -> "进行中";
            case 2 -> "已结束";
            case 3 -> "已抢光";
            default -> "未知";
        };
    }

    public String getOrderStatusDesc(int status) {
        return switch (status) {
            case -1 -> "处理中";
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已使用";
            case 3 -> "已退款";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    public int calculateStockPercent(Integer remain, Integer total) {
        if (total == null || total <= 0 || remain == null) {
            return 0;
        }
        return (int) ((remain * 100.0) / total);
    }

    public Long toTimestamp(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
