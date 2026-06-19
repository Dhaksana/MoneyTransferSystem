package com.bd.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bd.dto.BeneficiaryDTO;
import com.bd.exception.AccountNotFoundException;
import com.bd.model.Account;
import com.bd.model.Beneficiary;
import com.bd.repository.AccountRepository;
import com.bd.repository.BeneficiaryRepository;

@Service
public class BeneficiaryService {
    private final BeneficiaryRepository beneficiaries;
    private final AccountRepository accounts;
    private final CurrentUserService currentUser;
    private final NotificationService notifications;
    private final AuditService auditService;

    public BeneficiaryService(BeneficiaryRepository beneficiaries, AccountRepository accounts, CurrentUserService currentUser, NotificationService notifications, AuditService auditService) {
        this.beneficiaries = beneficiaries;
        this.accounts = accounts;
        this.currentUser = currentUser;
        this.notifications = notifications;
        this.auditService = auditService;
    }

    public List<BeneficiaryDTO> listMine() {
        return beneficiaries.findByOwnerAccountUserUsernameOrderByFavoriteDescBeneficiaryNameAsc(currentUser.username())
                .stream().map(BeneficiaryDTO::from).toList();
    }

    @Transactional
    public BeneficiaryDTO add(BeneficiaryDTO dto) {
        Account owner = resolveOwnedAccount(dto.getOwnerAccountId());
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setOwnerAccount(owner);
        apply(dto, beneficiary);
        Beneficiary saved = beneficiaries.save(beneficiary);
        notifications.create(owner.getUser(), "BENEFICIARY_ADDED", "Beneficiary " + saved.getBeneficiaryName() + " added.");
        auditService.log("BENEFICIARY_ADDED", "Added beneficiary " + saved.getBeneficiaryName() + " (" + saved.getBeneficiaryAccountNumber() + ")");
        return BeneficiaryDTO.from(saved);
    }

    @Transactional
    public BeneficiaryDTO update(Long id, BeneficiaryDTO dto) {
        Beneficiary beneficiary = beneficiaries.findById(id).orElseThrow(() -> new IllegalArgumentException("Beneficiary not found"));
        assertOwner(beneficiary);
        apply(dto, beneficiary);
        auditService.log("BENEFICIARY_UPDATED", "Updated beneficiary " + beneficiary.getBeneficiaryName());
        return BeneficiaryDTO.from(beneficiary);
    }

    @Transactional
    public void delete(Long id) {
        Beneficiary beneficiary = beneficiaries.findById(id).orElseThrow(() -> new IllegalArgumentException("Beneficiary not found"));
        assertOwner(beneficiary);
        String name = beneficiary.getBeneficiaryName();
        var user = beneficiary.getOwnerAccount().getUser();
        auditService.log("BENEFICIARY_REMOVED", "Removed beneficiary " + name);
        beneficiaries.delete(beneficiary);
        notifications.create(user, "BENEFICIARY_REMOVED", "Beneficiary " + name + " removed.");
    }

    private void apply(BeneficiaryDTO dto, Beneficiary beneficiary) {
        beneficiary.setBeneficiaryName(dto.getBeneficiaryName());
        beneficiary.setBeneficiaryAccountNumber(dto.getBeneficiaryAccountNumber());
        beneficiary.setBankName(dto.getBankName());
        beneficiary.setIfsc(dto.getIfsc());
        beneficiary.setNickname(dto.getNickname());
        beneficiary.setFavorite(dto.isFavorite());
    }

    private Account resolveOwnedAccount(String accountId) {
        Account account = accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        if (!currentUser.username().equals(account.getUser().getUsername())) {
            throw new IllegalArgumentException("Cannot manage beneficiaries for another user");
        }
        return account;
    }

    private void assertOwner(Beneficiary beneficiary) {
        if (!currentUser.username().equals(beneficiary.getOwnerAccount().getUser().getUsername())) {
            throw new IllegalArgumentException("Cannot modify another user's beneficiary");
        }
    }
}
