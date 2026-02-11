package com.bd.controller;

import com.bd.dto.TransactionHistoryDTO;
import org.springframework.web.bind.annotation.*;

import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.service.ITransferService;

import java.util.List;

@RestController
@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true",   // only if you send cookies/Authorization header
        maxAge = 3600,
        exposedHeaders = {"Idempotency-Key"} // if you want FE to read this response header
)

@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final ITransferService transferService;

    public TransferController(ITransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public TransferResponseDTO transfer(@RequestBody TransferRequestDTO request) {
        return transferService.transfer(request);
    }


    // ✅ GET /api/v1/transfers/history/{accountId}
    @GetMapping("/history/{accountId}")
    public List<TransactionHistoryDTO> history(@PathVariable Integer accountId) {
        return transferService.getTransactionHistory(accountId);
    }
}
