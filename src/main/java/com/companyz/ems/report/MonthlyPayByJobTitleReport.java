package com.companyz.ems.report;

import com.companyz.ems.dao.ReportDao;
import com.companyz.ems.model.PayTotal;
import java.time.YearMonth;
import java.util.List;

/** Report 6b: total pay for a month by job title. HR admin only. */
public class MonthlyPayByJobTitleReport extends MonthlyPayTotalReport {

    private final ReportDao reportDao;

    public MonthlyPayByJobTitleReport(ReportDao reportDao) {
        super("6b", "Total Pay for Month by Job Title");
        this.reportDao = reportDao;
    }

    @Override
    protected String groupColumn() {
        return "Job Title";
    }

    @Override
    protected List<PayTotal> fetchTotals(YearMonth month) {
        return reportDao.totalPayByJobTitle(month);
    }
}
