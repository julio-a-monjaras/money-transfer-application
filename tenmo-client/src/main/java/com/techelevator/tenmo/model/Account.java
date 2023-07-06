package com.techelevator.tenmo.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private int accountID;
    private int userID;
    private BigDecimal balance;


    public void sendMoney(BigDecimal amount) {
        BigDecimal newBalance = balance.subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) >= 0) {
            this.balance = newBalance;
        } else {
            System.out.println("Insufficient funds");
        }
    }

    public void receiveMoney(BigDecimal amount) {
        balance = balance.add(amount);
    }


    @Override
    public String toString() {
        return "Account{" +
                "accountID=" + accountID +
                ", balance=" + balance +
                '}';
    }
}