package com.training.service;

import com.training.dto.AccountResponse;
import com.training.dto.CreateAccountRequest;
import com.training.entity.Account;
import com.training.entity.AccountType;
import com.training.entity.User;
import com.training.exception.BadRequestException;
import com.training.exception.ResourceNotFoundException;
import com.training.repository.AccountRepository;
import com.training.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

//    Create a new bank account and generate 10 digit account number
    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {

        User user=userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","id",userId));

        //check already have loan account
        if (request.getAccountType() == AccountType.LOAN
                        &&
                        accountRepository.existsByUserAndAccountType(user, AccountType.LOAN))
        {
            throw new BadRequestException("You already have a loan account");
        }

//      generating number
        String accountNumber=generateAccountNumber();

        Account account=Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .accountType(request.getAccountType())
                .user(user)
                .build();

        Account savedAccount=accountRepository.save(account);
        log.info("Account Created: {} for USER : {}",accountNumber,user.getEmail());

        return mapToResponse(savedAccount);
    }


//    get account by account number
    public AccountResponse getByAccountNumber(String accountNumber)
        {
            Account account =
                    accountRepository.findByAccountNumber(accountNumber)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Account",
                                            "accountNumber",
                                            accountNumber
                                    )
                            );

            return mapToResponse(account);
        }


// GEt all account of a User by ID
    public List<AccountResponse> getAccountsByUserId(Long userId) {

        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

//    Single account by its ID
    public AccountResponse getAccountById(Long accountId) {

        Account account=accountRepository.findById(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account","id",accountId));
        return mapToResponse(account);
    }

    //get all accounts--admin
    public List<AccountResponse> getAllAccounts()
    {
        return accountRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private String generateAccountNumber() {
        return String.valueOf(1000000000L+(long)(Math.random()*9000000000L));
    }

//    mapping dto
    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountHolderName(account.getUser().getName())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountType(account.getAccountType().name())
                .createdAt(account.getCreatedAt())
                .build();
    }

    //reserve account
    public AccountResponse getReserveAccount()
    {
        Account reserveAccount =
                accountRepository
                        .findByAccountNumber("UNITYBANK")
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Reserve account not found"
                                )
                        );

        return mapToResponse(reserveAccount);
    }
}
