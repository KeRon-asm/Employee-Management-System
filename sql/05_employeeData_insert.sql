

SET FOREIGN_KEY_CHECKS=0;

INSERT INTO employees (empID, Fname, Lname, email, HireDate, Salary, SSN)
VALUES 
(1,'Snoopy', 'Beagle', 	'Snoopy@example.com', 	'2022-08-01', 45000.00, '111-11-1111'),
(2,'Charlie', 'Brown', 	'Charlie@example.com', 	'2022-07-01', 48000.00, '111-22-1111'),
(3,'Lucy', 'Doctor', 		'Lucy@example.com', 	'2022-07-03', 55000.00, '111-33-1111'),
(4,'Pepermint', 'Patti', 	'Peppermint@example.com', '2022-08-02', 98000.00, '111-44-1111'),
(5,'Linus', 'Blanket', 	'Linus@example.com', 	'2022-09-01', 43000.00, '111-55-1111'),
(6,'PigPin', 'Dusty', 	'PigPin@example.com', 	'2022-10-01', 33000.00, '111-66-1111')
;

INSERT INTO job_titles
VALUES 
(100,'software manager'),
(101,'software architect'),
(102,'software engineer'),
(103,'software developer'),
(200,'marketing manager'),
(201,'marketing associate'),
(202,'marketing assistant'),
(900,'Chief Exec. Officer'),
(901,'Chief Finn. Officer'),
(902,'Chief Info. Officer')
;
			
INSERT INTO employee_job_titles 
VALUES
(1,902),
(2,900),
(3,901),
(4,200),
(5,101),
(6,201)
;

INSERT INTO division 
VALUES 
(1,'Systems','Atlanta','1 Peachtree Road','','GA','USA','30303'),
(2,'Marketing','Atlanta','1 Oaktree Road','','GA','USA','30303'),
(999,'C-Suite','Atlanta','1 Oaktree Road','','GA','USA','30303'),
(0,'HR','Atlanta','1 Oaktree Road','','GA','USA','30303')
;

INSERT INTO employee_division 
VALUES
(1,999),
(2,1),
(3,999),
(4,2),
(5,1),
(6,2)
;

