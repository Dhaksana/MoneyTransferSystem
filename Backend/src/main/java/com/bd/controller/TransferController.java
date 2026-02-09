package com.bd.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;
import com.bd.service.ITransferService;

@RestController
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
}
