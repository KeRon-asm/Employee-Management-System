package com.companyz.ems.report;

import com.companyz.ems.model.Role;
import java.util.List;
import java.util.Set;

/**
 * Base class for every report in feature 6. Each report (6a-6e) is one
 * subclass that supplies its title, who may run it, and its rows.
 *
 * @param <P> the report's input parameters (e.g. a month, a date range)
 */
public abstract class Report<P> {

    private final String title;
    private final Set<Role> allowedRoles;

    protected Report(String title, Set<Role> allowedRoles) {
        this.title = title;
        this.allowedRoles = Set.copyOf(allowedRoles);
    }

    public String getTitle() {
        return title;
    }

    public boolean isAllowedFor(Role role) {
        return allowedRoles.contains(role);
    }

    /** Column headings, in display order. */
    public abstract List<String> columns();

    /** Report rows; each row has one value per column. */
    protected abstract List<List<Object>> generate(P parameters);

    /** Runs the report after checking the caller's role. */
    public final List<List<Object>> run(Role callerRole, P parameters) {
        if (!isAllowedFor(callerRole)) {
            throw new SecurityException(callerRole + " may not run report: " + title);
        }
        return generate(parameters);
    }
}
