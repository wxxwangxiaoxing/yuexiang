package com.yuexiang.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "yuexiang.business")
public class BusinessProperties {

    @Valid
    private Points points = new Points();
    @Valid
    private Sign sign = new Sign();
    @Valid
    private Wallet wallet = new Wallet();
    @Valid
    private Account account = new Account();
    @Valid
    private Blog blog = new Blog();

    @Getter
    @Setter
    public static class Points {
        @Min(1)
        private int defaultLevel = 1;
        @Positive
        private int maxPoints = 1000000;
        @PositiveOrZero
        private int minPoints = 0;
        @Positive
        private int bonusPoints = 30;
        @Positive
        private int bonusTriggerDays = 7;
    }

    @Getter
    @Setter
    public static class Sign {
        @PositiveOrZero
        private int repairCostPoints = 50;
        @Min(1)
        private int repairMaxPerMonth = 3;
        @Positive
        private int repairWindowDays = 7;
    }

    @Getter
    @Setter
    public static class Wallet {
        @Min(1)
        private int maxRetry = 3;
        @Positive
        private long maxAmount = 100_000_000L;
    }

    @Getter
    @Setter
    public static class Account {
        @Min(1)
        private int maxErrL1 = 3;
        @Min(1)
        private int maxErrL2 = 5;
        @Min(1)
        private int maxErrL3 = 10;
        @Positive
        private int lockMinutesL1 = 120;
        @Positive
        private int lockMinutesL2 = 1440;
        @Positive
        private int coolingDays = 7;
        @Min(4)
        @Max(12)
        private int bcryptStrength = 10;
    }

    @Getter
    @Setter
    public static class Blog {
        @Min(1)
        private int maxMonthLookback = 13;
    }
}
