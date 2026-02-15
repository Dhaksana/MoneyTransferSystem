package com.bd.controller;

import com.bd.dto.AccountDTO;
import com.bd.dto.PaginatedResponse;
import com.bd.dto.TransactionHistoryDTO;
import com.bd.service.IAccountService;
import com.bd.service.ITransferService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Controller - Restricted access for admin users only
 * Admin can view and manage all accounts and transactions
 */
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(
        origins = "http://localhost:4200",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true",
        maxAge = 3600
)
public class AdminController {

    private final IAccountService accountService;
    private final ITransferService transferService;

    public AdminController(IAccountService accountService, ITransferService transferService) {
        this.accountService = accountService;
        this.transferService = transferService;
    }

    // GET all accounts
    @GetMapping("/accounts")
    public List<AccountDTO> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    // GET all transactions (paginated)
    @GetMapping("/transactions/paginated")
    public PaginatedResponse<TransactionHistoryDTO> getAllTransactionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return transferService.getAllTransactionsPaginated(page, size);
    }

    // UPDATE account details (unlock, change status, etc.)
    @PutMapping("/accounts/{id}")
    public AccountDTO updateAccount(@PathVariable String id, @RequestBody AccountDTO accountDTO) {
        return accountService.updateAccount(id, accountDTO);
    }

    // DELETE/DEACTIVATE an account
    @DeleteMapping("/accounts/{id}")
    public void deactivateAccount(@PathVariable String id) {
        accountService.deactivateAccount(id);
    }
}
