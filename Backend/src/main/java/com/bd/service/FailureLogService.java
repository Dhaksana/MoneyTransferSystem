package com.bd.service;

import com.bd.model.TransactionLog;
import com.bd.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FailureLogService {

    private final TransactionLogRepository logRepo;

    public FailureLogService(TransactionLogRepository logRepo) {
        this.logRepo = logRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailureLog(TransactionLog log) {
        logRepo.save(log);
    }
}
