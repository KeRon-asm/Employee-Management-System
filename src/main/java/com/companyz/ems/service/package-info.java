/**
 * Business rules between the UI and the DAOs: login and lockout ({@link com.companyz.ems.service.AuthService}),
 * role checks, input validation and audit logging ({@link com.companyz.ems.service.EmployeeService}).
 * The UI calls services; services call DAOs. The UI never talks to a DAO directly.
 * Reports (feature 6) live in {@code com.companyz.ems.report} and check roles themselves.
 */
package com.companyz.ems.service;
