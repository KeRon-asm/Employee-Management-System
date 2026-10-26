package com.companyz.ems.report;

import com.companyz.ems.dao.ReportDao;
import com.companyz.ems.model.PayTotal;
import java.time.YearMonth;
import java.util.List;

/** Report 6c: total pay for a month by division. HR admin only. */
public class MonthlyPayByDivisionReport extends MonthlyPayTotalReport {

    private final ReportDao reportDao;

    public MonthlyPayByDivisionReport(ReportDao reportDao) {
        super("6c", "Total Pay for Month by Division");
        this.reportDao = reportDao;
    }

    @Override
    protected String groupColumn() {
        return "Division";
    }

    @Override
    protected List<PayTotal> fetchTotals(YearMonth month) {
        return reportDao.totalPayByDivision(month);
    }
}
