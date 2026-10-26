package com.companyz.ems.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * One employee, assembled from the employees, addresses, demographics,
 * job title and division tables. Mutable so the HR edit screen can bind to it.
 *
 * <p>Money is always {@link BigDecimal}, never double, so raises and
 * payroll totals never pick up rounding errors.
 */
public class Employee {

    private Integer empId;           // null until inserted (auto-generated)
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String ssn;
    private String jobTitle;
    private String division;
    private Address address;
    private Demographics demographics;

    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public Demographics getDemographics() { return demographics; }
    public void setDemographics(Demographics demographics) { this.demographics = demographics; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Employee other && empId != null && empId.equals(other.empId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(empId);
    }

    /** Never prints the SSN. */
    @Override
    public String toString() {
        return "Employee{empId=" + empId + ", name=" + getFullName() + "}";
    }
}
