package com.companyz.ems.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.companyz.ems.dao.AuditDao;
import com.companyz.ems.dao.EmployeeDao;
import com.companyz.ems.dao.TransactionManager;
import com.companyz.ems.model.Demographics;
import com.companyz.ems.model.Employee;
import com.companyz.ems.model.Role;
import com.companyz.ems.model.SalaryRaise;
import com.companyz.ems.model.SearchCriteria;
import com.companyz.ems.model.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * Search rules for features 2 and 3, using three of the shared test employees (sql/05 to 07).
 * The fake DAO imitates the SQL (AND of the given fields, contains-match on names), so these
 * tests check the SERVICE rules, not the SQL itself.
 */
class EmployeeServiceImplTest {

    private static final Session HR = new Session(1, "hradmin", Role.HR_ADMIN, null);
    private static final Session SBEAGLE = new Session(2, "sbeagle", Role.EMPLOYEE, 1);

    private final FakeEmployeeDao dao = new FakeEmployeeDao();
    private final EmployeeServiceImpl service =
            new EmployeeServiceImpl(dao, (userId, action, table, recordId, oldValue, newValue) -> { },
                    new TransactionManager() {
                        @Override
                        public <T> T inTransaction(Supplier<T> work) {
                            return work.get(); // the fake DAO has no database, so just run the work
                        }
                    });

    // ---- Feature 2: HR searches to edit (TC-2.1 to TC-2.3) ----

    @Test
    void hrFindsCharlieBrownByName() {
        List<Employee> found = service.searchForEditing(HR, SearchCriteria.byName("brown"));
        assertEquals(1, found.size());
        assertEquals(2, found.get(0).getEmpId());
        assertEquals("Charlie Brown", found.get(0).getFullName());
    }

    @Test
    void hrFindsLucyDoctorBySsn() {
        List<Employee> found = service.searchForEditing(HR, SearchCriteria.bySsn("111-33-1111"));
        assertEquals(1, found.size());
        assertEquals(3, found.get(0).getEmpId());
    }

    @Test
    void hrSearchForMissingEmpIdReturnsEmptyList() {
        assertTrue(service.searchForEditing(HR, SearchCriteria.byEmpId(9999)).isEmpty());
    }

    @Test
    void hrFieldsAreCombinedWithAnd() {
        SearchCriteria brownBornIn1984 = new SearchCriteria(null, "brown", LocalDate.of(1984, 9, 2), null);
        SearchCriteria brownBornIn2000 = new SearchCriteria(null, "brown", LocalDate.of(2000, 1, 1), null);
        assertEquals(1, service.searchForEditing(HR, brownBornIn1984).size());
        assertTrue(service.searchForEditing(HR, brownBornIn2000).isEmpty());
    }

    @Test
    void employeeCannotUseTheEditingSearch() {
        assertThrows(SecurityException.class,
                () -> service.searchForEditing(SBEAGLE, SearchCriteria.byName("brown")));
        assertEquals(0, dao.searchCalls);
    }

