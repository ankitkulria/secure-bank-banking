package com.training.service;

import com.training.Util.LoanUtil;
import com.training.dto.*;
import com.training.entity.*;
import com.training.exception.BadRequestException;
import com.training.exception.ResourceNotFoundException;
import com.training.repository.AccountRepository;
import com.training.repository.LoanRepository;
import com.training.repository.TransactionRepository;
import com.training.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;

import static org.springframework.core.annotation.OrderUtils.getPriority;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanUtil loanUtil;
    private final EmailService emailService;

//    Calculate EMI, total payment, and total interest for loan
        public LoanCalculationResponse calculateEmi(LoanCalculationRequest request)
        {
            BigDecimal principal = request.getPrincipal();
            BigDecimal annualRate = request.getAnnualRate();
            int tenureMonths=request.getTenureMonths();

//            Monthly Interest rate= annualrate/12/100
            BigDecimal monthlyRate=annualRate
                    .divide(BigDecimal.valueOf(12),10, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(100),10,RoundingMode.HALF_UP);

//            EMI formula: P x R x(1+r)^n/((1+r)^n-1)
            BigDecimal onePlusR= BigDecimal.ONE.add(monthlyRate);
            BigDecimal power=onePlusR.pow(tenureMonths, MathContext.DECIMAL128);

            BigDecimal numerator=principal.multiply(monthlyRate).multiply(power);
            BigDecimal denominator=power.subtract(BigDecimal.ONE);

            BigDecimal emi=numerator.divide(denominator,2,RoundingMode.HALF_UP);
            BigDecimal totalPayment= emi.multiply(BigDecimal.valueOf(tenureMonths))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalInterest=totalPayment.subtract(principal)
                    .setScale(2, RoundingMode.HALF_UP);

            return LoanCalculationResponse.builder()
                    .emi(emi)
                    .totalPayment(totalPayment)
                    .totalInterest(totalInterest)
                    .build();
        }

        //apply loan
        public void applyLoan(
                LoanApplicationRequest request,
                String email)
        {
            // find logged in user
            User user = userRepository
                            .findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // User can have only one pending request
            boolean pendingLoanExists = loanRepository.existsByUserAndStatus(user, LoanStatus.PENDING);

            if (pendingLoanExists)
            {
                throw new BadRequestException("You already have a pending loan request");
            }

            boolean activeLoanExists =
                    loanRepository.existsByUserAndStatus(user, LoanStatus.ACTIVE);

            if (activeLoanExists)
            {
                throw new BadRequestException(
                        "You already have an active loan. Please close your current loan before applying for a new one.");
            }

            // find loan account
            Account loanAccount = accountRepository
                    .findByAccountNumber(request.getLoanAccountNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found"));

            //check loan account type
            if (loanAccount.getAccountType() != AccountType.LOAN)
            {
                throw new BadRequestException("Please provide a valid loan account");
            }
            //loan account ownership check
            if (!loanAccount.getUser().getId().equals(user.getId()))
            {
                throw new BadRequestException("You are not owner of this loan account");
            }

            // find  repayment account with account number
            Account repaymentAccount = accountRepository
                            .findByAccountNumber(request.getRepaymentAccountNumber())
                            .orElseThrow(() -> new ResourceNotFoundException("Repayment Account not found"));

            // check if account is eligible for repayment or not
            if (repaymentAccount.getAccountType() == AccountType.LOAN)
            {
                throw new BadRequestException("Loan account cannot be used for repayment");
            }

            // Check ownership of account
            if (!repaymentAccount.getUser().getId().equals(user.getId()))
            {
                throw new BadRequestException("You are not owner of this account");
            }

            // Interest Rate
            BigDecimal interestRate = loanUtil.getInterestRate(request.getLoanAmount());

            // Loan Object
            Loan loan = Loan.builder()
                            .user(user)
                            .loanAccount(loanAccount)
                            .repaymentAccount(repaymentAccount)
                            .loanAmount(request.getLoanAmount())
                            .remainingAmount(request.getLoanAmount())
                            .interestRate(interestRate)
                            .durationMonths(request.getDurationMonths())
                            .purpose(request.getPurpose())
                            .monthlyEmi(BigDecimal.ZERO)
                            .status(LoanStatus.PENDING)
                            .appliedAt(LocalDateTime.now())
                            .build();
            // Save
            loanRepository.save(loan);
        }

        //loan response
        public List<LoanResponse> getPendingLoans()
        {
            return loanRepository.findByStatus(LoanStatus.PENDING)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

    private LoanResponse mapToResponse(Loan loan)
    {
        return LoanResponse.builder()
                .loanId(loan.getId())
                .customerName(loan.getUser().getName())
                .customerEmail(loan.getUser().getEmail())
                .loanAccountNumber(loan.getLoanAccount().getAccountNumber())
                .repaymentAccountNumber(loan.getRepaymentAccount().getAccountNumber())
                .loanAmount(loan.getLoanAmount())
                .interestRate(loan.getInterestRate())
                .durationMonths(loan.getDurationMonths())
                .purpose(loan.getPurpose())
                .status(loan.getStatus())
                .build();
    }

    //approve loan
    @Transactional
    public void approveLoan(Long loanId)
    {
        Loan loan=loanRepository.findById(loanId)
                .orElseThrow(()->new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING)
        {
            throw new BadRequestException("Only pending loans can be approved");
        }

        boolean activeLoanExists =
                loanRepository.existsByUserAndStatus(loan.getUser(), LoanStatus.ACTIVE);

        if(activeLoanExists)
        {
            throw new BadRequestException("User already has an active loan");
        }

        Account loanAccount=loan.getLoanAccount();

        Account bankReserve=accountRepository.findByAccountNumber("UNITYBANK")
                .orElseThrow(()->new ResourceNotFoundException("Bank Reserve Account not Found"));

        if (bankReserve.getBalance().compareTo(loan.getLoanAmount()) < 0)
        {
            throw new BadRequestException("Bank itself is running out of Money");
        }

        bankReserve.setBalance(bankReserve.getBalance().subtract(loan.getLoanAmount()));
        loanAccount.setBalance(loanAccount.getBalance().add(loan.getLoanAmount()));

        accountRepository.save(bankReserve);
        accountRepository.save(loanAccount);

        Transaction transaction = Transaction.builder()
                        .type(TransactionType.LOAN_DISBURSEMENT)
                        .amount(loan.getLoanAmount())
                        .balanceAfter(loanAccount.getBalance())
                        .description("Loan disbursement for Loan ID " + loan.getId())
                        .sourceAccount(bankReserve)
                        .targetAccount(loanAccount)
                        .build();
        transactionRepository.save(transaction);

        // Log transaction for target account (credit)
        Transaction creditTransaction = Transaction.builder()
                .type(TransactionType.LOAN_DISBURSEMENT)
                .amount(loan.getLoanAmount())
                .balanceAfter(loanAccount.getBalance())
                .description("Loan disbursement from" + bankReserve.getAccountNumber())
                .sourceAccount(loanAccount)
                .targetAccount(bankReserve)
                .build();
        transactionRepository.save(creditTransaction);

        LoanCalculationRequest emiRequest =
                new LoanCalculationRequest(
                        loan.getLoanAmount(),
                        loan.getInterestRate(),
                        loan.getDurationMonths()
                );

        LoanCalculationResponse emiResponse = calculateEmi(emiRequest);

        loan.setMonthlyEmi(emiResponse.getEmi());
        loan.setRemainingAmount(emiResponse.getTotalPayment());
        loan.setApprovedAt(LocalDateTime.now());
        loan.setNextDueDate(LocalDate.now().plusMonths(1));
        loan.setStatus(LoanStatus.ACTIVE);

        loanRepository.save(loan);

        sendApprovalEmail(loan,emiResponse);
    }

    //mail for approval
    private void sendApprovalEmail(
            Loan loan,
            LoanCalculationResponse emiResponse
    )
    {
        String subject = "Loan Application Approved";

        String body = "Dear "
                        + loan.getUser().getName()
                        + ",\n\n"
                        + "Congratulations! Your loan application has been approved.\n\n"
                        + "Loan Amount: ₹"
                        + loan.getLoanAmount()
                        + "\nInterest Rate: "
                        + loan.getInterestRate()
                        + "%"
                        + "\nMonthly EMI: ₹"
                        + emiResponse.getEmi()
                        + "\nDuration: "
                        + loan.getDurationMonths()
                        + " Months"
                        + "\nTotal Interest: ₹"
                        + emiResponse.getTotalInterest()
                        + "\nTotal Amount To Be Paid: ₹"
                        + emiResponse.getTotalPayment()
                        + "\nNext EMI Due Date: "
                        + loan.getNextDueDate()
                        + "\n\nRegards,\n"
                        + "Banking Management System";

        emailService.sendEmail(
                loan.getUser().getEmail(),
                subject,
                body
        );
    }

    //reject loan
    public void rejectLoan(Long loanId)
    {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING)
        {
            throw new BadRequestException("Only pending loans can be rejected");
        }

        loan.setStatus(LoanStatus.REJECTED);

        loanRepository.save(loan);

        sendRejectionEmail(loan);
    }

    // mail for rejection
    private void sendRejectionEmail(Loan loan)
    {
        String subject = "Loan Application Rejected";

        String body =
                "Dear "
                        + loan.getUser().getName()
                        + ",\n\n"
                        + "We regret to inform you that your loan application has been rejected.\n\n"
                        + "You may apply again later.\n\n"
                        + "Regards,\n"
                        + "Banking Management System";

        emailService.sendEmail(
                loan.getUser().getEmail(),
                subject,
                body
        );
    }

    //show loan in user dashboard
    public List<LoanDetailsResponse> getMyLoans(String email)
    {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found"));

        return loanRepository
                .findByUser(user)
                .stream()

                // hide rejected
                .filter(loan -> loan.getStatus() != LoanStatus.REJECTED)

                // ACTIVE -> PENDING -> CLOSED
                .sorted(
                        (a, b) ->
                                getPriority(a.getStatus())
                                        - getPriority(
                                        b.getStatus()))
                .map(
                        loan ->
                                LoanDetailsResponse
                                        .builder()
                                        .loanId(loan.getId())
                                        .loanAmount(loan.getLoanAmount())
                                        .remainingAmount(loan.getRemainingAmount())
                                        .monthlyEmi(loan.getMonthlyEmi())
                                        .interestRate(loan.getInterestRate())
                                        .nextDueDate(loan.getNextDueDate())
                                        .status(loan.getStatus())
                                        .durationMonths(loan.getDurationMonths())
                                        .purpose(loan.getPurpose())
                                        .build()
                )
                .toList();
    }

    private int getPriority(LoanStatus status)
    {
        return switch (status)
        {
            case ACTIVE -> 1;
            case PENDING -> 2;
            case CLOSED -> 3;
            default -> 99;
        };
    }

    //pay emi
    @Transactional
    public void payEmi(String email)
    {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Loan loan = loanRepository
                .findByUserAndStatus(user, LoanStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active loan found"));

        Account repaymentAccount = loan.getRepaymentAccount();

        Account bankReserve =
                accountRepository
                        .findByAccountNumber("UNITYBANK")
                        .orElseThrow(() -> new ResourceNotFoundException("Bank reserve account not found"));

        BigDecimal emi = loan.getMonthlyEmi();

        // Balance Check
        if (repaymentAccount.getBalance().compareTo(emi) < 0)
        {
            throw new BadRequestException("Insufficient balance to pay EMI");
        }

        // Deduct from repayment account
        repaymentAccount.setBalance(repaymentAccount.getBalance().subtract(emi));

        // Credit bank reserve
        bankReserve.setBalance(bankReserve.getBalance().add(emi));
        accountRepository.save(repaymentAccount);
        accountRepository.save(bankReserve);

        // Repayment Account Transaction
        Transaction emiPayment =
                Transaction.builder()
                        .type(TransactionType.EMI_PAYMENT)
                        .amount(emi)
                        .balanceAfter(repaymentAccount.getBalance())
                        .description("Loan EMI Payment")
                        .sourceAccount(repaymentAccount)
                        .targetAccount(bankReserve)
                        .build();
        transactionRepository.save(emiPayment);

        // Bank Reserve Transaction
        Transaction emiReceived =
                Transaction.builder()
                        .type(TransactionType.EMI_RECEIVED)
                        .amount(emi)
                        .balanceAfter(bankReserve.getBalance())
                        .description("Loan EMI Received")
                        .sourceAccount(bankReserve)
                        .targetAccount(repaymentAccount)
                        .build();
        transactionRepository.save(emiReceived);

        // Remaining Amount Update
        BigDecimal remainingAmount = loan.getRemainingAmount().subtract(emi);
        loan.setRemainingAmount(remainingAmount);

        // Loan Closed?
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setRemainingAmount(BigDecimal.ZERO);
            sendLoanClosedEmail(loan);
            loan.setNextDueDate(null);
        }
        else
        {
            loan.setNextDueDate(loan.getNextDueDate().plusMonths(1));
        }
        loanRepository.save(loan);
    }

    //mail for closed loan
    private void sendLoanClosedEmail(Loan loan)
    {
        String subject = "Loan Closed Successfully";
        String body =
                "Dear "
                        + loan.getUser().getName()
                        + ",\n\n"
                        + "Congratulations! "
                        + "Your loan has been fully repaid "
                        + "and is now closed.\n\n"
                        + "Loan Amount: ₹"
                        + loan.getLoanAmount()
                        + "\n\n"
                        + "Thank you for banking with us.\n\n"
                        + "Regards,\n"
                        + "Banking Management System";

        emailService.sendEmail(
                loan.getUser().getEmail(),
                subject,
                body
        );
    }

    public List<LoanResponse> getAllLoans()
    {
        return loanRepository
                .findAll()
                .stream()
                .filter(loan -> loan.getStatus() != LoanStatus.REJECTED)
                .map(this::mapToResponse)
                .toList();
    }

    public long getActiveLoanCount()
    {
        return loanRepository.countByStatus(
                LoanStatus.ACTIVE
        );
    }
}
