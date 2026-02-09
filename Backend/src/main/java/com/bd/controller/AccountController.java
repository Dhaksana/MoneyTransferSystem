package com.bd.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bd.dto.AccountDTO;
import com.bd.model.Account;
import com.bd.service.AccountService;
import com.bd.service.IAccountService;

@RestController
public class AccountController {
	@Autowired
	private final IAccountService accountService;
	
	public AccountController(IAccountService accountService) {
		this.accountService = accountService;
	}
	
	@PostMapping("/createAccount")
	public AccountDTO createAccount(@RequestBody AccountDTO account) {
		return accountService.createAccount(account);
	}

}
