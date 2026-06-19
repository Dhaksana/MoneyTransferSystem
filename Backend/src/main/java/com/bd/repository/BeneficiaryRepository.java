package com.bd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bd.model.Beneficiary;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByOwnerAccountUserUsernameOrderByFavoriteDescBeneficiaryNameAsc(String username);
}
