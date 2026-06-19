package com.bd.controller;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.bd.dto.AccountDTO;
import com.bd.service.IAccountService;

@RestController
@Validated
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    // CREATE
    @PostMapping
    public AccountDTO createAccount(@Valid @RequestBody AccountDTO account) {
        return accountService.createAccount(account);
    }

    // READ by ID
    @GetMapping("/{id}")
    public AccountDTO getAccountById(@PathVariable String id) {
        return accountService.getAccountById(id);
    }

    // READ balance only
    @GetMapping("/{id}/balance")
    public Double getBalance(@PathVariable String id) {
        return accountService.getBalance(id);
    }

    // READ all accounts
    @GetMapping
    public List<AccountDTO> getAllAccounts() {
        return accountService.getAllAccounts();
    }
    // CHECK if account exists
    @GetMapping("/exists/{id}")
    public boolean accountExists(@PathVariable String id) {
        return accountService.accountExists(id);
    }

    @GetMapping("/exists/account-number/{accountNumber}")
    public boolean accountNumberExists(@PathVariable String accountNumber) {
        return accountService.accountNumberExists(accountNumber);
    }

}
