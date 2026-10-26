package com.companyz.ems.dao;

import com.companyz.ems.model.PayStatement;
import java.util.List;

/** Data access for the payroll table. */
@FunctionalInterface
public interface PayrollDao {

    /** Report 6a: one employee's pay statements, most recent pay date first. */
    List<PayStatement> findByEmployeeNewestFirst(int empId);
}
