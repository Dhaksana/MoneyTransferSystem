package com.bd.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.bd.dto.AccountDTO;
import com.bd.service.IAccountService;

@RestController
@RequestMapping("/api/v1/accounts")

@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true",   // only if you send cookies/Authorization header
        maxAge = 3600,
        exposedHeaders = {"Idempotency-Key"} // if you want FE to read this response header
)

public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    // CREATE
    @PostMapping
    public AccountDTO createAccount(@RequestBody AccountDTO account) {
        return accountService.createAccount(account);
    }

    // READ by ID
    @GetMapping("/{id}")
    public AccountDTO getAccountById(@PathVariable Integer id) {
        return accountService.getAccountById(id);
    }

    // READ balance only
    @GetMapping("/{id}/balance")
    public Double getBalance(@PathVariable Integer id) {
        return accountService.getBalance(id);
    }

    // READ all accounts
    @GetMapping
    public List<AccountDTO> getAllAccounts() {
        return accountService.getAllAccounts();
    }
}
