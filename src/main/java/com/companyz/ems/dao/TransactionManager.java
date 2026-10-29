package com.companyz.ems.dao;

import java.util.function.Supplier;

/**
 * Runs several DAO calls as one transaction: they are all saved together, or none are.
 * Services use this so a change and its {@code audit_log} row can never be split.
 *
 * <pre>{@code
 * return transactions.inTransaction(() -> {
 *     auditDao.record(...);              // audit row first
 *     return employeeDao.delete(empId);  // then the change
 * });
 * }</pre>
 *
 * <ul>
 *   <li>The block returns normally: everything is committed.</li>
 *   <li>The block throws: everything is rolled back and the same exception is rethrown.</li>
 *   <li>Called inside another transaction: joins it; only the outermost block commits.</li>
 * </ul>
 *
 * See {@code docs/design/transactions.md} for the full rules.
 */
public interface TransactionManager {

    /** Runs {@code work} in one transaction and returns its result. */
    <T> T inTransaction(Supplier<T> work);

    /** Same as {@link #inTransaction(Supplier)} for work that returns nothing. */
    default void runInTransaction(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }
}
