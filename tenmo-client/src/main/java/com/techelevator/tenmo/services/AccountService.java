package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


//This is a service class. Its purpose is to handle Account services making requests to the server and prompting the user to enter data
//to be sent to the server.
public class AccountService {
    private final String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser authenticatedUser;

    public AccountService(String url) {
        this.baseUrl = url;
    }

    public AuthenticatedUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }


    //This method pass an Account object as a parameter and makes a request to the server to be updated in the database.
    public void updateAccount(Account account) {
        HttpEntity<Account> entity = makeAccountEntity(account);

        try {
            restTemplate.put(baseUrl + "/dashboard/account", entity);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
    }

    //This method connects to the server and retrieve an account with the user ID as the parameter
    public Account getAccount(int id) {
        Account account = null;

        try {
            ResponseEntity<Account> response = restTemplate.exchange(baseUrl + "/dashboard/account/" + id, HttpMethod.GET, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    //This method connects to the server and retrieve an account with the account ID as the parameter
    public Account getAccountById(int id) {
        Account account = null;

        try {
            ResponseEntity<Account> response = restTemplate.exchange(baseUrl + "/dashboard/get_account/" + id, HttpMethod.GET, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    //This method connects to the server and retrieve all the accounts of the current user.
    public List<Account> getUserAccounts() {
        Account[] accounts = null;

        try {
            ResponseEntity<Account[]> response =
                    restTemplate.exchange(baseUrl + "/dashboard/accounts", HttpMethod.GET,
                            makeAuthEntity(), Account[].class);
            accounts = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return Arrays.asList(accounts);
    }

    //Method to return all the transfers history of the current user
    public List<Transfer> getTransfers() {
        Transfer[] transfers = null;

        try {
            ResponseEntity<Transfer[]> responseEntity = restTemplate.exchange(baseUrl + "/dashboard/transfer" + "/transfers_history", HttpMethod.GET,
                    makeAuthEntity(), Transfer[].class);
            transfers = responseEntity.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return Arrays.asList(transfers);
    }


    //Method to return all the pending transfers of the current user
    public List<Transfer> getPendingTransfers() {
        Transfer[] transfers = null;

        try {
            ResponseEntity<Transfer[]> responseEntity = restTemplate.exchange(baseUrl + "/dashboard/transfer" + "/pending_transfers", HttpMethod.GET,
                    makeAuthEntity(), Transfer[].class);
            transfers = responseEntity.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return Arrays.asList(transfers);
    }

    //This method gets the account of the user. The logic for this method originally was supposed to search an account
    //if the user had more than one account, but since there is not a service to add more than one account to a user
    //the original proposal was discarded. Code still is suited to complete that task if in a future there is
    //the feature of adding another account to an existing user.
    public Account getUserAccount() {
        List<Account> accounts = getUserAccounts();
        Account account = null;

        if (!hasMultipleAccounts(accounts)) {
            account = accounts.get(0);
        }
        return account;
    }

    //Validation to check if the current user has multiple accounts.
    public boolean hasMultipleAccounts(List<Account> accounts) {
        return accounts.size() > 1;
    }

    //Validation to check when the current user selects the send option in a transfer service to see if the
    //intended amount to send is within the range of the account balance.
    public boolean isAmountInAccountRange(BigDecimal amountSend, BigDecimal accountBalance) {
        return amountSend.compareTo(accountBalance) == 1;
    }


    private HttpEntity<Account> makeAccountEntity(Account account) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authenticatedUser.getToken());
        return new HttpEntity<>(account, headers);
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authenticatedUser.getToken());
        return new HttpEntity<>(headers);
    }
}