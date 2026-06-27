package com.training.service;

import com.training.dto.DepositRequest;
import com.training.dto.TransactionResponse;
import com.training.dto.TransferRequest;
import com.training.dto.WithdrawRequest;
import com.training.entity.Account;
import com.training.entity.Transaction;
import com.training.entity.TransactionType;
import com.training.exception.BadRequestException;
import com.training.exception.InsufficientBalanceException;
import com.training.exception.ResourceNotFoundException;
import com.training.repository.AccountRepository;
import com.training.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

//    Deposit money into account
    @Transactional
    public TransactionResponse deposit(Long userId, DepositRequest request)
    {
        Account account=getAccountAndValidateOwnership(request.getAccountId(),userId);

//        Credit the amount
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

//        Log the transaction
        Transaction transaction=Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceAfter(account.getBalance())
                .description("Deposit of ₹"+ request.getAmount())
                .sourceAccount(account)
                .build();

        Transaction saved=transactionRepository.save(transaction);
        log.info("Deposit: ₹{} to account {}", request.getAmount(), account.getAccountNumber());

        return mapToResponse(saved);
    }

//    //Withdraw money from account- must have sufficient balance
    @Transactional
    public TransactionResponse withdraw(Long userId, WithdrawRequest request)
    {
        Account account= getAccountAndValidateOwnership(request.getAccountId(),userId);

        //check balance
        if(account.getBalance().compareTo(request.getAmount())<0){
            throw new InsufficientBalanceException(account.getAccountNumber());
        }

        //DEbit amount
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        //log the transaction
        Transaction transaction=Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceAfter(account.getBalance())
                .balanceAfter(account.getBalance())
                .description("Withdrawal of ₹" + request.getAmount())
                .sourceAccount(account)
                .build();
        Transaction saved = transactionRepository.save(transaction);
        log.info("Withdrawal: ₹{} from account {}", request.getAmount(), account.getAccountNumber());

        return mapToResponse(saved);
    }

    //transfer between  two accounts
    @Transactional
    public TransactionResponse transfer(Long userId, TransferRequest request)
    {
        if(request.getFromAccountId().equals(request.getToAccountId())){
            throw new BadRequestException("Can not transfer to same account");
        }

        Account sourceAccount=getAccountAndValidateOwnership(request.getFromAccountId(),userId);
        Account targetAccount=accountRepository.findById(request.getToAccountId())
                .orElseThrow(()->new ResourceNotFoundException("Account","id",request.getToAccountId()));

        //check balance
        if(sourceAccount.getBalance().compareTo(request.getAmount())<0){
            throw new InsufficientBalanceException(sourceAccount.getAccountNumber());
        }

        //debit and credit
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        //log transaction for source account(Debit)
        Transaction debitTransaction=Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .balanceAfter(sourceAccount.getBalance())
                .description("Transfer to account: "+targetAccount.getAccountNumber())
                .sourceAccount(sourceAccount)
                .targetAccount(targetAccount)
                .build();

        // Log transaction for target account (credit)
        Transaction creditTransaction = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .balanceAfter(targetAccount.getBalance())
                .description("Transfer from account " + sourceAccount.getAccountNumber())
                .sourceAccount(targetAccount)
                .targetAccount(sourceAccount)
                .build();

        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);

        log.info("Transfer: ₹{} from {} to {}",
                request.getAmount(),
                sourceAccount.getAccountNumber(),
                targetAccount.getAccountNumber());

        return mapToResponse(debitTransaction);
    }

    //pagination account history
    public Page<TransactionResponse> getTransactionHistory(Long accountId, Pageable pageable) {
        // Verify account exists
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account", "id", accountId);
        }

        return transactionRepository.findBySourceAccountId(accountId, pageable)
                .map(this::mapToResponse);
    }

    //all transaction non paginated
    public List<TransactionResponse> getAllTransactions(Long accountId) {
        return transactionRepository.findBySourceAccountIdOrderByTimeStampDesc(accountId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //fetch account and validate
    private Account getAccountAndValidateOwnership(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new BadRequestException("Account does not belong to the authenticated user");
        }

        return account;
    }
    /**
     * Map Transaction entity → TransactionResponse DTO.
     */
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .timeStamp(transaction.getTimeStamp())
                .build();
    }
}
