package com.companyz.ems.service;

import com.companyz.ems.dao.AuditDao;
import com.companyz.ems.dao.EmployeeDao;
import com.companyz.ems.model.Employee;
import com.companyz.ems.model.Role;
import com.companyz.ems.model.SalaryRaise;
import com.companyz.ems.model.SearchCriteria;
import com.companyz.ems.model.Session;
import com.companyz.ems.security.SsnMasker;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link EmployeeService}. Every method checks the caller's role first,
 * then calls the DAO.
 *
 * <p>Build one per signed-in user, passing a DAO created for that user's database account
 * (see {@code JdbcEmployeeDao}). Members 4, 5 and 6 each fill in their own methods below;
 * they use {@code auditDao} to record every insert, update and delete.
 */
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeDao employeeDao;
    private final AuditDao auditDao; // used by the update/add/delete/raise methods (Members 4-6)

    public EmployeeServiceImpl(EmployeeDao employeeDao, AuditDao auditDao) {
        this.employeeDao = Objects.requireNonNull(employeeDao, "employeeDao");
        this.auditDao = Objects.requireNonNull(auditDao, "auditDao");
    }

    // ------------------------------------------------------------------
    // Member 2: features 2 and 3 (search)
    // ------------------------------------------------------------------

    /** Feature 2: an HR admin searches everyone. At least one field is required. */
    @Override
    public List<Employee> searchForEditing(Session session, SearchCriteria criteria) {
        requireRole(session, Role.HR_ADMIN);
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalArgumentException(
                    "Enter at least one search value: name, date of birth, SSN or employee ID");
        }
        return employeeDao.search(criteria);
    }

    /**
     * Feature 3: an employee views their OWN record only. Whatever was typed, the search is
     * forced to the signed-in employee's own empID, so it can never reveal anyone else
     * (not even whether a co-worker's SSN exists). Empty criteria simply show their own record.
     * The returned copy has the SSN masked.
     */
    @Override
    public Optional<Employee> searchForViewing(Session session, SearchCriteria criteria) {
        requireRole(session, Role.EMPLOYEE);
        if (session.empId() == null) {
            throw new IllegalStateException(session.username() + " has no employee record");
        }
        int ownId = session.empId();
        SearchCriteria typed = criteria == null ? new SearchCriteria(null, null, null, null) : criteria;

        if (typed.empId() != null && typed.empId() != ownId) {
            return Optional.empty(); // asked for someone else: no database call at all
        }
        SearchCriteria ownOnly = new SearchCriteria(ownId, typed.name(), typed.dateOfBirth(), typed.ssn());
        return employeeDao.search(ownOnly).stream().findFirst().map(EmployeeServiceImpl::withMaskedSsn);
    }

    private static void requireRole(Session session, Role role) {
        Objects.requireNonNull(session, "session");
        session.requireRole(role);
    }

    private static Employee withMaskedSsn(Employee original) {
        Employee copy = new Employee();
        copy.setEmpId(original.getEmpId());
        copy.setFirstName(original.getFirstName());
        copy.setLastName(original.getLastName());
        copy.setEmail(original.getEmail());
        copy.setHireDate(original.getHireDate());
        copy.setSalary(original.getSalary());
        copy.setSsn(SsnMasker.mask(original.getSsn()));
        copy.setJobTitle(original.getJobTitle());
        copy.setDivision(original.getDivision());
        copy.setAddress(original.getAddress());           // immutable records: safe to share
        copy.setDemographics(original.getDemographics());
        return copy;
    }

    // ------------------------------------------------------------------
    // Not written yet. Each owner replaces their stub(s) below.
    // ------------------------------------------------------------------

    /** TODO Member 4 (PT-9). */
    @Override
    public int addEmployee(Session session, Employee employee) {
        throw todo("Member 4");
    }

    /** TODO Member 4 (PT-4). */
    @Override
    public void updateEmployee(Session session, Employee employee) {
        throw todo("Member 4");
    }

    /** TODO Member 5 (PT-5). */
    @Override
    public Optional<Employee> findForDeletion(Session session, int empId) {
        throw todo("Member 5");
    }

    /** TODO Member 5 (PT-5). */
    @Override
    public boolean deleteEmployee(Session session, int empId) {
        throw todo("Member 5");
    }

    /** TODO Member 6 (PT-10). */
    @Override
    public List<Employee> previewRaise(Session session, SalaryRaise raise) {
        throw todo("Member 6");
    }

    /** TODO Member 6 (PT-6). */
    @Override
    public int applyRaise(Session session, SalaryRaise raise) {
        throw todo("Member 6");
    }

    private static UnsupportedOperationException todo(String owner) {
        return new UnsupportedOperationException("TODO: not written yet (" + owner + ")");
    }
}
