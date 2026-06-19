package com.bd.event;

import com.bd.model.TransactionLog;

public record TransferCompletedEvent(TransactionLog transaction) {
}
