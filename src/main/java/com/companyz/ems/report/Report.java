package com.companyz.ems.report;

import com.companyz.ems.model.Role;
import com.companyz.ems.model.Session;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Base class for every report in feature 6. Each report is one subclass that
 * supplies its title, which roles may run it, its columns and its rows.
 * The role check lives here, once, so no report can forget it.
 *
 * <pre>
 *   6a PayHistoryReport           EMPLOYEE              own pay statements, newest first
 *   6b MonthlyPayByJobTitleReport HR_ADMIN              total pay for a month by job title
 *   6c MonthlyPayByDivisionReport HR_ADMIN              total pay for a month by division
 *   6d NewHiresReport             HR_ADMIN, EMPLOYEE    hires within a date range
 *   6e EmployeesByJobTitleReport  HR_ADMIN              all employees grouped by job title
 * </pre>
 *
 * @param <P> the report's input (a month, a date range), or {@link Void} if none
 */
public abstract class Report<P> {

    private final String id;
    private final String title;
    private final Set<Role> allowedRoles;

    protected Report(String id, String title, Set<Role> allowedRoles) {
        this.id = id;
        this.title = title;
        this.allowedRoles = Set.copyOf(allowedRoles);
    }

    /** User story reference, e.g. "6b". */
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAllowedFor(Role role) {
        return allowedRoles.contains(role);
    }

    /** Column headings, in display order. */
    public abstract List<String> columns();

    /** Builds the rows; each row has one value per column. Called only after the role check. */
    protected abstract List<List<Object>> generate(Session session, P parameters);

    /** Runs the report for the signed-in user. */
    public final List<List<Object>> run(Session session, P parameters) {
        requireAccess(session);
        return generate(session, parameters);
    }

    /** Throws SecurityException unless this user's role may run this report. */
    protected final void requireAccess(Session session) {
        Objects.requireNonNull(session, "session");
        if (!isAllowedFor(session.role())) {
            throw new SecurityException(session.role() + " may not run report " + id + ": " + title);
        }
    }
}
