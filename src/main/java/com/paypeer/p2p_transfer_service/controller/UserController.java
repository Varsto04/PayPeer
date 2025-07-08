package com.paypeer.p2p_transfer_service.controller;

import com.paypeer.p2p_transfer_service.model.Account;
import com.paypeer.p2p_transfer_service.model.User;
import com.paypeer.p2p_transfer_service.service.AccountService;
import com.paypeer.p2p_transfer_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/login")
    public String login(Model model, @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("error", "Invalid phone number or password");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String countryCode,
                           @RequestParam String normalizedPhoneNumber,
                           @RequestParam String password,
                           @RequestParam(required = false) String email,
                           Model model) {
        try {
            String phoneNumber = normalizedPhoneNumber.startsWith(countryCode)
                    ? normalizedPhoneNumber
                    : countryCode + normalizedPhoneNumber.replaceAll("[^0-9]", "");

            userService.registerUser(fullName, phoneNumber, password, email);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        String phoneNumber = authentication.getName();
        User user = userService.findByPhoneNumber(phoneNumber);
        List<Account> accounts = accountService.getUserAccounts(user.getId());
        model.addAttribute("accounts", accounts);
        model.addAttribute("user", user);
        return "home";
    }

    @PostMapping("/account/create")
    public String createAccount(@RequestParam double initialBalance, Authentication authentication, Model model) {
        try {
            String phoneNumber = authentication.getName();
            User user = userService.findByPhoneNumber(phoneNumber);
            accountService.createAccount(user.getId(), initialBalance);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            String phoneNumber = authentication.getName();
            User user = userService.findByPhoneNumber(phoneNumber);
            model.addAttribute("accounts", accountService.getUserAccounts(user.getId()));
            model.addAttribute("user", user);
            return "home";
        }
    }

    @GetMapping("/transfer")
    public String transferForm(Model model, Authentication authentication) {
        String phoneNumber = authentication.getName();
        User user = userService.findByPhoneNumber(phoneNumber);
        model.addAttribute("accounts", accountService.getUserAccounts(user.getId()));
        return "transfer";
    }

    @PostMapping("/account/close")
    public String closeAccount(@RequestParam String accountNumber, Authentication authentication, Model model) {
        try {
            accountService.closeAccount(accountNumber);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            String phoneNumber = authentication.getName();
            User user = userService.findByPhoneNumber(phoneNumber);
            model.addAttribute("accounts", accountService.getUserAccounts(user.getId()));
            model.addAttribute("user", user);
            return "home";
        }
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam String fromAccountNumber,
                           @RequestParam String toAccountNumber,
                           @RequestParam double amount,
                           Model model) {
        try {
            accountService.transfer(fromAccountNumber, toAccountNumber, amount);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "transfer";
        }
    }

    @GetMapping("/history")
    public String transactionHistory(@RequestParam String accountNumber, Model model, Authentication authentication) {
        String phoneNumber = authentication.getName();
        User user = userService.findByPhoneNumber(phoneNumber);
        model.addAttribute("accounts", accountService.getUserAccounts(user.getId()));
        model.addAttribute("transactions", accountService.getTransactionHistory(accountNumber));
        model.addAttribute("selectedAccount", accountNumber);
        return "history";
    }
}