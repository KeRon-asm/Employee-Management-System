package com.companyz.ems.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.companyz.ems.dao.PayrollDao;
import com.companyz.ems.dao.ReportDao;
import com.companyz.ems.model.DateRange;
import com.companyz.ems.model.Employee;
import com.companyz.ems.model.PayStatement;
import com.companyz.ems.model.PayTotal;
import com.companyz.ems.model.Role;
import com.companyz.ems.model.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Checks each feature 6 report against the roles and ordering in the user story. No database needed. */
class ReportTest {

    private static final Session HR = new Session(1, "hradmin", Role.HR_ADMIN, null);
    private static final Session EMPLOYEE = new Session(2, "sbeagle", Role.EMPLOYEE, 1);

    private final FakeReportDao reportDao = new FakeReportDao();

    // ---- Who can run what (user story feature 6) ----

    @Test
    void hrOnlyReportsRejectGeneralEmployees() {
        assertThrows(SecurityException.class,
                () -> new MonthlyPayByJobTitleReport(reportDao).run(EMPLOYEE, YearMonth.of(2026, 9)));
        assertThrows(SecurityException.class,
                () -> new MonthlyPayByDivisionReport(reportDao).run(EMPLOYEE, YearMonth.of(2026, 9)));
        assertThrows(SecurityException.class,
                () -> new EmployeesByJobTitleReport(reportDao).run(EMPLOYEE, null));
        assertThrows(SecurityException.class,
                () -> new EmployeesByJobTitleReport(reportDao).groups(EMPLOYEE));
    }

    @Test
    void newHiresReportIsOpenToAllEmployees() {
        NewHiresReport report = new NewHiresReport(reportDao);
        assertTrue(report.isAllowedFor(Role.HR_ADMIN));
        assertTrue(report.isAllowedFor(Role.EMPLOYEE));
    }

    @Test
    void newHiresReportNeverShowsSalaryOrSsn() {
        List<String> columns = new NewHiresReport(reportDao).columns();
        assertFalse(columns.contains("Salary"));
        assertFalse(columns.contains("SSN"));
    }

    @Test
    void payHistoryIsForGeneralEmployees() {
        assertTrue(new PayHistoryReport(id -> List.of()).isAllowedFor(Role.EMPLOYEE));
    }

    // ---- 6a: own statements, most recent first ----

    @Test
    void payHistoryShowsOnlyOwnStatementsNewestFirst() {
        List<Integer> requestedIds = new ArrayList<>();
        PayrollDao payrollDao = empId -> {
            requestedIds.add(empId);
            // Deliberately out of order to prove the report sorts
            return List.of(pay(LocalDate.of(2026, 9, 15)), pay(LocalDate.of(2026, 9, 30)), pay(LocalDate.of(2026, 8, 31)));
        };

        List<List<Object>> rows = new PayHistoryReport(payrollDao).run(EMPLOYEE, null);

        assertEquals(List.of(1), requestedIds);
        assertEquals(LocalDate.of(2026, 9, 30), rows.get(0).get(0));
        assertEquals(LocalDate.of(2026, 9, 15), rows.get(1).get(0));
        assertEquals(LocalDate.of(2026, 8, 31), rows.get(2).get(0));
    }

    // ---- 6b / 6c: totals plus a grand total ----

    @Test
    void monthlyTotalsEndWithAGrandTotal() {
        List<List<Object>> rows = new MonthlyPayByJobTitleReport(reportDao).run(HR, YearMonth.of(2026, 9));
        assertEquals(YearMonth.of(2026, 9), reportDao.lastMonth);
        assertEquals(List.of("Accountant", 2, new BigDecimal("12000.00")), rows.get(0));
        assertEquals(List.of(MonthlyPayTotalReport.TOTAL_LABEL, 3, new BigDecimal("16000.00")), rows.get(rows.size() - 1));
    }

    // ---- 6e: grouped by job title ----

    @Test
    void employeesAreGroupedByJobTitleAlphabetically() {
        Map<String, List<Employee>> groups = new EmployeesByJobTitleReport(reportDao).groups(HR);
        assertEquals(List.of("Accountant", "Software Engineer", EmployeesByJobTitleReport.NO_TITLE),
                new ArrayList<>(groups.keySet()));
        assertEquals("Okafor", groups.get("Software Engineer").get(0).getLastName()); // before Zane
    }

    // ---- helpers ----

    private static PayStatement pay(LocalDate date) {
        BigDecimal z = BigDecimal.ZERO;
        return new PayStatement(0, 1, date, new BigDecimal("1875.00"), z, z, z, z, z, z);
    }

    private static Employee employee(int id, String first, String last, String title) {
        Employee e = new Employee();
        e.setEmpId(id);
        e.setFirstName(first);
        e.setLastName(last);
        e.setJobTitle(title);
        e.setHireDate(LocalDate.of(2026, 1, id % 28 + 1));
        e.setSalary(new BigDecimal("60000.00"));
        return e;
    }

    private static final class FakeReportDao implements ReportDao {
        YearMonth lastMonth;

        @Override
        public List<PayTotal> totalPayByJobTitle(YearMonth month) {
            lastMonth = month;
            return List.of(new PayTotal("Accountant", 2, new BigDecimal("12000.00")),
                    new PayTotal("Software Engineer", 1, new BigDecimal("4000.00")));
        }

        @Override
        public List<PayTotal> totalPayByDivision(YearMonth month) {
            lastMonth = month;
            return List.of();
        }

        @Override
        public List<Employee> findHiredBetween(DateRange range) {
            return List.of();
        }

        @Override
        public List<Employee> findAllWithJobAndDivision() {
            return List.of(employee(1, "Ann", "Zane", "Software Engineer"),
                    employee(2, "Bo", "Young", null),
                    employee(3, "Daniel", "Okafor", "Software Engineer"),
                    employee(4, "Marcus", "Reed", "Accountant"));
        }
    }
}
