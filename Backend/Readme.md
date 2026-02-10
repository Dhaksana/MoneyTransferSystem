POST http://localhost:8080/api/v1/accounts
Body:
{
    "holderName": "Ram",
    "balance": 90,
    "status": "ACTIVE"
}

Output:
{
    "balance": 90.0,
    "holderName": "Ram",
    "id": 3,
    "status": "ACTIVE",
    "version": 0
}



GET http://localhost:8080/api/v1/accounts/1/balance
Output:
4000.0



POST http://localhost:8080/api/v1/transfers
Body:
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 1000,
  "idempotencyKey": "txn-001"
}

Output:
{
    "transactionId": 1,
    "status": "SUCCESS",
    "message": "Transfer completed successfully"
}


GET http://localhost:8080/api/v1/transfers/history/1

Output:
[
    {
        "transactionId": 1,
        "fromAccountId": 1,
        "toAccountId": 2,
        "amount": 1000.0,
        "status": "SUCCESS",
        "failureReason": null,
        "createdOn": "2026-02-10T10:12:04.167553"
    }
]