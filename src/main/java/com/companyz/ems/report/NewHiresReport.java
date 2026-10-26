package com.companyz.ems.report;

import com.companyz.ems.dao.ReportDao;
import com.companyz.ems.model.DateRange;
import com.companyz.ems.model.Employee;
import com.companyz.ems.model.Role;
import com.companyz.ems.model.Session;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Report 6d: new hires within a date range. Open to ALL employees, so it deliberately
 * shows only directory-level fields -- never salary, SSN, DOB, address or phone.
 */
public class NewHiresReport extends Report<DateRange> {

    private final ReportDao reportDao;

    public NewHiresReport(ReportDao reportDao) {
        super("6d", "New Hires in Date Range", Set.of(Role.HR_ADMIN, Role.EMPLOYEE));
        this.reportDao = reportDao;
    }

    @Override
    public List<String> columns() {
        return List.of("Hire Date", "Emp ID", "Name", "Job Title", "Division");
    }

    @Override
    protected List<List<Object>> generate(Session session, DateRange range) {
        Objects.requireNonNull(range, "range");
        return reportDao.findHiredBetween(range).stream()
                .sorted(Comparator.comparing(Employee::getHireDate).thenComparing(Employee::getLastName))
                .map(e -> List.<Object>of(e.getHireDate(), e.getEmpId(), e.getFullName(),
                        orBlank(e.getJobTitle()), orBlank(e.getDivision())))
                .toList();
    }

    private static String orBlank(String s) {
        return s == null ? "" : s;
    }
}
