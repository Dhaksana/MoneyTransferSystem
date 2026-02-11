package com.bd.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bd.dto.AccountDTO;
import com.bd.model.Account;
import com.bd.repository.AccountRepository;

@Service
public class AccountService implements IAccountService {

    private final AccountRepository accountRepo;

    public AccountService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    public AccountDTO createAccount(AccountDTO dto) {
        Account saved = accountRepo.save(AccountDTO.fromDTO(dto));
        return AccountDTO.toDTO(saved);
    }

    @Override
    public AccountDTO getAccountById(Integer id) {
        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id " + id));
        return AccountDTO.toDTO(account);
    }

    @Override
    public Double getBalance(Integer id) {
        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id " + id));
        return account.getBalance();
    }

    @Override
    public List<AccountDTO> getAllAccounts() {
        return accountRepo.findAll()
                .stream()
                .map(AccountDTO::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    public boolean accountExists(Integer id) {
        return accountRepo.existsById(id);
    }

}
