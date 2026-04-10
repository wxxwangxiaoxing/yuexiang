package com.yuexiang.shop.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 客户端 IP 提取工具类
 */
public class ClientIpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String COMMA = ",";

    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        // 依次尝试获取常见的代理头
        String ip = request.getHeader("x-forwarded-for");
        if (isUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isUnknown(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        // 对于通过多个代理的情况，第一个 IP 为客户端真实 IP，多个 IP 按照','分割
        if (StringUtils.hasText(ip) && ip.contains(COMMA)) {
            ip = ip.split(COMMA)[0];
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    private static boolean isUnknown(String ip) {
        return !StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip);
    }
}