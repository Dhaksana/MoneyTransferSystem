package com.bd.service;

import java.util.ArrayList;

import com.bd.dto.AccountDTO;
import com.bd.model.Account;
import java.util.List;


public interface IAccountService {

	AccountDTO createAccount(AccountDTO account);

    AccountDTO getAccountById(Integer id);

    Double getBalance(Integer id);

    List<AccountDTO> getAllAccounts();
    boolean accountExists(Integer id);

}
