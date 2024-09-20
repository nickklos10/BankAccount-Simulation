# Multithreaded Banking Simulation

### Project Overview

This project is a multithreaded banking system simulation designed to handle concurrent banking operations such as deposits, withdrawals, and transfers between accounts. The project leverages Java's multithreading and synchronization mechanisms to simulate real-world banking behavior with multiple agents interacting concurrently with shared resources. Additionally, internal and federal audits are conducted at random intervals to ensure the correctness of transactions. Transactions are logged, and any that violate defined thresholds are flagged for further analysis.

### Key Features

- Concurrent transactions: Agents perform deposits, withdrawals, and transfers concurrently across multiple bank accounts.
- Thread synchronization: Locks are used to ensure data consistency and prevent race conditions during concurrent access.
- Audits: Independent internal and federal audits run periodically to verify the correctness of account states.
- Transaction logging: All transactions are logged, and flagged transactions are recorded separately for auditing purposes.
- Thread pools: The simulation uses thread pools to manage agent threads efficiently.
- Retry mechanism: Agents retry acquiring locks if unavailable, ensuring eventual progress without deadlock.

### Code Structure

1. **Main Simulation Logic**
   
   At the heart of the system is the main driver class (BankAccountSimulation.java) which initializes the bank, accounts, agents (depositors, withdrawers), and audit mechanisms.
   - Bank: This class is responsible for holding the account balances and facilitating transactions.
   - Thread pool: A fixed-size thread pool ensures that multiple agents can operate concurrently without overwhelming the system.

2. **Bank and Account Class**
   
   The Bank class manages multiple bank accounts, and the Account class stores the balance for each account. Thread safety is implemented using synchronized methods or blocks to ensure that only one thread can perform operations like deposit or withdraw at a time.
   - Synchronized methods: By synchronizing the deposit, withdraw, and transfer methods, we prevent multiple threads from accessing and modifying the same account simultaneously, thus avoiding data inconsistencies.
  
3. **Transaction Logging and Flagging**
   
   Transactions are logged for audit purposes, and any transaction that violates certain predefined criteria (e.g., exceeding a threshold amount or negative balance) is flagged.
   - TransactionLogger: This class handles writing transactions to a file and flagging transactions that need special attention. Flagged transactions could involve unusual amounts or incorrect behavior, which triggers audits.

4. **Auditing Mechanism**
   
   Auditors are separate threads that run on random intervals to check the consistency of the account balances. The auditors acquire locks on all accounts before performing their checks, ensuring no transactions occur during the audit process.
   - Auditors: Auditors lock all accounts to prevent any operations while verifying the integrity of the system. They ensure that the balance totals remain consistent across all accounts and that no unauthorized transactions are present.
  
5. **Thread Pool and Locking Mechanism**
    
   A thread pool is used to manage the agents (depositors, withdrawers, auditors) to ensure efficient handling of concurrent operations. Locks are used to avoid race conditions.
   - Thread Pool: This ensures that there are a limited number of threads operating concurrently, improving performance and reducing contention.
   - Lock Retry Mechanism: If an agent cannot acquire the lock, it retries immediately to ensure continuous progress without blocking the system unnecessarily.
  
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

