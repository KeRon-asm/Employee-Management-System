package com.companyz.ems.dao;

import com.companyz.ems.model.PayStatement;
import java.util.List;

/** Data access for the payroll table. */
public interface PayrollDao {

    /** Report 6a: an employee's pay history, most recent pay date first. */
    List<PayStatement> findByEmployeeNewestFirst(int empId);
}
