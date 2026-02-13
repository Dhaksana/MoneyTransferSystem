package com.bd.repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bd.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String>{
    Optional<Account> findByHolderNameIgnoreCase(String holderName);

}
