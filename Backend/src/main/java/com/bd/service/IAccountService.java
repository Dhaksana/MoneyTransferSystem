package com.bd.service;

import com.bd.dto.AccountDTO;
import java.util.List;


public interface IAccountService {

	AccountDTO createAccount(AccountDTO account);

    AccountDTO getAccountById(String id);

    Double getBalance(String id);

    List<AccountDTO> getAllAccounts();
    boolean accountExists(String id);
    boolean accountNumberExists(String accountNumber);

}
