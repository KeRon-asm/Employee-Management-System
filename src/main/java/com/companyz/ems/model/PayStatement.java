package com.companyz.ems.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Row of the {@code payroll} table. */
public record PayStatement(
        int payId,
        int empId,
        LocalDate payDate,
        BigDecimal earnings,
        BigDecimal federalTax,
        BigDecimal federalMedicare,
        BigDecimal federalSocialSecurity,
        BigDecimal stateTax,
        BigDecimal retirement401k,
        BigDecimal healthCare) {

    public BigDecimal totalDeductions() {
        return federalTax.add(federalMedicare).add(federalSocialSecurity)
                .add(stateTax).add(retirement401k).add(healthCare);
    }

    public BigDecimal netPay() {
        return earnings.subtract(totalDeductions());
    }
}
