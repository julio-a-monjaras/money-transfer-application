package com.techelevator.tenmo.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transfer {
    private int transferId;
    private int transferTypeId;
    private int transferStatusId;
    private int accountFrom;
    private int accountTo;
    private BigDecimal amount;


    private String transferType(int transferTypeId) {
        if (transferTypeId == 1) {
            return "Request";
        }
        return "Send";
    }

    public String transferStatus(int transferStatusId) {
        if (transferStatusId == 1) {
            return "Pending";
        } else if (transferStatusId == 2) {
            return "Approved";
        }
        return "Rejected";
    }


    @Override
    public String toString() {
        return "Transfer Id: " + transferId +
                "\nAccount From: " + accountFrom +
                "\nAccount To: " + accountTo +
                "\nTransfer Type: " + transferType(transferTypeId) +
                "\nTransfer Status: " + transferStatus(transferStatusId) +
                "\nAmount: $" + amount;
    }
}