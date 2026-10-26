package com.companyz.ems.report;

import com.companyz.ems.dao.ReportDao;
import com.companyz.ems.model.Employee;
import com.companyz.ems.model.Role;
import com.companyz.ems.model.Session;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Report 6e: all employees grouped by job title, for management. HR admin only. */
public class EmployeesByJobTitleReport extends Report<Void> {

    public static final String NO_TITLE = "(No job title)";

    private final ReportDao reportDao;

    public EmployeesByJobTitleReport(ReportDao reportDao) {
        super("6e", "Employees by Job Title", Set.of(Role.HR_ADMIN));
        this.reportDao = reportDao;
    }

    @Override
    public List<String> columns() {
        return List.of("Job Title", "Emp ID", "Name", "Division", "Hire Date", "Salary");
    }

    /** Employees grouped by job title (titles A-Z, names A-Z within each). Handy for a grouped UI. */
    public Map<String, List<Employee>> groups(Session session) {
        requireAccess(session);
        return group(reportDao.findAllWithJobAndDivision());
    }

    @Override
    protected List<List<Object>> generate(Session session, Void unused) {
        return group(reportDao.findAllWithJobAndDivision()).entrySet().stream()
                .flatMap(g -> g.getValue().stream().map(e -> List.<Object>of(g.getKey(), e.getEmpId(),
                        e.getFullName(), e.getDivision() == null ? "" : e.getDivision(),
                        e.getHireDate(), e.getSalary())))
                .toList();
    }

    private static Map<String, List<Employee>> group(List<Employee> employees) {
        // Real titles A-Z, then employees with no title last
        Comparator<Employee> byTitle = Comparator.comparing(
                e -> NO_TITLE.equals(titleOf(e)) ? null : titleOf(e),
                Comparator.nullsLast(Comparator.<String>naturalOrder()));
        Comparator<Employee> byName = Comparator.comparing(Employee::getLastName)
                .thenComparing(Employee::getFirstName);
        return employees.stream()
                .sorted(byTitle.thenComparing(byName))
                .collect(Collectors.groupingBy(EmployeesByJobTitleReport::titleOf,
                        LinkedHashMap::new, Collectors.toList()));
    }

    private static String titleOf(Employee e) {
        return e.getJobTitle() == null || e.getJobTitle().isBlank() ? NO_TITLE : e.getJobTitle();
    }
}
