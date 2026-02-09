package com.bd.service;

import com.bd.dto.TransferRequestDTO;
import com.bd.dto.TransferResponseDTO;

public interface ITransferService {
    TransferResponseDTO transfer(TransferRequestDTO request);
}
