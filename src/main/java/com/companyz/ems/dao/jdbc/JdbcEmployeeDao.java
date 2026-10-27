package com.companyz.ems.dao.jdbc;

import com.companyz.ems.config.ConnectionFactory;
import com.companyz.ems.config.DatabaseConfig.Account;
import com.companyz.ems.dao.DataAccessException;
import com.companyz.ems.dao.EmployeeDao;
import com.companyz.ems.model.Address;
import com.companyz.ems.model.Demographics;
import com.companyz.ems.model.Employee;
import com.companyz.ems.model.SalaryRaise;
import com.companyz.ems.model.SearchCriteria;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * MySQL implementation of {@link EmployeeDao}.
 *
 * <p><b>Which database account?</b> Build one instance per signed-in role:
 * {@code new JdbcEmployeeDao(connections, DatabaseConfig.accountFor(session.role()))}.
 * An HR session gets the {@code ems_hr} account; an employee session gets the read-only
 * {@code ems_employee} account, so a bug in the employee screens can never change data.
 *
 * <p><b>Rules for everyone adding methods to this class</b> (Members 4, 5, 6):
 * PreparedStatement only, try-with-resources on every Connection / Statement / ResultSet,
 * wrap {@link SQLException} in {@link DataAccessException}, and use one transaction
 * ({@code setAutoCommit(false)} ... {@code commit()} / {@code rollback()}) for any change that
 * touches more than one table. Reuse {@link #SELECT_EMPLOYEES} and {@link #mapEmployee} to
 * load full {@link Employee} objects.
 */
public class JdbcEmployeeDao implements EmployeeDao {

    /**
     * Every column of a full Employee, one row per employee (every join is 1-to-1).
     * LEFT JOINs so an employee with a missing address or demographics row still appears.
     * Add WHERE / ORDER BY after this.
     */
    static final String SELECT_EMPLOYEES = """
            SELECT e.empID, e.Fname, e.Lname, e.email, e.HireDate, e.Salary, e.SSN,
                   d.DOB, d.mobile_phone, d.emergency_contact_name, d.emergency_contact_phone,
                   a.street, c.city_name, s.abbreviation, a.zip,
                   jt.job_title, dv.Name AS division_name
            FROM employees e
            LEFT JOIN demographics        d   ON d.empID = e.empID
            LEFT JOIN addresses           a   ON a.empID = e.empID
            LEFT JOIN cities              c   ON c.cityID = a.cityID
            LEFT JOIN states              s   ON s.stateID = a.stateID
            LEFT JOIN employee_job_titles ejt ON ejt.empID = e.empID
            LEFT JOIN job_titles          jt  ON jt.job_titleID = ejt.job_titleID
            LEFT JOIN employee_division   ed  ON ed.empID = e.empID
            LEFT JOIN division            dv  ON dv.divID = ed.divID
            """;

    private final ConnectionFactory connections;
    private final Account account;

    public JdbcEmployeeDao(ConnectionFactory connections, Account account) {
        this.connections = connections;
        this.account = account;
    }

    // ------------------------------------------------------------------
    // Member 2: feature 2 and 3 (search)
    // ------------------------------------------------------------------

    @Override
    public Optional<Employee> findById(int empId) {
        return search(SearchCriteria.byEmpId(empId)).stream().findFirst();
    }

    @Override
    public List<Employee> search(SearchCriteria criteria) {
        if (criteria == null || criteria.isEmpty()) {
            // Never return the whole table (with SSNs) just because nothing was typed.
            throw new IllegalArgumentException("Give at least one search value");
        }
        List<Object> params = new ArrayList<>();
        String sql = buildSearchSql(criteria, params);

        try (Connection conn = connections.open(account);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bind(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Employee> found = new ArrayList<>();
                while (rs.next()) {
                    found.add(mapEmployee(rs));
                }
                return found;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not search employees", e);
        }
    }

    /**
     * Builds the search SQL. Only fixed text goes into the SQL string; every value the user
     * typed is added to {@code params} and bound as a ? placeholder, so SQL injection is
     * impossible. Fields that were given are combined with AND.
     *
     * @param params receives the values for the ? placeholders, in order
     */
    static String buildSearchSql(SearchCriteria criteria, List<Object> params) {
        List<String> conditions = new ArrayList<>();

        if (criteria.empId() != null) {
            conditions.add("e.empID = ?");
            params.add(criteria.empId());
        }
        if (hasText(criteria.name())) {
            // Contains-match, case-insensitive, on first name, last name, or "first last"
            conditions.add("(LOWER(e.Fname) LIKE ? OR LOWER(e.Lname) LIKE ?"
                    + " OR LOWER(CONCAT(e.Fname, ' ', e.Lname)) LIKE ?)");
            String pattern = "%" + escapeLike(criteria.name().trim().toLowerCase(Locale.ROOT)) + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (criteria.dateOfBirth() != null) {
            conditions.add("d.DOB = ?");
            params.add(criteria.dateOfBirth());
        }
        if (hasText(criteria.ssn())) {
            conditions.add("e.SSN = ?");
            params.add(normalizeSsn(criteria.ssn()));
        }
        return SELECT_EMPLOYEES + "WHERE " + String.join(" AND ", conditions)
                + "\nORDER BY e.Lname, e.Fname, e.empID";
    }

    /** "123456789" and " 123 45 6789 " both become "123-45-6789", the format stored in the database. */
    static String normalizeSsn(String ssn) {
        String digits = ssn.replaceAll("\\D", "");
        if (digits.length() == 9) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 5) + "-" + digits.substring(5);
        }
        return ssn.trim(); // not a full SSN: will simply match nobody
    }

    /** Makes %, _ and \ in what the user typed match literally inside a LIKE pattern. */
    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** Turns the current row of a {@link #SELECT_EMPLOYEES} query into an Employee. */
    static Employee mapEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmpId(rs.getInt("empID"));
        e.setFirstName(rs.getString("Fname"));
        e.setLastName(rs.getString("Lname"));
        e.setEmail(rs.getString("email"));
        Date hired = rs.getDate("HireDate"); // nullable in the professor's schema
        e.setHireDate(hired == null ? null : hired.toLocalDate());
        e.setSalary(rs.getBigDecimal("Salary"));
        e.setSsn(rs.getString("SSN"));
        e.setJobTitle(rs.getString("job_title"));
        e.setDivision(rs.getString("division_name"));

        String street = rs.getString("street");
        if (street != null) {
            e.setAddress(new Address(street, rs.getString("city_name"),
                    rs.getString("abbreviation"), rs.getString("zip")));
        }
        Date dob = rs.getDate("DOB");
        if (dob != null) {
            e.setDemographics(new Demographics(dob.toLocalDate(), rs.getString("mobile_phone"),
                    rs.getString("emergency_contact_name"), rs.getString("emergency_contact_phone")));
        }
        return e;
    }

    private static void bind(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            int index = i + 1;
            if (value instanceof Integer n) {
                stmt.setInt(index, n);
            } else if (value instanceof LocalDate date) {
                stmt.setDate(index, Date.valueOf(date));
            } else {
                stmt.setString(index, (String) value);
            }
        }
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    // ------------------------------------------------------------------
    // Not written yet. Each owner replaces their stub(s) below.
    // ------------------------------------------------------------------

    /** TODO Member 4 (PT-9). */
    @Override
    public int insert(Employee employee) {
        throw todo("Member 4");
    }

    /** TODO Member 4 (PT-4). */
    @Override
    public void update(Employee employee) {
        throw todo("Member 4");
    }

    /** TODO Member 5 (PT-5). */
    @Override
    public boolean delete(int empId) {
        throw todo("Member 5");
    }

    /** TODO Member 6 (PT-10). */
    @Override
    public List<Employee> findAffectedBy(SalaryRaise raise) {
        throw todo("Member 6");
    }

    /** TODO Member 6 (PT-6). */
    @Override
    public int applyRaise(SalaryRaise raise) {
        throw todo("Member 6");
    }

    private static UnsupportedOperationException todo(String owner) {
        return new UnsupportedOperationException("TODO: not written yet (" + owner + ")");
    }
}
