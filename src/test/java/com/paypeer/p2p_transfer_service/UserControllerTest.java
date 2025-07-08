package com.paypeer.p2p_transfer_service;

import com.paypeer.p2p_transfer_service.model.Account;
import com.paypeer.p2p_transfer_service.model.User;
import com.paypeer.p2p_transfer_service.service.AccountService;
import com.paypeer.p2p_transfer_service.service.UserService;
import com.paypeer.p2p_transfer_service.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private UserController userController;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test User");
        user.setPhoneNumber("+1234567890");

        account = new Account();
        account.setId(1L);
        account.setUserId(1L);
        account.setAccountNumber("1234567890123456");
        account.setBalance(100.0);
        account.setStatus("ACTIVE");
    }

    @Test
    @WithMockUser(username = "+1234567890")
    void home_success() throws Exception {
        when(userService.findByPhoneNumber("+1234567890")).thenReturn(user);
        when(accountService.getUserAccounts(1L)).thenReturn(List.of(account));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("accounts"));
    }

    @Test
    void login_success() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void login_withError() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Invalid phone number or password"));
    }

    @Test
    void registerForm_success() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @WithMockUser(username = "+1234567890")
    void createAccount_success() throws Exception {
        when(userService.findByPhoneNumber("+1234567890")).thenReturn(user);
        when(accountService.createAccount(anyLong(), anyDouble())).thenReturn(account);

        mockMvc.perform(post("/account/create").param("initialBalance", "50.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "+1234567890")
    void createAccount_negativeBalance() throws Exception {
        when(userService.findByPhoneNumber("+1234567890")).thenReturn(user);
        when(accountService.createAccount(anyLong(), anyDouble()))
                .thenThrow(new IllegalArgumentException("Initial balance cannot be negative"));
        when(accountService.getUserAccounts(1L)).thenReturn(List.of(account));

        mockMvc.perform(post("/account/create").param("initialBalance", "-10.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("error", "Initial balance cannot be negative"));
    }

    @Test
    @WithMockUser(username = "+1234567890")
    void closeAccount_success() throws Exception {
        mockMvc.perform(post("/account/close").param("accountNumber", "1234567890123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}