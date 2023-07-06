package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcAccountDao implements AccountDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    //DAO method to find all accounts in the database
    @Override
    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_id, user_id, balance FROM account;";

        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql);
        while (rows.next()) {
            Account account = mapRowToAccount(rows);
            accounts.add(account);
        }
        return accounts;
    }

    //DAO method to get an account with an account ID
    @Override
    public Account getAccountById(int id) {
        Account account = null;
        String sql = "SELECT account_id, user_id, balance FROM account WHERE account_id = ?;";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, id);
        if (rows.next()) {
            account = mapRowToAccount(rows);
        }
        return account;
    }

    //DAO method to get an account with a user ID
    @Override
    public Account findByUserId(int id) {
        Account account = null;
        String sql = "SELECT account_id, user_id, balance FROM account WHERE user_id = ?;";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, id);
        if (rows.next()) {
            account = mapRowToAccount(rows);
        }
        return account;
    }

    ////DAO method to find a list of accounts using a username
    @Override
    public List<Account> findByUsername(String username) {
        List<Account> accounts = new ArrayList<>();
        if (username == null) throw new IllegalArgumentException("Username cannot be null");

        String sql = "SELECT account_id, user_id, balance FROM account " +
                "JOIN tenmo_user USING(user_id) " +
                "WHERE username = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        while (rowSet.next()) {
            Account account = mapRowToAccount(rowSet);
            accounts.add(account);
        }
        if (!accounts.isEmpty()) {
            return accounts;
        }
        throw new UsernameNotFoundException("Account with username " + username + " was not found.");
    }

    ////DAO method to update the balance using an account ID
    @Override
    public boolean update(Account account) {
        String sql = "UPDATE account SET balance = ? WHERE account_id = ? ";
        return jdbcTemplate.update(sql, account.getBalance(), account.getAccountID()) == 1;
    }


    private Account mapRowToAccount(SqlRowSet rowSet) {
        Account account = new Account();
        account.setAccountID(rowSet.getInt("account_id"));
        account.setUserID(rowSet.getInt("user_id"));
        account.setBalance(rowSet.getBigDecimal("balance"));
        return account;
    }
}