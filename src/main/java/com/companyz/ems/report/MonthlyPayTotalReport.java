package com.companyz.ems.report;

import com.companyz.ems.model.PayTotal;
import com.companyz.ems.model.Role;
import com.companyz.ems.model.Session;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Shared shape of reports 6b and 6c: total pay for one month, grouped by something,
 * with a grand-total row. HR admin only. Subclasses only say what they group by.
 */
public abstract class MonthlyPayTotalReport extends Report<YearMonth> {

    public static final String TOTAL_LABEL = "TOTAL";

    protected MonthlyPayTotalReport(String id, String title) {
        super(id, title, Set.of(Role.HR_ADMIN));
    }

    /** Heading for the first column, e.g. "Job Title". */
    protected abstract String groupColumn();

    protected abstract List<PayTotal> fetchTotals(YearMonth month);

    @Override
    public List<String> columns() {
        return List.of(groupColumn(), "Employees Paid", "Total Pay");
    }

    @Override
    protected List<List<Object>> generate(Session session, YearMonth month) {
        Objects.requireNonNull(month, "month");
        List<List<Object>> rows = new ArrayList<>();
        int employees = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (PayTotal t : fetchTotals(month)) {
            rows.add(List.of(t.group(), t.employeeCount(), t.totalPay()));
            employees += t.employeeCount();
            total = total.add(t.totalPay());
        }
        rows.add(List.of(TOTAL_LABEL, employees, total));
        return rows;
    }
}
