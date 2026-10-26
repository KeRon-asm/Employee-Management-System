package com.companyz.ems.model;

import java.math.BigDecimal;

/**
 * One row of reports 6b and 6c: total gross pay for a month, for one job title or division.
 *
 * @param group the job title or division name
 */
public record PayTotal(String group, int employeeCount, BigDecimal totalPay) {
}
