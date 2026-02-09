package com.bd.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bd.dto.AccountDTO;
import com.bd.model.Account;
import com.bd.repository.AccountRepository;

@Service
public class AccountService implements IAccountService{
	@Autowired
	AccountRepository accountRepo;

	public AccountDTO createAccount(AccountDTO account) {
		accountRepo.save(AccountDTO.fromDTO(account));
		return account;
	}



}
