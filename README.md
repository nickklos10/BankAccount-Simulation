# Synchronized & Cooperating Threads – A Banking Simulation

### Project Overview

This project is a multithreaded banking system simulation designed to handle concurrent banking operations such as deposits, withdrawals, and transfers between accounts. The project leverages Java's multithreading and synchronization mechanisms to simulate real-world banking behavior with multiple agents interacting concurrently with shared resources. Additionally, internal and federal audits are conducted at random intervals to ensure the correctness of transactions. Transactions are logged, and any that violate defined thresholds are flagged for further analysis.

### Key Features

- Concurrent Transactions: Agents perform deposits, withdrawals, and transfers concurrently across multiple bank accounts.
- Thread Synchronization: Locks are managed within the account methods to ensure data consistency and prevent race conditions during concurrent access. Condition variables are utilized to coordinate between threads, allowing withdrawal agents to wait for sufficient funds.
- Audits: Independent internal and federal audits run periodically to verify the correctness of account states.
- Transaction Logging: All transactions are logged, and flagged transactions that exceed predefined thresholds are recorded separately in a CSV file (transactions.csv) for auditing purposes.
- Thread Pools: The simulation uses thread pools to manage agent threads efficiently.
- Waiting Mechanism for Withdrawals: Withdrawal agents now wait for sufficient funds using condition variables, ensuring they can perform withdrawals effectively when funds become available.
- Deadlock Prevention: Consistent lock ordering and proper use of condition variables prevent deadlocks in the system.

### Code Structure

1. **Main Simulation Logic**
   
   At the heart of the system is the main driver class (BankAccountSimulation.java), which initializes the bank accounts, agents (depositors, withdrawers, transferers), and audit mechanisms.

   - Bank Accounts: Each account is responsible for holding its balance and facilitating transactions through synchronized methods.
   - Thread Pool: A fixed-size thread pool ensures that multiple agents can operate concurrently without overwhelming the system.

2. **Bank and Account Class**
   
   The Account class manages individual bank accounts. Thread safety is implemented by moving the locking mechanism inside the account methods (deposit, withdraw, and transfer).

   - Synchronized Methods with Locks: By using locks within the account methods, we prevent multiple threads from accessing and modifying the same account simultaneously, thus avoiding data      inconsistencies.
   - Condition Variables: The Condition object is used in the withdraw method to allow withdrawal agents to wait for sufficient funds rather than giving up immediately when funds are             insufficient.
  
3. **Transaction Logging and Flagging**
   
   Transactions are logged for audit purposes, and any transaction that violates certain predefined criteria (e.g., exceeding a threshold amount) is flagged.

   - CSV Logging: Flagged transactions are now recorded separately in a CSV file (transactions.csv), providing a structured and easily accessible format for auditing.

   - TransactionLogger: This component handles writing transactions to the CSV file and flagging transactions that need special attention. Flagged transactions involve unusual amounts,           which triggers audits.

4. **Auditing Mechanism**
   
   Auditors are separate threads that run at random intervals to check the consistency of the account balances. The auditors acquire locks on all accounts before performing their checks,       ensuring no transactions occur during the audit process.

   - Auditors: Auditors lock all accounts to prevent any operations while verifying the integrity of the system. They ensure that the balance totals remain consistent across all accounts         and that no unauthorized transactions are present.

   - Lock Ordering: To prevent deadlocks during audits, locks on accounts are acquired in a consistent order.
  
5. **Thread Pool and Locking Mechanism**
    
   A thread pool is used to manage the agents (depositors, withdrawers, transferers, auditors) to ensure efficient handling of concurrent operations. Locks and condition variables are used     to avoid race conditions and manage thread synchronization.

   - Thread Pool: This ensures that there are a limited number of threads operating concurrently, improving performance and reducing contention.

   - Waiting Mechanism with Condition Variables: Withdrawal agents utilize condition variables to wait for sufficient funds, ensuring they can perform withdrawals effectively when funds          become available.

   Deadlock Prevention: By acquiring locks in a consistent order and using condition variables appropriately, the system prevents deadlocks and ensures continuous progress.
  
## How to Run

1. Install Java Development Kit (JDK): Ensure that JDK 8 or higher is installed on your system.
   
2. Compile the project:
```
javac BankAccountSimulation.java
```

3. Run the project:
```
java BankAccountSimulation
```

Upon running the simulation, the system will start processing transactions across multiple bank accounts concurrently, logging all activities, and performing periodic audits. You can monitor the output in the console or review the log files generated during the simulation.


### Conclusion

This Multithreaded Banking Simulation provides a robust platform for simulating and analyzing concurrent financial operations in a controlled environment. By using thread pools, synchronization, and auditing mechanisms, the project ensures consistency and correctness in all banking activities while enabling high levels of concurrency.

Feel free to explore and modify the codebase to suit your specific needs or integrate additional features like security measures or fraud detection algorithms.

