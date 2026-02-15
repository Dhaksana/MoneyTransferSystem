package com.bd.controller;

import com.bd.dto.TransactionHistoryDTO;
import com.bd.dto.PaginatedResponse;
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

    // GET /api/v1/transfers/history/{accountId}
    @GetMapping("/history/{accountId}")
    public List<TransactionHistoryDTO> history(@PathVariable String accountId) {
        return transferService.getTransactionHistory(accountId);
    }

    // GET /api/v1/transfers/history/{accountId}/paginated?page=0&size=10
    @GetMapping("/history/{accountId}/paginated")
    public PaginatedResponse<TransactionHistoryDTO> historyPaginated(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return transferService.getTransactionHistoryPaginated(accountId, page, size);
    }

    // GET /api/v1/transfers/history/{accountId}/paginated-filter?page=0&size=10&filter=sent
    @GetMapping("/history/{accountId}/paginated-filter")
    public PaginatedResponse<TransactionHistoryDTO> historyPaginatedWithFilter(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String filter) {
        return transferService.getTransactionHistoryPaginatedWithFilter(accountId, page, size, filter);
    }
}