    @Test
    void editingSearchWithNoFieldsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.searchForEditing(HR, new SearchCriteria(null, "  ", null, "")));
        assertThrows(IllegalArgumentException.class, () -> service.searchForEditing(HR, null));
        assertEquals(0, dao.searchCalls);
    }

    // ---- Feature 3: employee views only their own record ----

    @Test
    void employeeSeesOwnRecordWithMaskedSsn() {
        Optional<Employee> found = service.searchForViewing(SBEAGLE, SearchCriteria.byEmpId(1));
        assertTrue(found.isPresent());
        assertEquals("Snoopy Beagle", found.get().getFullName());
        assertEquals("***-**-1111", found.get().getSsn());
        assertEquals(new BigDecimal("45000.00"), found.get().getSalary());
        assertEquals(LocalDate.of(1985, 5, 21), found.get().getDemographics().dateOfBirth());
    }

    @Test
    void maskingDoesNotChangeTheStoredEmployee() {
        service.searchForViewing(SBEAGLE, SearchCriteria.byEmpId(1));
        assertEquals("111-11-1111", dao.findById(1).orElseThrow().getSsn());
    }

    @Test
    void employeeWithNoCriteriaSeesOwnRecord() {
        Optional<Employee> found = service.searchForViewing(SBEAGLE, new SearchCriteria(null, null, null, null));
        assertEquals(1, found.orElseThrow().getEmpId());
    }

    @Test
    void employeeAskingForACoworkerGetsNothingAndNoDatabaseCall() {
        assertTrue(service.searchForViewing(SBEAGLE, SearchCriteria.byEmpId(2)).isEmpty());
        assertEquals(0, dao.searchCalls);
    }

    @Test
    void employeeSearchingACoworkersNameOrSsnGetsNothing() {
        assertTrue(service.searchForViewing(SBEAGLE, SearchCriteria.byName("brown")).isEmpty());
        assertTrue(service.searchForViewing(SBEAGLE, SearchCriteria.bySsn("111-22-1111")).isEmpty());
        // The search was narrowed to her own empID before it reached the DAO
        assertEquals(1, dao.lastCriteria.empId());
    }

    @Test
    void employeeSearchingOwnNameFindsOwnRecord() {
        assertEquals(1, service.searchForViewing(SBEAGLE, SearchCriteria.byName("BEAGLE")).orElseThrow().getEmpId());
    }

    @Test
    void hrCannotUseTheViewingSearch() {
        assertThrows(SecurityException.class, () -> service.searchForViewing(HR, SearchCriteria.byEmpId(1)));
    }

    @Test
    void accountWithoutAnEmployeeRecordCannotView() {
        Session orphan = new Session(3, "orphan", Role.EMPLOYEE, null);
        assertThrows(IllegalStateException.class, () -> service.searchForViewing(orphan, SearchCriteria.byEmpId(1)));
    }

    // ---- Other members' methods are clearly marked as not written ----

    @Test
    void unwrittenMethodsSayWhoOwnsThem() {
        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
                () -> service.deleteEmployee(HR, 6));
        assertTrue(e.getMessage().contains("Member 5"));
    }

    // ---- Stand-in DAO with three of the seed employees ----

    private static final class FakeEmployeeDao implements EmployeeDao {
        int searchCalls;
        SearchCriteria lastCriteria;

        private final List<Employee> all = List.of(
                employee(1, "Snoopy", "Beagle", "45000.00", "111-11-1111", LocalDate.of(1985, 5, 21)),
                employee(2, "Charlie", "Brown", "48000.00", "111-22-1111", LocalDate.of(1984, 9, 2)),
                employee(3, "Lucy", "Doctor", "55000.00", "111-33-1111", LocalDate.of(1983, 12, 15)));

        @Override
        public Optional<Employee> findById(int empId) {
            return all.stream().filter(e -> e.getEmpId() == empId).findFirst();
        }

        @Override
        public List<Employee> search(SearchCriteria c) {
            searchCalls++;
            lastCriteria = c;
            List<Employee> result = new ArrayList<>();
            for (Employee e : all) {
                boolean ok = (c.empId() == null || c.empId().equals(e.getEmpId()))
                        && (c.name() == null || c.name().isBlank() || nameContains(e, c.name()))
                        && (c.dateOfBirth() == null || c.dateOfBirth().equals(e.getDemographics().dateOfBirth()))
                        && (c.ssn() == null || c.ssn().isBlank() || c.ssn().equals(e.getSsn()));
                if (ok) {
                    result.add(e);
                }
            }
            return result;
        }

        private static boolean nameContains(Employee e, String typed) {
            String t = typed.trim().toLowerCase(Locale.ROOT);
            return e.getFullName().toLowerCase(Locale.ROOT).contains(t);
        }

        @Override public int insert(Employee employee) { throw new UnsupportedOperationException(); }
        @Override public void update(Employee employee) { throw new UnsupportedOperationException(); }
        @Override public boolean delete(int empId) { throw new UnsupportedOperationException(); }
        @Override public List<Employee> findAffectedBy(SalaryRaise raise) { throw new UnsupportedOperationException(); }
        @Override public int applyRaise(SalaryRaise raise) { throw new UnsupportedOperationException(); }
    }

    private static Employee employee(int id, String first, String last, String salary, String ssn, LocalDate dob) {
        Employee e = new Employee();
        e.setEmpId(id);
        e.setFirstName(first);
        e.setLastName(last);
        e.setSalary(new BigDecimal(salary));
        e.setSsn(ssn);
        e.setDemographics(new Demographics(dob, "404-555-0100", "Contact", "404-555-0200"));
        return e;
    }
}
