package com.bd.service;

import java.util.ArrayList;

import com.bd.dto.AccountDTO;
import com.bd.dto.PaginatedResponse;
import com.bd.model.Account;
import java.util.List;


public interface IAccountService {

	AccountDTO createAccount(AccountDTO account);

    AccountDTO getAccountById(String id);

    Double getBalance(String id);

    List<AccountDTO> getAllAccounts();

    PaginatedResponse<AccountDTO> getAccountsPaginated(int page, int size, String search);
    
    boolean accountExists(String id);
    
    // Admin operations
    AccountDTO updateAccount(String id, AccountDTO accountDTO);
    
    void deactivateAccount(String id);

}
