
/* 
    THESE ARE FOR dBeaver usage
    VS Code usage may give you unpredictable results. 	
*/


/***********************************************************************/
USE employeeData;

SET sql_require_primary_key=0;

CREATE TABLE employees (
  empID INT NOT NULL,
  Fname VARCHAR(65) NOT NULL,
  Lname VARCHAR(65) NOT NULL,
  email VARCHAR(65) NOT NULL,
  HireDate DATE,
  Salary DECIMAL(10,2) NOT NULL,
  SSN VARCHAR(12)
);
/***********************************************************************/

CREATE TABLE payroll (
  payID INT,
  pay_date DATE,
  earnings DECIMAL(8,2),
  fed_tax DECIMAL(7,2),
  fed_med DECIMAL(7,2),
  fed_SS DECIMAL(7,2),
  state_tax DECIMAL(7,2),
  retire_401k DECIMAL(7,2),
  health_care DECIMAL(7,2),
  empID INT
);

/***********************************************************************/ 

CREATE TABLE employee_job_titles (
  empID INT NOT NULL,
  job_titleID INT NOT NULL
);

/***********************************************************************/ 

CREATE TABLE job_titles (
  job_titleID INT,
  job_title VARCHAR(125) NOT NULL
);

/***********************************************************************/ 

CREATE TABLE division (
  divID int NOT NULL,
  Name varchar(100) DEFAULT NULL,
  city varchar(50) NOT NULL,
  addressLine1 varchar(50) NOT NULL,
  addressLine2 varchar(50) DEFAULT NULL,
  state varchar(50) DEFAULT NULL,
  country varchar(50) NOT NULL,
  postalCode varchar(15) NOT NULL
);

/***********************************************************************/ 

-- employee_division definition

CREATE TABLE employee_division (
  empid int NOT NULL,
  divID int NOT NULL
);

