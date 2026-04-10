package com.yuexiang.user.support;

import com.yuexiang.user.constant.UserAccountConstants;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserAccountSupport {

    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public String maskRealName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    public String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 6) {
            return idCard;
        }
        return idCard.substring(0, 4)
                + "*".repeat(idCard.length() - 6)
                + idCard.substring(idCard.length() - 2);
    }

    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(UserAccountConstants.BCRYPT_STRENGTH));
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(UserAccountConstants.DT_FORMATTER) : null;
    }

    public boolean isSequentialDigits(String value) {
        if (value == null || value.length() < 2) {
            return false;
        }

        boolean ascending = true;
        boolean descending = true;
        for (int i = 1; i < value.length(); i++) {
            int diff = value.charAt(i) - value.charAt(i - 1);
            if (diff != 1) {
                ascending = false;
            }
            if (diff != -1) {
                descending = false;
            }
        }
        return ascending || descending;
    }
}
