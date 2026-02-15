package com.bd.service;

import com.bd.dto.TransactionHistoryDTO;
import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.dto.PaginatedResponse;

import java.util.List;

public interface ITransferService {
    TransferResponseDTO transfer(TransferRequestDTO request);

    // 🔹 HISTORY API (Legacy - returns all)
    List<TransactionHistoryDTO> getTransactionHistory(String accountId);

    // 🔹 HISTORY API WITH PAGINATION
    PaginatedResponse<TransactionHistoryDTO> getTransactionHistoryPaginated(
            String accountId, int page, int size);

    // 🔹 HISTORY API WITH PAGINATION AND FILTERING
    PaginatedResponse<TransactionHistoryDTO> getTransactionHistoryPaginatedWithFilter(
            String accountId, int page, int size, String filter);

    // 🔹 ADMIN API - GET ALL TRANSACTIONS IN SYSTEM (paginated)
    PaginatedResponse<TransactionHistoryDTO> getAllTransactionsPaginated(int page, int size);
}
