package com.companyz.ems.report;

import com.companyz.ems.dao.PayrollDao;
import com.companyz.ems.model.PayStatement;
import com.companyz.ems.model.Role;
import com.companyz.ems.model.Session;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Report 6a: a full-time employee's pay statement history, most recent pay date first.
 * Always the signed-in employee's own statements -- there is no empID input to tamper with.
 * Replaces Company Z's current PDF-only pay stubs.
 */
public class PayHistoryReport extends Report<Void> {

    private final PayrollDao payrollDao;

    public PayHistoryReport(PayrollDao payrollDao) {
        super("6a", "My Pay Statement History", Set.of(Role.EMPLOYEE));
        this.payrollDao = payrollDao;
    }

    @Override
    public List<String> columns() {
        return List.of("Pay Date", "Gross Pay", "Federal Tax", "Medicare", "Social Security",
                "State Tax", "401(k)", "Health Care", "Net Pay");
    }

    @Override
    protected List<List<Object>> generate(Session session, Void unused) {
        if (session.empId() == null) {
            throw new IllegalStateException(session.username() + " has no employee record");
        }
        return payrollDao.findByEmployeeNewestFirst(session.empId()).stream()
                // Re-sort here so the requirement holds even if a DAO forgets ORDER BY
                .sorted(Comparator.comparing(PayStatement::payDate).reversed())
                .map(p -> List.<Object>of(p.payDate(), p.earnings(), p.federalTax(), p.federalMedicare(),
                        p.federalSocialSecurity(), p.stateTax(), p.retirement401k(), p.healthCare(), p.netPay()))
                .toList();
    }
}
