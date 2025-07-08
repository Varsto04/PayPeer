package com.paypeer.p2p_transfer_service.repository;

import com.paypeer.p2p_transfer_service.model.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TransactionRepository {
    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Transaction transaction) {
        String sql = "INSERT INTO transactions (from_account_id, to_account_id, amount, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, transaction.getFromAccountId(), transaction.getToAccountId(),
                transaction.getAmount(), transaction.getStatus());
    }

    public List<Transaction> findByAccountId(Long accountId) {
        String sql = "SELECT * FROM transactions WHERE from_account_id = ? OR to_account_id = ? ORDER BY transaction_date DESC";
        return jdbcTemplate.query(sql, new Object[]{accountId, accountId}, this::mapRowToTransaction);
    }

    private Transaction mapRowToTransaction(ResultSet rs, int rowNum) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setFromAccountId(rs.getLong("from_account_id"));
        transaction.setToAccountId(rs.getLong("to_account_id"));
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
        transaction.setStatus(rs.getString("status"));
        return transaction;
    }
}