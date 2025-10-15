package com.levelup.payment_service.dto.response;

import java.math.BigDecimal;

public class RevenueSummaryResponse {
    private BigDecimal revenue;

    public RevenueSummaryResponse(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
