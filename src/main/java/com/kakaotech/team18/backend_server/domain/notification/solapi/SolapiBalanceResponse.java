package com.kakaotech.team18.backend_server.domain.notification.solapi;

import java.math.BigDecimal;

public record SolapiBalanceResponse(
        BigDecimal balance,
        BigDecimal point
) {

    public SolapiBalanceResponse {
        balance = balance == null ? BigDecimal.ZERO : balance;
        point = point == null ? BigDecimal.ZERO : point;
    }

    public BigDecimal availableAmount() {
        return balance.add(point);
    }
}
