package pro2;

import java.util.TimeZone;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.Random;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BankAccountSimulation {
    private static final int NUM_ACCOUNTS = 2;
    private static final int NUM_DEPOSITORS = 5;
    private static final int NUM_WITHDRAWERS = 10;
    private static final int NUM_TRANSFERERS = 2;
    private static final int MAX_DEPOSIT = 600;
    private static final int MAX_WITHDRAWAL = 99;
    private static final int FLAG_DEPOSIT_THRESHOLD = 450;
    private static final int FLAG_WITHDRAWAL_THRESHOLD = 90;

    private static class Account {
        private int balance = 0;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition sufficientFunds = lock.newCondition();

        public void deposit(int amount) {
            balance += amount;
            sufficientFunds.signalAll();
        }

        public boolean withdraw(int amount) {
            if (balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        }

        public int getBalance() {
            return balance;
        }

        public ReentrantLock getLock() {
            return lock;
        }

        public Condition getSufficientFunds() {
            return sufficientFunds;
        }
    }

    private static final Account[] accounts = new Account[NUM_ACCOUNTS];
    private static int transactionCounter = 0;
    private static final Object transactionLock = new Object();

    private static final Random random = new Random();

    private static void logFlaggedTransaction(String type, int accountNum, int amount, String agentId, int transactionNumber) {
        try (FileWriter fw = new FileWriter("transactions.csv", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("EST"));
            String timestamp = sdf.format(new Date());

            String transactionType = type.equals("Deposit") ? "deposit" : "withdrawal";

            out.printf("Agent %s issued %s of $%d at: %s EST     Transaction Number: %d%n",
                    agentId, transactionType, amount, timestamp, transactionNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Depositor implements Runnable {
        private final int id;

        public Depositor(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                int accountNum = random.nextInt(NUM_ACCOUNTS);
                int amount = random.nextInt(MAX_DEPOSIT) + 1;
                Account account = accounts[accountNum];

                account.getLock().lock();
                try {
                    account.deposit(amount);
                    int currentTransactionNumber;
                    synchronized (transactionLock) {
                        transactionCounter++;
                        currentTransactionNumber = transactionCounter;
                        System.out.printf("Agent DT%-1d deposits $%-3d into JA-%-45d (+) JA-%-1d balance is $%-10d %-22s#%d%n",
                                          id, amount, accountNum, accountNum, account.getBalance(), "", transactionCounter);
                    }
                    if (amount > FLAG_DEPOSIT_THRESHOLD) {
                        System.out.println("\n***FLAGGED TRANSACTION*** Agent DT" + id + " Made a deposit in excess of $450.00 USD - See Flagged transaction log\n");
                        logFlaggedTransaction("Deposit", accountNum, amount, "DT" + id, currentTransactionNumber);
                    }
                } finally {
                    account.getLock().unlock();
                }

                try {
                    // Increasing sleep time for depositors to reduce monopolization
                    Thread.sleep(random.nextInt(1700));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static class Withdrawer implements Runnable {
        private final int id;

        public Withdrawer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                int accountNum = random.nextInt(NUM_ACCOUNTS);
                int amount = random.nextInt(MAX_WITHDRAWAL) + 1;
                Account account = accounts[accountNum];

                account.getLock().lock();
                try {
                    if (account.withdraw(amount)) {
                        int currentTransactionNumber;
                        synchronized (transactionLock) {
                            transactionCounter++;
                            currentTransactionNumber = transactionCounter;
                        }
                        System.out.printf("                              Agent WT%-1d withdraws $%-2d from JA-%-15d (-) JA-%-1d balance is $%-10d %-22s#%d%n",
                                id, amount, accountNum, accountNum, account.getBalance(), "", currentTransactionNumber);

                        if (amount > FLAG_WITHDRAWAL_THRESHOLD) {
                            System.out.println("\n***FLAGGED TRANSACTION*** Agent WT" + id + " made a withdrawal in excess of $90.00 USD - See Flagged Transaction Log.\n");
                            logFlaggedTransaction("Withdrawal", accountNum, amount, "WT" + id, currentTransactionNumber);
                        }
                    } else {
                        System.out.printf("\nAgent WT%d attempts to withdraw $%-3d from: JA-%d (******) WITHDRAWAL BLOCKED - INSUFFICIENT FUNDS!!! Balance only $%-3d%n\n",
                                id, amount, accountNum, account.getBalance());
                    }
                } finally {
                    account.getLock().unlock();
                }

                try {
                    Thread.sleep(random.nextInt(200));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static class Transferer implements Runnable {
        private final int id;

        public Transferer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                int fromAccount = random.nextInt(NUM_ACCOUNTS);
                int toAccount = (fromAccount + 1) % NUM_ACCOUNTS;
                int amount = random.nextInt(MAX_WITHDRAWAL) + 1;

                Account from = accounts[fromAccount];
                Account to = accounts[toAccount];

                while (true) {
                    boolean fromLocked = false;
                    boolean toLocked = false;
                    try {
                        fromLocked = from.getLock().tryLock();
                        if (fromLocked) {
                            toLocked = to.getLock().tryLock();
                            if (toLocked) {
                                if (from.withdraw(amount)) {
                                    to.deposit(amount);
                                    synchronized (transactionLock) {
                                        transactionCounter++;
                                        System.out.printf("\nTRANSFER --> Agent TR%-1d transferring $%-1d from JA-%-1d to JA-%-21d  -- JA-%-1d balance is now $%-28d #%d%n",
                                                id, amount, fromAccount, toAccount, fromAccount, from.getBalance() , transactionCounter);
                                        System.out.printf("TRANSFER COMPLETE --> Account JA-%-1d balance now $%-1d\n",
                                                toAccount, to.getBalance());
                                    }
                                    break;
                                } else {
                                    System.out.printf("\nAgent TR%d attempts to transfer $%-3d from: JA-%d to JA-%d (******) TRANSFER BLOCKED - INSUFFICIENT FUNDS!!! Balance only $%-3d%n\n",
                                            id, amount, fromAccount, toAccount, from.getBalance());
                                }
                            }
                        }
                    } finally {
                        if (toLocked) {
                            to.getLock().unlock();
                        }
                        if (fromLocked) {
                            from.getLock().unlock();
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                try {
                    Thread.sleep(random.nextInt(2500));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static class InternalAuditor implements Runnable {
        private int lastTransactionCount = 0;

        @Override
        public void run() {
            while (true) {
                boolean allLocked = true;
                for (Account account : accounts) {
                    if (!account.getLock().tryLock()) {
                        allLocked = false;
                        break;
                    }
                }

                if (allLocked) {
                    try {
                        int currentTransactionCount;
                        synchronized (transactionLock) {
                            currentTransactionCount = transactionCounter;
                        }
                        System.out.println("\n\n********************************************************************************************\n\n");
                        System.out.println("Internal Bank Audit Beginning...\n");
                        System.out.println("The total number of transactions since the last Internal Audit is: " + (currentTransactionCount - lastTransactionCount));
                        System.out.println("\n      INTERNAL BANK AUDITOR FINDS CURRENT ACCOUNT BALANCE FOR JA-0 TO BE: $" + accounts[0].getBalance());
                        System.out.println("      INTERNAL BANK AUDITOR FINDS CURRENT ACCOUNT BALANCE FOR JA-1 TO BE: $" + accounts[1].getBalance());
                        System.out.println("\nInternal Bank Audit complete...\n\n");
                        System.out.println("********************************************************************************************\n\n");
                        lastTransactionCount = currentTransactionCount;
                    } finally {
                        for (Account account : accounts) {
                            account.getLock().unlock();
                        }
                    }
                }

                try {
                    Thread.sleep(random.nextInt(4500));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static class TreasuryAuditor implements Runnable {
        private int lastTransactionCount = 0;

        @Override
        public void run() {
            while (true) {
                boolean allLocked = true;
                for (Account account : accounts) {
                    if (!account.getLock().tryLock()) {
                        allLocked = false;
                        break;
                    }
                }

                if (allLocked) {
                    try {
                        int currentTransactionCount;
                        synchronized (transactionLock) {
                            currentTransactionCount = transactionCounter;
                        }
                        System.out.println("\n\n********************************************************************************************\n\n");
                        System.out.println("UNITED STATES DEPARTMENT OF TREASURY - Bank audit beginning...\n");
                        System.out.println("The total number of transactions since the last Treasury Department Audit is: " + (currentTransactionCount - lastTransactionCount));
                        System.out.println("\n      TREASURY DEPT AUDITOR FINDS CURRENT ACCOUNT BALANCE FOR JA-0 TO BE: $" + accounts[0].getBalance());
                        System.out.println("      TREASURY DEPT AUDITOR FINDS CURRENT ACCOUNT BALANCE FOR JA-1 TO BE: $" + accounts[1].getBalance());
                        System.out.println("\nUNITED STATES DEPARTMENT OF TREASURY - Bank audit complete...\n\n");
                        System.out.println("********************************************************************************************\n\n");
                        lastTransactionCount = currentTransactionCount;
                    } finally {
                        for (Account account : accounts) {
                            account.getLock().unlock();
                        }
                    }
                }

                try {
                    Thread.sleep(random.nextInt(5500));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Creating a FileOutputStream
            FileOutputStream fos = new FileOutputStream("simulationOutput.txt");
            // Creating a PrintStream that will write to the file
            PrintStream filePrintStream = new PrintStream(fos);

            // Creating a custom PrintStream that writes to both console and file
            PrintStream multiOut = new PrintStream(new OutputStreamMultiplexer(System.out, filePrintStream));

            // Setting System.out to use the custom PrintStream
            System.setOut(multiOut);

            System.out.println("*** Simulation Begins...");
            System.out.printf("%-35s %-45s %-40s %-40s%n",
                    "Deposit Agents", "Withdrawal agents", "Balances", "Transaction number");
            System.out.printf("%-35s %-45s %-40s %-40s%n",
                    "----------------", "-------------------", "----------", "---------------------");

            for (int i = 0; i < NUM_ACCOUNTS; i++) {
                accounts[i] = new Account();
            }

            ExecutorService executor = Executors.newFixedThreadPool(
                    NUM_DEPOSITORS + NUM_WITHDRAWERS + NUM_TRANSFERERS + 2);

            for (int i = 0; i < NUM_DEPOSITORS; i++) {
                executor.submit(new Depositor(i));
            }
            for (int i = 0; i < NUM_WITHDRAWERS; i++) {
                executor.submit(new Withdrawer(i));
            }
            for (int i = 0; i < NUM_TRANSFERERS; i++) {
                executor.submit(new Transferer(i));
            }
            executor.submit(new InternalAuditor());
            executor.submit(new TreasuryAuditor());

            // The simulation will run indefinitely
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Custom OutputStream that writes to multiple OutputStreams
    private static class OutputStreamMultiplexer extends OutputStream {
        private final OutputStream[] outputStreams;

        public OutputStreamMultiplexer(OutputStream... outputStreams) {
            this.outputStreams = outputStreams;
        }

        @Override
        public void write(int b) throws IOException {
            for (OutputStream out : outputStreams) {
                out.write(b);
                out.flush();
            }
        }

        @Override
        public void write(byte[] b) throws IOException {
            for (OutputStream out : outputStreams) {
                out.write(b);
                out.flush();
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            for (OutputStream out : outputStreams) {
                out.write(b, off, len);
                out.flush();
            }
        }

        @Override
        public void flush() throws IOException {
            for (OutputStream out : outputStreams) {
                out.flush();
            }
        }

        @Override
        public void close() throws IOException {
            for (OutputStream out : outputStreams) {
                out.close();
            }
        }
    }
}
