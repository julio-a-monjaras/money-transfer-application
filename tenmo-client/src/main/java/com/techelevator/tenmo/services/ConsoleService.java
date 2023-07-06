package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//This is a service class. This particular one is to wrap all the displayed prompts
// and text for the frontend.
public class ConsoleService {
    private final Scanner scanner = new Scanner(System.in);

    //Prompts the user for a menu selection with a prompt as a parameter
    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);

        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    //Prints the greeting for the frontend
    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");
    }

    //Prints login menu
    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    //Prints the main menu with the current User as the parameter for a greeting.
    public void printMainMenu(User user) {
        System.out.println("Welcome, " + user.getUsername() + "!");
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your past transfers");
        System.out.println("3: View your pending requests");
        System.out.println("4: Send TE bucks");
        System.out.println("5: Request TE bucks");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printAccountBalance(Account account) {
        System.out.println("Your current account balance is: $" + account.getBalance());
    }

    //This method is used to display all the available users to do a Transfer action.
    public void displayAvailableUsers(List<User> users, int id) {
        System.out.println("------------------------------------------------------");
        System.out.printf("%17s%8s%17s%n","User ID", "||", "Username");
        System.out.println("------------------------------------------------------");
        for (User user : users) {
            if (user.getId() == id) {
                continue;
            }
            System.out.printf("%15d%10s%15s%n", user.getId(), "||", user.getUsername());
        }
        System.out.println("------------------------------------------------------");
    }

    public void printTransferHistory(List<Transfer> transfers, List<String> users, Account currentAccount) { //Add date:
        System.out.println("-------------------------------------------\n" +
                "                 Transfers\n" +
                "ID          From/To             Amount\n" +
                "-------------------------------------------");
        int i = 0;
        String fromTo = "";
        for (Transfer transfer : transfers) {
            if(((transfer.getAccountFrom() == currentAccount.getAccountID()) && transfer.getTransferTypeId() == 1) || ((transfer.getAccountTo() == currentAccount.getAccountID()) && transfer.getTransferTypeId() == 2)){
                fromTo = "From: ";
            } else{
                fromTo = "To: ";
            }
            String username = users.get(i++);
            System.out.printf("%d %16s %16s%n", transfer.getTransferId(), fromTo + username, "$" + transfer.getAmount());
        }
        System.out.println("-------------------------------------------");
    }

    public void printPendingTransfers(List<Transfer> transfers, List<String> users, Account currentAccount) {
        System.out.println("-------------------------------------------\n" +
                "              Pending Transfers\n");
        System.out.printf("ID %17s %17s%n", "From/To", "Amount");
        System.out.println("-------------------------------------------");
        int i = 0;
        String fromTo = "";
        for (Transfer transfer : transfers) {

            if(((transfer.getAccountFrom() == currentAccount.getAccountID()) && transfer.getTransferTypeId() == 1) || ((transfer.getAccountTo() == currentAccount.getAccountID()) && transfer.getTransferTypeId() == 2)){
                fromTo = "From: ";
            } else{
                fromTo = "To: ";
            }


            //String fromTo = transfer.getAccountFrom() == currentAccount.getAccountID() && transfer.getTransferTypeId() == 1 ? "From: " : "To: ";
            String username = users.get(i++);
            System.out.printf("%d %16s %16s%n", transfer.getTransferId(), fromTo + username, "$" + transfer.getAmount());
        }
        System.out.println("-------------------------------------------");
    }

    public void printTransferDetails(Transfer transfer, String userFrom, String userTo) {
        String transferType = transfer.getTransferTypeId() == 1 ? "Request" : "Send";
        String transferStatus = "";
        if (transfer.getTransferStatusId() == 1) {
            transferStatus = "Pending";
        }
        if (transfer.getTransferStatusId() == 2) {
            transferStatus = "Approved";
        }
        if (transfer.getTransferStatusId() == 3) {
            transferStatus = "Rejected";
        }

        System.out.println("Transfer Id: " + transfer.getTransferId() +
                "\nAccount From: " + userFrom +
                "\nAccount To: " + userTo +
                "\nTransfer Type: " + transferType +
                "\nTransfer Status: " + transferStatus +
                "\nAmount: $" + transfer.getAmount());
    }

    public void printPendingOptions() {
        System.out.println("---------------------\n" +
                "1: Approve\n" +
                "2: Reject\n" +
                "0: Don't approve or reject\n" +
                "---------------------\n");
    }

    //Methods for printing success on request or send transfers
    public void printRequestSuccess(int accountFrom, BigDecimal amount, int transferId) {
        System.out.println("Success! Your transfer request was sent to account " + accountFrom + " of the amount of $" + amount);
        System.out.println("You will receive a status update in your transfers history.");
        System.out.println("Your transfer ID is #" + transferId);
        System.out.println();
    }

    public void printSendSuccess(int accountTo, BigDecimal amount, int transferId) {
        System.out.println("Success! Your transfer was sent to account " + accountTo + " with the approved amount of $" + amount);
        System.out.println("Your transfer ID is #" + transferId);
        System.out.println();
    }

    //Helper methods for the console
    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username, password);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);

        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        System.out.print(prompt);
        while (true) {

            try {
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a decimal number.");
            }
        }
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printErrorMessage() {
        System.out.println("An error occurred. Check the log for details.");
    }
}