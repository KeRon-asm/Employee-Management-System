/**
 * Business rules between the UI and the DAOs: login and lockout, role checks
 * (an EMPLOYEE may only see their own records), input validation, and audit logging.
 * The UI calls services; services call DAOs. The UI never talks to a DAO directly.
 */
package com.companyz.ems.service;
