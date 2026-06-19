package com.bd.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bd.dto.AccountDTO;
import com.bd.exception.AccountNotFoundException;
import com.bd.model.Account;
import com.bd.model.AppUser;
import com.bd.repository.AccountRepository;
import com.bd.repository.AppUserRepository;

@Service
public class AccountService implements IAccountService {

    private final AccountRepository accountRepo;
    private final AppUserRepository userRepo;

    public AccountService(AccountRepository accountRepo, AppUserRepository userRepo) {
        this.accountRepo = accountRepo;
        this.userRepo = userRepo;
    }

    @Override
    public AccountDTO createAccount(AccountDTO dto) {
        Account account = AccountDTO.fromDTO(dto);
        if (account.getId() == null || account.getId().isBlank()) {
            account.setId(generateUniqueAccountId());
        }
        if (account.getAccountNumber() == null || account.getAccountNumber().isBlank()) {
            account.setAccountNumber(account.getId());
        }
        if (account.getAccountType() == null || account.getAccountType().isBlank()) {
            account.setAccountType("SAVINGS");
        }
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User id is required to create an account");
        }
        AppUser user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUserId()));
        account.setUser(user);
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
                .orElseThrow(() -> new AccountNotFoundException(id));
        return AccountDTO.toDTO(account);
    }

    @Override
    public Double getBalance(String id) {
        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
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
    public boolean accountNumberExists(String accountNumber) {
        return accountRepo.findByAccountNumber(accountNumber).isPresent();
    }

}
