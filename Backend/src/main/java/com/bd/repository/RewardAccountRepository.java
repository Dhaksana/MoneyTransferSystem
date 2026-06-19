package com.bd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bd.model.AppUser;
import com.bd.model.RewardAccount;

public interface RewardAccountRepository extends JpaRepository<RewardAccount, Long> {
    Optional<RewardAccount> findByUser(AppUser user);
    Optional<RewardAccount> findByUserUsername(String username);
}
