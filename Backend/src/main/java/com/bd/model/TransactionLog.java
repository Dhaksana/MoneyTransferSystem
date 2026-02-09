package com.bd.model;

import java.time.ZonedDateTime;

public class TransactionLog {
		int id;
		String fromAccountId;
		String toAccountID;
		int amount;
		String status;
		String failureReason;
		int idempotencyKey;
		ZonedDateTime createdOn;


}