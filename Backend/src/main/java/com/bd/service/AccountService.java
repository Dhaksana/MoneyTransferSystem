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
        Account account = AccountDTO.fromDTO(dto);
        if (account.getId() == null || account.getId().isBlank()) {
            account.setId(generateUniqueAccountId());
        }
        Account saved = accountRepo.save(account);
        return AccountDTO.toDTO(saved);
    }

    private final java.security.SecureRandom rnd = new java.security.SecureRandom();

    private String generateUniqueAccountId() {
        String candidate;
        int attempts = 0;
        do {
            String year = String.valueOf(java.time.LocalDate.now().getYear());
            int part = 10000000 + rnd.nextInt(90000000);
            candidate = "MTS" + year + "-" + part;
            attempts++;
            if (attempts > 20) break;
        } while (accountRepo.existsById(candidate));
        return candidate;
    }

    @Override
    public AccountDTO getAccountById(String id) {
        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id " + id));
        return AccountDTO.toDTO(account);
    }

    @Override
    public Double getBalance(String id) {
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
    public boolean accountExists(String id) {
        return accountRepo.existsById(id);
    }

    @Override
    public AccountDTO updateAccount(String id, AccountDTO accountDTO) {
        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id " + id));
        
        // Admin can update holder name, status, and balance
        if (accountDTO.getHolderName() != null && !accountDTO.getHolderName().isBlank()) {
            account.setHolderName(accountDTO.getHolderName());
        }
        if (accountDTO.getStatus() != null && !accountDTO.getStatus().isBlank()) {
            account.setStatus(accountDTO.getStatus());
        }
        if (accountDTO.getBalance() >= 0) {
            account.setBalance(accountDTO.getBalance());
        }
        
        Account updated = accountRepo.save(account);
        return AccountDTO.toDTO(updated);
    }

    @Override
    public void deactivateAccount(String id) {
        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id " + id));
        account.setStatus("INACTIVE");
        accountRepo.save(account);
    }
}
