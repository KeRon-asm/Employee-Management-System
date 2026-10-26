package com.companyz.ems.dao;

/** Writes to {@code audit_log}. Every insert, update and delete in the app should call this. */
public interface AuditDao {

    enum Action { INSERT, UPDATE, DELETE, LOGIN, LOGIN_FAILED }

    void record(Integer userId, Action action, String tableName, String recordId,
                String oldValue, String newValue);
}
