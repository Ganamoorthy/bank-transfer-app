
package com.example.bankapp.service;

import com.example.bankapp.exception.*;
import com.example.bankapp.model.*;
import com.example.bankapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    public List<Customer> getCustomers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return customerRepository.findAllOrdered();
        }
        return customerRepository.searchByName(keyword);
    }

    public BigDecimal getTotalBalance() {
        BigDecimal total = customerRepository.getTotalBalance();
        return total == null ? BigDecimal.ZERO : total;
    }

    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    public List<BankTransaction> getHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    public long countStatus(TransactionStatus status) {
        return transactionRepository.countByStatus(status);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.countAccounts(customer.getAccountNumber()) > 0) {
            throw new SameAccountException("Account number already exists");
        }

        if (customerRepository.countEmails(customer.getEmail()) > 0) {
            throw new SameAccountException("Email already exists");
        }

        return customerRepository.save(customer);
    }

    // EXPERIMENT 10:
    // Temporarily use SUPPORTS instead of REQUIRED transaction propagation.

    @Transactional(propagation = Propagation.SUPPORTS)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        if (fromAccount.equals(toAccount)) {
            throw new SameAccountException(
                    "Sender and receiver must be different accounts");
        }

        // Step 1: Validate both accounts exist
        customerRepository.getByAccountNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));

        customerRepository.getByAccountNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        // Step 2: Debit sender
        int debited = customerRepository.debit(fromAccount, amount);

        if (debited == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + fromAccount);
        }

        // Step 3: Simulate system failure
        if (simulateFailure) {
            throw new RuntimeException(
                    "Simulated system failure after the withdrawal");
        }

        // Step 4: Credit receiver
        int credited = customerRepository.credit(toAccount, amount);

        if (credited == 0) {
            throw new AccountNotFoundException(
                    "Receiver account not found during credit");
        }

        // Step 5: Save successful transaction
        BankTransaction tx = BankTransaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .description("Transfer completed")
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(tx);
    }
}
