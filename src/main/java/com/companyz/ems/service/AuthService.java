package com.companyz.ems.service;

import com.companyz.ems.model.Session;

/**
 * Feature 1: user logon and authorization.
 *
 * <p>Expected behavior for the implementation:
 * <ul>
 *   <li>Look the user up with UserDao (ems_auth account) and check the password with PasswordHasher.</li>
 *   <li>After {@link #MAX_FAILED_ATTEMPTS} wrong passwords, lock the account for {@link #LOCKOUT_MINUTES}.</li>
 *   <li>Record LOGIN and LOGIN_FAILED in the audit log.</li>
 *   <li>Give the same message for "no such user" and "wrong password", so usernames can't be guessed.</li>
 * </ul>
 */
public interface AuthService {

    int MAX_FAILED_ATTEMPTS = 5;
    int LOCKOUT_MINUTES = 15;

    /**
     * @return the session for the signed-in user
     * @throws AuthenticationException if the username/password is wrong or the account is locked
     */
    Session login(String username, char[] password);
}
