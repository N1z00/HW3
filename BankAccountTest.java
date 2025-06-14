import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

// Custom Exception Classes
class NegativeDepositException extends Exception {
    public NegativeDepositException(String message) {
        super(message);
    }
}

class OverdrawException extends Exception {
    public OverdrawException(String message) {
        super(message);
    }
}

class InvalidAccountOperationException extends Exception {
    public InvalidAccountOperationException(String message) {
        super(message);
    }
}

// Observer Pattern - Define Observer Interface
interface Observer {
    void update(String message);
}

// TransactionLogger class (Concrete Observer)
class TransactionLogger implements Observer {
    @Override
    public void update(String message) {
        System.out.println("Transaction Log: " + message);
        
        // Extra Credit: Write to file
        try (FileWriter writer = new FileWriter("transaction_log.txt", true)) {
            writer.write("Transaction Log: " + message + "\n");
        } catch (IOException e) {
            System.out.println("Error writing to log file: " + e.getMessage());
        }
    }
}

// BankAccount (Subject in Observer Pattern)
class BankAccount {
    protected String accountNumber;
    protected double balance;
    protected boolean isActive;
    private List<Observer> observers = new ArrayList<>();

    public BankAccount(String accNum, double initialBalance) {
        this.accountNumber = accNum;
        this.balance = initialBalance;
        this.isActive = true;
    }

    // Attach observer
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    // Notify observers
    private void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public void deposit(double amount) throws NegativeDepositException, InvalidAccountOperationException {
        // Check if account is active
        if (!isActive) {
            throw new InvalidAccountOperationException("Cannot deposit to a closed account");
        }
        
        // Check for negative deposit
        if (amount < 0) {
            throw new NegativeDepositException("Cannot deposit negative amount: $" + amount);
        }
        
        balance += amount;
        notifyObservers("Deposited $" + amount + " to account " + accountNumber + ". New balance: $" + balance);
    }

    public void withdraw(double amount) throws OverdrawException, InvalidAccountOperationException {
        // Check if account is active
        if (!isActive) {
            throw new InvalidAccountOperationException("Cannot withdraw from a closed account");
        }
        
        // Check for overdraw
        if (amount > balance) {
            throw new OverdrawException("Insufficient funds. Balance: $" + balance + ", Withdrawal: $" + amount);
        }
        
        balance -= amount;
        notifyObservers("Withdrew $" + amount + " from account " + accountNumber + ". New balance: $" + balance);
    }

    public double getBalance() {
        return balance;
    }

    public void closeAccount() {
        isActive = false;
        notifyObservers("Account " + accountNumber + " has been closed");
    }
}

// Decorator Pattern - Define BankAccountDecorator Class
abstract class BankAccountDecorator extends BankAccount {
    protected BankAccount decoratedAccount;

    public BankAccountDecorator(BankAccount account) {
        super(account.accountNumber, account.getBalance());
        this.decoratedAccount = account;
    }
}

// SecureBankAccount (Concrete Decorator)
class SecureBankAccount extends BankAccountDecorator {
    private static final double MAX_WITHDRAWAL_LIMIT = 500.0;
    private String pin;

    public SecureBankAccount(BankAccount account) {
        super(account);
        this.pin = "1234"; // Default PIN for extra credit
    }

    public SecureBankAccount(BankAccount account, String pin) {
        super(account);
        this.pin = pin;
    }

    @Override
    public void deposit(double amount) throws NegativeDepositException, InvalidAccountOperationException {
        decoratedAccount.deposit(amount);
        this.balance = decoratedAccount.getBalance();
    }

    @Override
    public void withdraw(double amount) throws OverdrawException, InvalidAccountOperationException {
        // Security rule: limit withdrawal to $500 per transaction
        if (amount > MAX_WITHDRAWAL_LIMIT) {
            throw new OverdrawException("Security limit exceeded. Maximum withdrawal per transaction: $" + MAX_WITHDRAWAL_LIMIT);
        }
        
        decoratedAccount.withdraw(amount);
        this.balance = decoratedAccount.getBalance();
    }

    // Extra Credit: PIN verification
    public boolean verifyPin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    @Override
    public void addObserver(Observer observer) {
        decoratedAccount.addObserver(observer);
    }

    @Override
    public void closeAccount() {
        decoratedAccount.closeAccount();
        this.isActive = false;
    }
}

// Main Program
public class BankAccountTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Ask the user to enter an initial balance and create a BankAccount object
            System.out.print("Enter initial balance: ");
            double initialBalance = scanner.nextDouble();
            BankAccount account = new BankAccount("123456", initialBalance);
            System.out.println("Bank Account Created: #123456");

            // Create a TransactionLogger and attach it to the account
            TransactionLogger logger = new TransactionLogger();
            account.addObserver(logger);

            // Wrap account in SecureBankAccount decorator
            SecureBankAccount secureAccount = new SecureBankAccount(account);
            secureAccount.addObserver(logger);

            // Allow the user to enter deposit and withdrawal amounts
            System.out.print("Enter deposit amount: ");
            double depositAmount = scanner.nextDouble();
            secureAccount.deposit(depositAmount);

            System.out.print("Enter withdrawal amount: ");
            double withdrawalAmount = scanner.nextDouble();
            secureAccount.withdraw(withdrawalAmount);

            // Display the final balance
            System.out.println("Final balance: $" + secureAccount.getBalance());

            // Test exception handling
            System.out.println("\n--- Testing Exception Handling ---");
            
            // Test negative deposit
            try {
                secureAccount.deposit(-50);
            } catch (NegativeDepositException e) {
                System.out.println("Caught exception: " + e.getMessage());
            }

            // Test overdraw
            try {
                secureAccount.withdraw(secureAccount.getBalance() + 100);
            } catch (OverdrawException e) {
                System.out.println("Caught exception: " + e.getMessage());
            }

            // Test security limit
            try {
                secureAccount.withdraw(600);
            } catch (OverdrawException e) {
                System.out.println("Caught exception: " + e.getMessage());
            }

            // Test closed account operations
            secureAccount.closeAccount();
            try {
                secureAccount.deposit(100);
            } catch (InvalidAccountOperationException e) {
                System.out.println("Caught exception: " + e.getMessage());
            }

        } catch (Exception e) {
            // Catch and handle exceptions properly
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}