package com.bd.controller;

import com.bd.dto.TransactionHistoryDTO;
import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.service.ITransferService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final ITransferService transferService;

    public TransferController(ITransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public TransferResponseDTO transfer(@Valid @RequestBody TransferRequestDTO request) {
        return transferService.transfer(request);
    }


    // ✅ GET /api/v1/transfers/history/{accountId}
    @GetMapping("/history/{accountId}")
    public List<TransactionHistoryDTO> history(@PathVariable String accountId) {
        return transferService.getTransactionHistory(accountId);
    }
}
