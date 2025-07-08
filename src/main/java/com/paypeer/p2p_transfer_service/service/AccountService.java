package com.paypeer.p2p_transfer_service.service;

import com.paypeer.p2p_transfer_service.model.Account;
import com.paypeer.p2p_transfer_service.model.Transaction;
import com.paypeer.p2p_transfer_service.repository.AccountRepository;
import com.paypeer.p2p_transfer_service.repository.TransactionRepository;
import com.paypeer.p2p_transfer_service.repository.UserRepository;
import com.paypeer.p2p_transfer_service.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    public Account createAccount(Long userId, double initialBalance) {
        logger.info("Creating account for userId: {} with initial balance: {}", userId, initialBalance);
        if (initialBalance < 0) {
            logger.warn("Attempted to create account with negative balance: {}", initialBalance);
            throw new IllegalArgumentException("Начальный баланс не может быть отрицательным");
        }
        // Fetch user to get phone number
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Account account = new Account();
        account.setUserId(userId);
        // Generate globally unique account number
        account.setAccountNumber(generateAccountNumber(user.getPhoneNumber()));
        account.setBalance(initialBalance);
        account.setStatus("ACTIVE");
        accountRepository.save(account);
        logger.info("Account created: {}", account.getAccountNumber());
        return account;
    }


    public List<Account> getUserAccounts(Long userId) {
        logger.info("Fetching accounts for userId: {}", userId);
        return accountRepository.findByUserId(userId);
    }

    public void closeAccount(String accountNumber) {
        logger.info("Attempting to close account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    logger.error("Account not found: {}", accountNumber);
                    return new IllegalArgumentException("Account not found");
                });
        if (account.getBalance() < 0) {
            logger.warn("Attempted to close account {} with negative balance: {}", accountNumber, account.getBalance());
            throw new IllegalArgumentException("You cannot close an account with a negative balance");
        }
        account.setStatus("CLOSED");
        accountRepository.update(account);
        logger.info("Account closed: {}", accountNumber);
    }

    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        logger.info("Initiating transfer from {} to {} for amount: {}", fromAccountNumber, toAccountNumber, amount);
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> {
                    logger.error("Source account not found: {}", fromAccountNumber);
                    return new IllegalArgumentException("Source account not found");
                });

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> {
                    logger.error("Destination account not found: {}", toAccountNumber);
                    return new IllegalArgumentException("Destination account not found");
                });

        if (fromAccount.getBalance() < amount) {
            logger.warn("Insufficient funds in account {}: balance {}, attempted transfer {}",
                    fromAccountNumber, fromAccount.getBalance(), amount);
            throw new IllegalArgumentException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.update(fromAccount);
        accountRepository.update(toAccount);

        Transaction transaction = new Transaction();
        transaction.setFromAccountId(fromAccount.getId());
        transaction.setToAccountId(toAccount.getId());
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transactionRepository.save(transaction);
        logger.info("Transfer completed: {} to {} for amount {}", fromAccountNumber, toAccountNumber, amount);
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        logger.info("Fetching transaction history for account: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> transactionRepository.findByAccountId(account.getId()))
                .orElseThrow(() -> {
                    logger.error("Account not found for transaction history: {}", accountNumber);
                    return new IllegalArgumentException("Account not found");
                });
    }

    private String generateAccountNumber(String phoneNumber) {
        String digits = phoneNumber.replaceAll("\\D+", "");
        String timePart = String.valueOf(System.currentTimeMillis());
        String uuidPart = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        return digits + timePart + uuidPart;
    }
}