package com.paypeer.p2p_transfer_service.repository;

import com.paypeer.p2p_transfer_service.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Account account) {
        String sql = "INSERT INTO accounts (user_id, account_number, balance, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, account.getUserId(), account.getAccountNumber(), account.getBalance(), account.getStatus());
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ? AND status = 'ACTIVE'";
        try {
            Account account = jdbcTemplate.queryForObject(sql, new Object[]{accountNumber}, this::mapRowToAccount);
            return Optional.ofNullable(account);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Account> findByUserId(Long userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ? AND status = 'ACTIVE'";
        return jdbcTemplate.query(sql, new Object[]{userId}, this::mapRowToAccount);
    }

    public void update(Account account) {
        String sql = "UPDATE accounts SET balance = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(sql, account.getBalance(), account.getStatus(), account.getId());
    }

    private Account mapRowToAccount(ResultSet rs, int rowNum) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("id"));
        account.setUserId(rs.getLong("user_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setBalance(rs.getDouble("balance"));
        account.setStatus(rs.getString("status"));
        return account;
    }
}