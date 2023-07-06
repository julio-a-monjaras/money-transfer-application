package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final int TRANSFER_TYPE_REQUEST = 1;
    private final int TRANSFER_TYPE_SEND = 2;
    private final int TRANSFER_STATUS_PENDING = 1;
    private final int TRANSFER_STATUS_APPROVED = 2;
    private final int TRANSFER_STATUS_REJECTED = 3;

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private AuthenticatedUser currentUser;
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final TransferService transferService = new TransferService(API_BASE_URL);
    private final UserService userService = new UserService(API_BASE_URL);


    public static void main(String[] args) {
        App app = new App();
        app.run();
    }


    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        //Sets the authenticated user to be able to make use of the services.
        accountService.setAuthenticatedUser(currentUser);
        transferService.setAuthenticatedUser(currentUser);
        userService.setAuthenticatedUser(currentUser);
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu(currentUser.getUser());
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                System.exit(1);
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        consoleService.printAccountBalance(accountService.getUserAccount());
    }

    private void viewTransferHistory() {
        Account currentAccount = accountService.getUserAccount();
        List<Transfer> transfers = accountService.getTransfers();
        List<String> userFromTransfers = userService.getUsersFromTransfers(transfers, currentAccount);
        consoleService.printTransferHistory(transfers, userFromTransfers, currentAccount);
        int transferId = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
        System.out.println();

        if (!transferService.isTransferValid(transferId)) {
            System.out.println();
            mainMenu();
        }

        Transfer transfer = transferService.getTransferById(transferId);
        String userFrom = userService.getUsernameFromAccount(transfer.getAccountFrom());
        String userTo = userService.getUsernameFromAccount(transfer.getAccountTo());
        consoleService.printTransferDetails(transfer, userFrom, userTo);
    }

    private void viewPendingRequests() {
        List<Transfer> transfers = accountService.getPendingTransfers();
        List<String> usersFromTransfers = new ArrayList<>();
        Account currentAccount = accountService.getUserAccount();
        for (Transfer transfer : transfers) {
            String username = userService.getUsernameFromAccount(transfer.getAccountTo());
            usersFromTransfers.add(username);
        }
        if(transfers.isEmpty()){
            System.out.println("You don't have any pending transfers to be accepted.");
            System.out.println("Returning to the main menu...\n");
            mainMenu();
        }

        consoleService.printPendingTransfers(transfers, usersFromTransfers, currentAccount);

        int transferId = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel):");

        if (!transferService.isTransferValid(transferId)) {
            System.out.println();
            mainMenu();
        }

        consoleService.printPendingOptions();


        Transfer transfer = transferService.getTransferById(transferId);
        Account receiverAccount = accountService.getAccountById(transfer.getAccountTo());
        boolean isAmountInRange = accountService.isAmountInAccountRange(transfer.getAmount(), currentAccount.getBalance());

        startPendingTransferProcess(transfer, currentAccount, receiverAccount, isAmountInRange);

    }

    private void sendBucks() {
        beginTransferProcess(TRANSFER_TYPE_SEND);
    }


    private void requestBucks() {
        beginTransferProcess(TRANSFER_TYPE_REQUEST);
    }

    public void startPendingTransferProcess(Transfer transfer, Account currentAccount, Account receiverAccount,
                                            boolean isAmountInRange) {
        int transferChoice = consoleService.promptForInt("Please choose an option: ");

        if (!transferService.isTransferChoiceValid(transferChoice)) {
            mainMenu();
        }
        final int TRANSFER_APPROVE = 1;
        final int TRANSFER_REJECT = 2;
        if (transferChoice == TRANSFER_APPROVE) {
            transfer.setTransferStatusId(TRANSFER_STATUS_APPROVED);
        }

        if (transferChoice == TRANSFER_REJECT) {
            transfer.setTransferStatusId(TRANSFER_STATUS_REJECTED);
            transferService.updateTransfer(transfer);
            System.out.println("You have rejected the transfer with ID " + transfer.getTransferId());
            System.out.println();
            mainMenu();
        }

        if (isAmountInRange) {
            System.out.println("Not enough money to accept this request.");
            System.out.println("Returning to main menu...");
            mainMenu();
        }

        boolean wasAccepted = transferService.updateTransfer(transfer);

        if (!wasAccepted) {
            System.out.println("We have problems processing your request. Please try again later.");
            System.out.println();
            mainMenu();
        }

        currentAccount.sendMoney(transfer.getAmount());
        receiverAccount.receiveMoney(transfer.getAmount());

        accountService.updateAccount(currentAccount);
        accountService.updateAccount(receiverAccount);
        System.out.println("Success! Your transfer request for ID " + transfer.getTransferId() + " was processed correctly!");
        System.out.println();
        mainMenu();

    }

    //Helper method to begin a transfer process. Yet to be reviewed if this method should be in the TransferService class.
    private void beginTransferProcess(int transferType) {
        //Retrieve the user from the AuthenticatedUser to use the ID and Username if needed preventing chaining methods.
        User current = currentUser.getUser();
        //Retrieve all the existing and active users from the database and then displays the user_id and the username to the user.
        List<User> users = userService.getAllUsers();
        consoleService.displayAvailableUsers(users, current.getId());
        viewCurrentBalance();
        System.out.println();
        boolean isTransferTypeRequest = (transferType == TRANSFER_TYPE_REQUEST);

        //Sets the word of request or send with a ternary operator depending on the transfer type and prompts the user to enter the target user ID.
        String transferTypeString = (isTransferTypeRequest) ? "request" : "send";
        int targetUser = consoleService.promptForInt("Please enter the ID of the user you want to " + transferTypeString +
                " money (0 to cancel): ");

        //If user does not exist or the prompted user account is the same as the current user, returns the user to the main menu.
        if (!userService.isUserValid(targetUser, current)) {
            System.out.println();
            mainMenu();
        }

        BigDecimal amount = consoleService.promptForBigDecimal("Please enter the amount of money you want to " + transferTypeString + " (0 to cancel): ");
        //Conditional to validate if the amount entered is valid (if it is equal or less than 0 it'll return you to the main menu)
        if (!userService.isAmountValid(amount)) {
            System.out.println();
            mainMenu();
        }

        //Initialize the transfer process if all filters have passed and letting know the user what kind of process is starting depending
        //on his election.
        System.out.println((isTransferTypeRequest) ? "Requesting money..." : "Sending money...");

        //Retrieves the account of the current user account and the target user account to perform the transfer.
        Account currentAccount = accountService.getUserAccount();
        Account targetAccount = accountService.getAccount(targetUser);
        Transfer transfer = new Transfer();

        //For a request transfer, we just need to send it to the server. No more validations are needed for this process.
        //However, for sending money, we need to validate if the money in the current user account is more than what he is
        //going to send to the target account.
        if (isTransferTypeRequest) {
            //Using a helper method to set the transfer details.
            transferService.setTransferDetails(transfer, TRANSFER_TYPE_REQUEST, TRANSFER_STATUS_PENDING,
                    targetAccount.getAccountID(), currentAccount.getAccountID(), amount);
        } else {
            if (accountService.isAmountInAccountRange(amount, currentAccount.getBalance())) {
                System.out.println("Transfer was not completed. Reason: Not enough money in account.");
                mainMenu();
            }
            transferService.setTransferDetails(transfer, TRANSFER_TYPE_SEND, TRANSFER_STATUS_APPROVED,
                    currentAccount.getAccountID(), targetAccount.getAccountID(), amount);
            //Update the balance in the corresponding accounts.
            targetAccount.receiveMoney(amount);
            currentAccount.sendMoney(amount);

            //Sends the information to be updated in the server.
            accountService.updateAccount(targetAccount);
            accountService.updateAccount(currentAccount);
        }

        //We send the information to add the transfer to the database, and the server returns
        //the same transfer object with its generated ID.
        Transfer returnedTransfer = transferService.createTransfer(transfer);
        int transferId = returnedTransfer.getTransferId();

        //If transfer was not successful, the ID of the transfer is going be 0.
        if (returnedTransfer.getTransferId() == 0) {
            System.out.println("Transfer could not be completed. Try again later");
            mainMenu();
        }

        //Displaying success message for each request and send.
        if (isTransferTypeRequest) {
            consoleService.printRequestSuccess(targetUser, amount, transferId);
        } else {
            consoleService.printSendSuccess(targetUser, amount, transferId);
        }
        mainMenu();
    }
}