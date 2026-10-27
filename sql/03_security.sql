-- =====================================================================
-- 03_security.sql -- application login accounts and the audit trail
--
-- app_users: one login per person. Passwords are stored ONLY as salted
--   PBKDF2 hashes (see PasswordHasher.java) -- never in plain text.
-- audit_log: every create/update/delete records who, what, when,
--   and the old and new values.
-- =====================================================================
USE employeeData;

CREATE TABLE app_users (
    user_id         INT          NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            ENUM('HR_ADMIN', 'EMPLOYEE') NOT NULL,
    empID           INT          NULL,
    failed_attempts INT          NOT NULL DEFAULT 0,
    locked_until    DATETIME     NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_app_users_username (username),
    FOREIGN KEY (empID) REFERENCES employees (empID) ON DELETE CASCADE
);

CREATE TABLE audit_log (
    audit_id    BIGINT       NOT NULL AUTO_INCREMENT,
    changed_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id     INT          NULL,
    action      ENUM('INSERT', 'UPDATE', 'DELETE', 'LOGIN', 'LOGIN_FAILED') NOT NULL,
    table_name  VARCHAR(64)  NULL,
    record_id   VARCHAR(64)  NULL,
    old_value   TEXT         NULL,
    new_value   TEXT         NULL,
    PRIMARY KEY (audit_id),
    KEY idx_audit_changed_at (changed_at),
    FOREIGN KEY (user_id) REFERENCES app_users (user_id) ON DELETE SET NULL
);
