package com.companyz.ems.dao;

import com.companyz.ems.model.DateRange;
import com.companyz.ems.model.Employee;
import com.companyz.ems.model.PayTotal;
import java.time.YearMonth;
import java.util.List;

/**
 * Queries behind the feature 6 reports. Report 6a (pay history) uses {@link PayrollDao}.
 * "Total pay" means the sum of payroll.earnings (gross) for pay dates in that calendar month.
 */
public interface ReportDao {

    /** Report 6b, ordered by job title. */
    List<PayTotal> totalPayByJobTitle(YearMonth month);

    /** Report 6c, ordered by division name. */
    List<PayTotal> totalPayByDivision(YearMonth month);

    /** Report 6d: employees hired within the range (inclusive), ordered by hire date. */
    List<Employee> findHiredBetween(DateRange range);

    /** Report 6e: every employee with job title and division filled in. */
    List<Employee> findAllWithJobAndDivision();
}
