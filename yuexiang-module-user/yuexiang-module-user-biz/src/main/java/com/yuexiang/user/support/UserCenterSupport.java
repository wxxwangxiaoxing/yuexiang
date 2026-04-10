package com.yuexiang.user.support;

import com.yuexiang.common.utils.EpochMilliUtil;
import com.yuexiang.user.constant.UserCenterConstants;
import com.yuexiang.user.domain.vo.BrowseHistoryGroupVO;
import com.yuexiang.user.domain.vo.BrowseHistoryItemVO;
import com.yuexiang.user.domain.vo.MyBlogVO;
import com.yuexiang.user.domain.vo.MyFavoriteVO;
import com.yuexiang.user.domain.vo.MyOrderVO;
import com.yuexiang.user.domain.vo.MyVoucherVO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserCenterSupport {

    public void enrichVoucher(MyVoucherVO vo, LocalDateTime now) {
        vo.setStatusDesc(getVoucherStatusDesc(vo.getStatus(), vo.getValidEndTime(), now));

        if (vo.getValidEndTime() != null) {
            vo.setRemainDays(EpochMilliUtil.remainDaysFromNow(vo.getValidEndTime()));
        }
    }

    public void enrichOrder(MyOrderVO vo) {
        vo.setStatusDesc(getOrderStatusDesc(vo.getStatus()));
        vo.setPayTypeDesc(getPayTypeDesc(vo.getPayType()));
        vo.setActions(getOrderActions(vo.getStatus()));
    }

    public void enrichBlog(MyBlogVO vo) {
        vo.setStatusDesc(getBlogStatusDesc(vo.getStatus()));
    }

    public void enrichFavorite(MyFavoriteVO vo) {
        vo.setBizTypeDesc(toBizTypeDesc(vo.getBizType()));
    }

    public void enrichBrowseHistory(BrowseHistoryItemVO vo) {
        vo.setBizTypeDesc(toBizTypeDesc(vo.getBizType()));
    }

    public List<BrowseHistoryGroupVO> groupByDate(List<BrowseHistoryItemVO> records) {
        Map<LocalDate, List<BrowseHistoryItemVO>> grouped = records.stream()
                .collect(Collectors.groupingBy(
                        item -> EpochMilliUtil.toLocalDate(item.getViewTime()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<BrowseHistoryItemVO>>comparingByKey().reversed())
                .map(entry -> BrowseHistoryGroupVO.builder()
                        .date(entry.getKey().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .dateDesc(EpochMilliUtil.toFriendlyDate(entry.getKey()))
                        .list(entry.getValue())
                        .build())
                .toList();
    }

    private String getVoucherStatusDesc(Integer status, Long validEndTime, LocalDateTime now) {
        if (safeInt(status) == UserCenterConstants.VOUCHER_UNUSED && validEndTime != null) {
            if (EpochMilliUtil.toLocalDateTime(validEndTime).isBefore(now)) {
                return "已过期";
            }
        }

        return switch (safeInt(status)) {
            case UserCenterConstants.VOUCHER_PENDING_PAY -> "待支付";
            case UserCenterConstants.VOUCHER_UNUSED -> "未使用";
            case UserCenterConstants.VOUCHER_USED -> "已使用";
            case UserCenterConstants.VOUCHER_REFUNDED -> "已退款";
            case UserCenterConstants.VOUCHER_CANCELLED -> "已取消";
            default -> "未知";
        };
    }

    private String getOrderStatusDesc(Integer status) {
        return switch (safeInt(status)) {
            case UserCenterConstants.ORDER_PENDING_PAY -> "待支付";
            case UserCenterConstants.ORDER_PAID -> "已支付";
            case UserCenterConstants.ORDER_USED -> "已使用";
            case UserCenterConstants.ORDER_REFUNDED -> "已退款";
            case UserCenterConstants.ORDER_CANCELLED -> "已取消";
            default -> "未知";
        };
    }

    private String getPayTypeDesc(Integer payType) {
        return switch (safeInt(payType)) {
            case UserCenterConstants.PAY_NONE -> "-";
            case UserCenterConstants.PAY_BALANCE -> "余额支付";
            case UserCenterConstants.PAY_WECHAT -> "微信支付";
            case UserCenterConstants.PAY_ALIPAY -> "支付宝支付";
            default -> "未知";
        };
    }

    private List<String> getOrderActions(Integer status) {
        return switch (safeInt(status)) {
            case UserCenterConstants.ORDER_PENDING_PAY -> List.of("去支付", "取消订单");
            case UserCenterConstants.ORDER_PAID -> List.of("去使用", "申请退款");
            case UserCenterConstants.ORDER_USED -> List.of("去评价", "再来一单");
            case UserCenterConstants.ORDER_REFUNDED, UserCenterConstants.ORDER_CANCELLED -> List.of("再来一单");
            default -> List.of();
        };
    }

    private String getBlogStatusDesc(Integer status) {
        return switch (safeInt(status)) {
            case UserCenterConstants.BLOG_PENDING -> "待审核";
            case UserCenterConstants.BLOG_PUBLISHED -> "已发布";
            case UserCenterConstants.BLOG_BLOCKED -> "已屏蔽";
            case UserCenterConstants.BLOG_DRAFT -> "草稿";
            default -> "未知";
        };
    }

    private String toBizTypeDesc(Integer bizType) {
        return safeInt(bizType) == UserCenterConstants.BIZ_TYPE_SHOP ? "商户" : "笔记";
    }

    private int safeInt(Integer value) {
        return value != null ? value : -1;
    }
}
