package com.companyz.ems.model;

/** Security levels from the user story. Matches {@code app_users.role}. */
public enum Role {
    /** Full CRUD on the entire database. */
    HR_ADMIN,
    /** Can view only their own pay and demographic data. */
    EMPLOYEE
}
