package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

//This is a service class, meaning that is the one that make the requests to the server to send and/or retrieve information.
public class TransferService {
    private String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser authenticatedUser;


    public TransferService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }


    //This method connects to the server to insert a new transfer in the database, and the server returns the same transfer with
    //its generated ID.
    public Transfer createTransfer(Transfer transfer) {
        HttpEntity<Transfer> entity = createTransferEntity(transfer);
        Transfer returnedTransfer = null;

        try {
            returnedTransfer = restTemplate.postForObject(baseUrl + "dashboard/transfer", entity, Transfer.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return returnedTransfer;
    }

    //Sends an updated transfer to the server.
    public boolean updateTransfer(Transfer transfer) {
        HttpEntity<Transfer> entity = createTransferEntity(transfer);
        boolean wasUpdated = false;

        try {
            restTemplate.put(baseUrl + "/dashboard/transfer/transfer_approved", entity);
            wasUpdated = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return wasUpdated;
    }

    //Helper method. Verifies if the transfer is valid by requesting the transfer to the database and validating that
    //the transfer exists.
    public boolean isTransferValid(int transferId) {
        if (transferId == 0) {
            System.out.println("Returning to the main menu...");
            return false;
        }
        Transfer transfer = getTransferById(transferId);

        if (transfer == null) {
            System.out.println("You don't have a transfer with the ID " + transferId + ".");
            System.out.println("Returning to the main menu...");
            return false;
        }
        return true;
    }

    //Helper method. Verifies if the transfer choice from the user is valid. If not return a message with a false value in the
    //app class.
    public boolean isTransferChoiceValid(int transferChoice) {
        if (transferChoice == 0) {
            System.out.println("Returning to the main menu...");
            return false;
        }

        if (transferChoice != 1 && transferChoice != 2) {
            System.out.println("Not a valid selection.");
            System.out.println("Returning to the main menu...");
            return false;
        }
        return true;
    }

    //Should return transfer by transfer id
    public Transfer getTransferById(int id) {
        Transfer transfer = null;

        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(baseUrl + "/dashboard/transfer/" + id, HttpMethod.GET, makeAuthEntity(), Transfer.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    //Helper method to set the values of a transfer object. Method is used in App class.
    public Transfer setTransferDetails(Transfer transfer, int type, int status, int accountFrom, int accountTo, BigDecimal amount) {
        transfer.setTransferTypeId(type);
        transfer.setTransferStatusId(status);
        transfer.setAccountFrom(accountFrom);
        transfer.setAccountTo(accountTo);
        transfer.setAmount(amount);
        return transfer;
    }


    //Method to create an entity of a Transfer object and setting the authentication of the authenticated user.
    private HttpEntity<Transfer> createTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authenticatedUser.getToken());
        return new HttpEntity<>(transfer, headers);
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authenticatedUser.getToken());
        return new HttpEntity<>(headers);
    }
}