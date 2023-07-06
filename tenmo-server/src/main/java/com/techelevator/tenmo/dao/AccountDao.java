package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.util.List;

public interface AccountDao {

    List<Account> findAll();

    Account getAccountById(int id);

    Account findByUserId(int id);

    List<Account> findByUsername(String username);

    boolean update(Account account);
}