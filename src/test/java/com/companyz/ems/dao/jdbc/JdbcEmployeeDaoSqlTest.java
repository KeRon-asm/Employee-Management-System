package com.companyz.ems.dao.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.companyz.ems.model.SearchCriteria;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Checks the SQL text and parameters the search builds. No database needed. */
class JdbcEmployeeDaoSqlTest {

    @Test
    void ssnWithOrWithoutDashesBecomesTheStoredFormat() {
        assertEquals("111-22-1111", JdbcEmployeeDao.normalizeSsn("111-22-1111"));
        assertEquals("111-22-1111", JdbcEmployeeDao.normalizeSsn("111221111"));
        assertEquals("111-22-1111", JdbcEmployeeDao.normalizeSsn("  111 22 1111 "));
        assertEquals("1234", JdbcEmployeeDao.normalizeSsn("1234")); // partial: matches nobody
    }

    @Test
    void likeWildcardsTypedByTheUserAreMatchedLiterally() {
        assertEquals("50\\%\\_off", JdbcEmployeeDao.escapeLike("50%_off"));
        assertEquals("a\\\\b", JdbcEmployeeDao.escapeLike("a\\b"));
    }

    @Test
    void nameSearchBindsThreePatterns() {
        List<Object> params = new ArrayList<>();
        String sql = JdbcEmployeeDao.buildSearchSql(SearchCriteria.byName("Brown"), params);
        assertEquals(List.of("%brown%", "%brown%", "%brown%"), params);
        assertTrue(sql.contains("LOWER(e.Fname) LIKE ?"));
        assertTrue(sql.contains("ORDER BY e.Lname, e.Fname, e.empID"));
    }

    @Test
    void everyGivenFieldIsAddedWithAnd() {
        List<Object> params = new ArrayList<>();
        SearchCriteria all = new SearchCriteria(2, "brown", LocalDate.of(1984, 9, 2), "111221111");
        String sql = JdbcEmployeeDao.buildSearchSql(all, params);

        assertTrue(sql.contains("e.empID = ?"));
        assertTrue(sql.contains("d.DOB = ?"));
        assertTrue(sql.contains("e.SSN = ?"));
        assertEquals(2, params.get(0));
        assertEquals(LocalDate.of(1984, 9, 2), params.get(4));
        assertEquals("111-22-1111", params.get(5));
        assertEquals(6, params.size());
    }

    @Test
    void whatTheUserTypesNeverEntersTheSqlText() {
        String attack = "x'; DROP TABLE employees; --";
        List<Object> params = new ArrayList<>();
        String sql = JdbcEmployeeDao.buildSearchSql(SearchCriteria.byName(attack), params);
        assertFalse(sql.contains("DROP"));
        assertFalse(sql.contains(attack));
        assertTrue(params.get(0).toString().contains("drop table"));
    }

    @Test
    void queryUsesTheProfessorsColumnNames() {
        String sql = JdbcEmployeeDao.SELECT_EMPLOYEES;
        assertTrue(sql.contains("jt.job_titleID = ejt.job_titleID"));
        assertTrue(sql.contains("dv.divID = ed.divID"));
        assertFalse(sql.contains("job_title_id"));
        assertFalse(sql.contains("div_ID"));
    }

    @Test
    void blankFieldsAreIgnored() {
        List<Object> params = new ArrayList<>();
        String sql = JdbcEmployeeDao.buildSearchSql(new SearchCriteria(1, "   ", null, ""), params);
        assertEquals(List.of(1), params);
        assertFalse(sql.contains("LIKE"));
        assertFalse(sql.contains("SSN = ?"));
    }

    @Test
    void searchWithNothingGivenIsRefusedBeforeAnyConnectionIsOpened() {
        JdbcEmployeeDao dao = new JdbcEmployeeDao(null, null); // would fail if it tried to connect
        assertThrows(IllegalArgumentException.class,
                () -> dao.search(new SearchCriteria(null, null, null, null)));
    }
}
