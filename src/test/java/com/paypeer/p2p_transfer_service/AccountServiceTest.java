package com.paypeer.p2p_transfer_service;

import com.paypeer.p2p_transfer_service.model.Account;
import com.paypeer.p2p_transfer_service.model.Transaction;
import com.paypeer.p2p_transfer_service.repository.AccountRepository;
import com.paypeer.p2p_transfer_service.service.AccountService;
import com.paypeer.p2p_transfer_service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setUserId(1L);
        account.setAccountNumber("1234567890123456");
        account.setBalance(100.0);
        account.setStatus("ACTIVE");
    }

    @Test
    void createAccount_success() {
        doNothing().when(accountRepository).save(any(Account.class));
        Account created = accountService.createAccount(1L, 50.0);
        assertNotNull(created);
        assertEquals(50.0, created.getBalance());
        assertEquals("ACTIVE", created.getStatus());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_negativeBalance_throwsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(1L, -10.0)
        );
        assertEquals("Initial balance cannot be negative", exception.getMessage());
    }

    @Test
    void getUserAccounts_success() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(account));
        List<Account> accounts = accountService.getUserAccounts(1L);
        assertEquals(1, accounts.size());
        assertEquals(account, accounts.get(0));
        verify(accountRepository).findByUserId(1L);
    }

    @Test
    void closeAccount_success() {
        when(accountRepository.findByAccountNumber("1234567890123456")).thenReturn(Optional.of(account));
        doNothing().when(accountRepository).update(any(Account.class));
        accountService.closeAccount("1234567890123456");
        assertEquals("CLOSED", account.getStatus());
        verify(accountRepository).update(account);
    }

    @Test
    void closeAccount_negativeBalance_throwsException() {
        account.setBalance(-10.0);
        when(accountRepository.findByAccountNumber("1234567890123456")).thenReturn(Optional.of(account));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.closeAccount("1234567890123456")
        );
        assertEquals("Cannot close account with negative balance", exception.getMessage());
    }

    @Test
    void closeAccount_notFound_throwsException() {
        when(accountRepository.findByAccountNumber("1234567890123456")).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.closeAccount("1234567890123456")
        );
        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void transfer_success() {
        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAccountNumber("9876543210987654");
        toAccount.setBalance(50.0);
        toAccount.setStatus("ACTIVE");

        when(accountRepository.findByAccountNumber("1234567890123456")).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("9876543210987654")).thenReturn(Optional.of(toAccount));
        doNothing().when(accountRepository).update(any(Account.class));
        doNothing().when(transactionRepository).save(any(Transaction.class));

        accountService.transfer("1234567890123456", "9876543210987654", 30.0);

        assertEquals(70.0, account.getBalance());
        assertEquals(80.0, toAccount.getBalance());
        verify(accountRepository, times(2)).update(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_insufficientFunds_throwsException() {
        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAccountNumber("9876543210987654");
        toAccount.setBalance(50.0);

        when(accountRepository.findByAccountNumber("1234567890123456")).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("9876543210987654")).thenReturn(Optional.of(toAccount));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.transfer("1234567890123456", "9876543210987654", 150.0)
        );
        assertEquals("Insufficient funds", exception.getMessage());
    }
}