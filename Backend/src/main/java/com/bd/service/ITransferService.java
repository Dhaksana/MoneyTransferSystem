package com.bd.service;

import com.bd.dto.TransactionHistoryDTO;
import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;

import java.util.List;

public interface ITransferService {
    TransferResponseDTO transfer(TransferRequestDTO request);

    // 🔹 HISTORY API
    List<TransactionHistoryDTO> getTransactionHistory(
            Integer accountId);
}
