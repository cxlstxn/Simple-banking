package uk.co.asepstrath.bank;

import java.util.List;
import java.util.UUID;

public class Manager extends Account {
    private String managerId;

    public Manager(String name) {
        // Calls the parent constructor (Account)
        super(UUID.randomUUID(), name, 0, false);
        this.managerId = "MGR-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String getManagerId() {
        return managerId;
    }

    // Manager-specific function: View all accounts
    public void viewAllAccounts(List<Account> accounts) {
        System.out.println("All Accounts in the System:");
        for (Account acc : accounts) {
            System.out.println(acc.getName() + " - Balance: £" + acc.getBalance());
        }
    }

    // Manager-specific function: Approve transactions
    public void approveTransaction(Transaction transaction) {
        System.out.println("Transaction Approved: " + transaction.toString());
    }
}
