package com.bd.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bd.event.TransferCompletedEvent;
import com.bd.model.TransactionLog;

@Component
public class TransferEventListener {

    private final RewardService rewardService;
    private final NotificationService notifications;
    private final AuditService auditService;

    public TransferEventListener(RewardService rewardService,
                                 NotificationService notifications,
                                 AuditService auditService) {
        this.rewardService = rewardService;
        this.notifications = notifications;
        this.auditService = auditService;
    }

    @EventListener
    @Transactional
    public void onTransferCompleted(TransferCompletedEvent event) {
        TransactionLog transaction = event.transaction();
        rewardService.awardForSuccessfulTransfer(transaction);
        createNotifications(transaction);
        auditService.log(transaction.getSenderAccount().getUser(), "TRANSFER_SUCCESS",
                "Reference " + transaction.getReferenceNumber()
                + " from " + transaction.getSenderAccount().getAccountNumber()
                + " to " + transaction.getReceiverAccount().getAccountNumber());
    }

    private void createNotifications(TransactionLog transaction) {
        notifications.create(transaction.getSenderAccount().getUser(), "TRANSFER_DEBIT",
                "Transfer " + transaction.getReferenceNumber() + " completed successfully.");
        notifications.create(transaction.getReceiverAccount().getUser(), "TRANSFER_CREDIT",
                "You received transfer " + transaction.getReferenceNumber() + ".");
    }


}
