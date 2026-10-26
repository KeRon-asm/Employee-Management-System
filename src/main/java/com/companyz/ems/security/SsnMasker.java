package com.companyz.ems.security;

/** Hides all but the last four digits of an SSN for display, e.g. {@code ***-**-6789}. */
public final class SsnMasker {

    private SsnMasker() {
    }

    public static String mask(String ssn) {
        if (ssn == null) {
            return "";
        }
        String digits = ssn.replaceAll("\\D", "");
        if (digits.length() < 4) {
            return "***-**-****";
        }
        return "***-**-" + digits.substring(digits.length() - 4);
    }
}
