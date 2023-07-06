package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.security.Principal;
import java.util.List;

public interface TransferDao {

    Transfer createTransfer(Transfer transfer);

    List<Transfer> getAllTransfersByUserId(int id);

    Transfer getTransfer(int id, Principal principal);

    List<Transfer> getPendingTransfers(int id);

    boolean updateTransfer(Transfer transfer);
}