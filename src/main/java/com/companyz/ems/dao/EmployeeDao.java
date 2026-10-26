package com.companyz.ems.dao;

import com.companyz.ems.model.Employee;
import com.companyz.ems.model.SalaryRaise;
import com.companyz.ems.model.SearchCriteria;
import java.util.List;
import java.util.Optional;

/**
 * Data access for employees and their address, demographics, job title and division rows.
 * The UI and services depend on this interface, never on JDBC directly.
 *
 * <p>Implementations live in {@code com.companyz.ems.dao.jdbc}. They must use
 * PreparedStatement for every value and run multi-table changes in one transaction.
 * Role checks are NOT done here; they belong in the service layer.
 */
public interface EmployeeDao {

    Optional<Employee> findById(int empId);

    /** Features 2 and 3: search by any combination of empID, name, DOB, SSN. */
    List<Employee> search(SearchCriteria criteria);

    /**
     * Inserts the employee plus its address, demographics, job title and division rows
     * in one transaction (deliverable 3b).
     *
     * @return the auto-generated empID
     */
    int insert(Employee employee);

    /** Feature 4 / task (a): updates every field of an existing employee, all tables, one transaction. */
    void update(Employee employee);

    /** Task (b): deletes the employee and dependent rows. Returns false if no such empID. */
    boolean delete(int empId);

    /** Feature 5: the employees a raise would affect, for the preview screen. */
    List<Employee> findAffectedBy(SalaryRaise raise);

    /**
     * Feature 5 / task (c): applies the raise to every employee it covers, in one transaction.
     * Suggested SQL:
     * {@code UPDATE employees SET Salary = ROUND(Salary * (1 + ? / 100), 2)
     *        WHERE Salary >= ? AND Salary <op> ?}
     * where {@code <op>} is {@link SalaryRaise#upperBoundOperator()}.
     *
     * @return number of employees updated (0 is valid -- nobody was in range)
     */
    int applyRaise(SalaryRaise raise);
}
