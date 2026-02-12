package com.bd.service;

import java.util.ArrayList;

import com.bd.dto.AccountDTO;
import com.bd.model.Account;
import java.util.List;


public interface IAccountService {

	AccountDTO createAccount(AccountDTO account);

    AccountDTO getAccountById(String id);

    Double getBalance(String id);

    List<AccountDTO> getAllAccounts();
    boolean accountExists(String id);

}
