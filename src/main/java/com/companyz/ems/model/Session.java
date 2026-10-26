package com.companyz.ems.model;

/**
 * The signed-in user (feature 1). Every service and report call takes a Session,
 * so permission checks can't be skipped by the UI.
 *
 * @param empId the user's own employee record; null for an HR account with no employee row
 */
public record Session(int userId, String username, Role role, Integer empId) {

    public boolean isHrAdmin() {
        return role == Role.HR_ADMIN;
    }

    /** Throws if this user doesn't have the given role. */
    public void requireRole(Role required) {
        if (role != required) {
            throw new SecurityException(username + " (" + role + ") is not allowed to do this; requires " + required);
        }
    }
}
