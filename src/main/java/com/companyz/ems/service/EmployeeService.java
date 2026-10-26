package com.companyz.ems.service;

import com.companyz.ems.model.Employee;
import com.companyz.ems.model.SalaryRaise;
import com.companyz.ems.model.SearchCriteria;
import com.companyz.ems.model.Session;
import java.util.List;
import java.util.Optional;

/**
 * Features 1-5: everything the UI can do with employee records, with the role rules
 * from the user story enforced here. Every method that changes data writes the audit log.
 * Methods marked HR_ADMIN throw SecurityException for any other role.
 */
public interface EmployeeService {

    /** Feature 2 (HR_ADMIN): search any employee by name, DOB, SSN or empID, for editing. */
    List<Employee> searchForEditing(Session session, SearchCriteria criteria);

    /**
     * Feature 3 (EMPLOYEE): search by name, DOB, SSN or empID, for viewing only.
     * A general employee can see only their OWN pay and demographic data (feature 1b),
     * so this returns their record when the criteria match it, and empty otherwise
     * ("no matching employee"). The SSN is masked on screen.
     */
    Optional<Employee> searchForViewing(Session session, SearchCriteria criteria);

    /** Feature 1a (HR_ADMIN): add a new employee. The empID is auto-generated and returned. */
    int addEmployee(Session session, Employee employee);

    /** Feature 4 / task (a) (HR_ADMIN): save changes to an employee picked from search results. */
    void updateEmployee(Session session, Employee employee);

    /** Task (b) step 1 (HR_ADMIN): look up an empID before asking "Delete this employee?". */
    Optional<Employee> findForDeletion(Session session, int empId);

    /** Task (b) step 2 (HR_ADMIN): delete after the user confirms. False if no match. */
    boolean deleteEmployee(Session session, int empId);

    /** Feature 5 (HR_ADMIN): who a raise would affect, with old and new salaries, before applying. */
    List<Employee> previewRaise(Session session, SalaryRaise raise);

    /** Feature 5 / task (c) (HR_ADMIN): apply the raise in one transaction. Returns how many were updated. */
    int applyRaise(Session session, SalaryRaise raise);
}
