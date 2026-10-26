package com.companyz.ems.dao;

import com.companyz.ems.model.UserAccount;
import java.time.LocalDateTime;
import java.util.Optional;

/** Data access for application logins ({@code app_users}). Uses the ems_auth DB account. */
public interface UserDao {

    Optional<UserAccount> findByUsername(String username);

    void recordFailedAttempt(int userId, LocalDateTime lockUntilOrNull);

    void resetFailedAttempts(int userId);
}
