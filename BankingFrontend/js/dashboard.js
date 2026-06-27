
let currentTransactionPage = 0;

let currentTransactionAccountId = null;

let currentTransactionAccountNumber = null;

let selectedAccountId = null;

let userAccounts = [];

let loanAccountNumber = null;
// ====================================
// AUTH
// ====================================

const token = localStorage.getItem("token");

if (!token) {
    window.location.href = "login.html";
}

const userName = localStorage.getItem("name");

document.getElementById(
    "userEmail"
).innerText = `Hello, ${userName}`;

document
    .getElementById("logoutBtn")
    .addEventListener("click", () => {

        localStorage.clear();

        window.location.href = "login.html";
    });

// ====================================
// TOAST
// ====================================

function showToast(message, type = "success") {

    const toastContainer =
        document.getElementById("toastContainer");

    const toastId =
        "toast-" + Date.now();

    toastContainer.innerHTML += `

        <div
            id="${toastId}"
            class="toast toast-${type}"
            role="alert"
        >

            <div class="toast-body">

                ${message}

            </div>

        </div>
    `;

    const toastElement =
        document.getElementById(toastId);

    const toast =
        new bootstrap.Toast(
            toastElement,
            {
                delay: 3000
            }
        );

    toast.show();

    toastElement.addEventListener(
        "hidden.bs.toast",
        () => toastElement.remove()
    );
}

// ====================================
// CREATE ACCOUNT
// ====================================

document
    .getElementById("createAccountForm")
    .addEventListener(
        "submit",
        async (event) => {

            event.preventDefault();

            const accountType =
                document.getElementById(
                    "accountType"
                ).value;

            try {

                const response =
                    await fetch(
                        "http://localhost:8080/api/accounts",
                        {

                            method: "POST",

                            headers: {

                                "Content-Type":
                                    "application/json",

                                "Authorization":
                                    `Bearer ${token}`
                            },

                            body: JSON.stringify({
                                accountType
                            })
                        }
                    );

                const data =
                    await response.json();

                if (data.success) {

                    showToast(
                        "Account created successfully",
                        "success"
                    );

                    loadAccounts();

                    bootstrap.Modal
                        .getInstance(
                            document.getElementById(
                                "createAccountModal"
                            )
                        )
                        .hide();
                }
                else {

                    showToast(
                        data.message,
                        "danger"
                    );
                }

            }
            catch (error) {

                console.error(error);

                showToast(
                    "Failed to create account",
                    "danger"
                );
            }
        }
    );

// ====================================
// LOAD ACCOUNTS
// ====================================

async function loadAccounts() {

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/accounts",
                {

                    headers: {

                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {
            userAccounts = data.data;

            renderAccounts(data.data);

            populateLoanAccounts();
        }

    }
    catch (error) {

        console.error(error);
    }
}


function populateLoanAccounts() {
    const repaymentSelect =
        document.getElementById(
            "repaymentAccount"
        );

    repaymentSelect.innerHTML = "";

    const loanAccount =
        userAccounts.find(
            account =>
                account.accountType === "LOAN"
        );

    if (loanAccount) {
        loanAccountNumber =
            loanAccount.accountNumber;
    }

    userAccounts
        .filter(
            account =>
                account.accountType !== "LOAN"
        )
        .forEach(account => {
            repaymentSelect.innerHTML += `
                <option
                    value="${account.accountNumber}">
                    ${account.accountType}
                    -
                    ${account.accountNumber}
                </option>
            `;
        });
}


document
    .getElementById("applyLoanForm")
    .addEventListener(
        "submit",
        applyLoan
    );

async function applyLoan(event) {
    event.preventDefault();

    try {
        const response =
            await fetch(
                "http://localhost:8080/api/loans/apply",
                {
                    method: "POST",

                    headers:
                    {
                        "Content-Type":
                            "application/json",

                        "Authorization":
                            `Bearer ${token}`
                    },

                    body: JSON.stringify(
                        {
                            loanAccountNumber:
                                loanAccountNumber,

                            loanAmount:
                                document
                                    .getElementById(
                                        "loanAmount"
                                    )
                                    .value,

                            durationMonths:
                                document
                                    .getElementById(
                                        "durationMonths"
                                    )
                                    .value,

                            purpose:
                                document
                                    .getElementById(
                                        "purpose"
                                    )
                                    .value,

                            repaymentAccountNumber:
                                document
                                    .getElementById(
                                        "repaymentAccount"
                                    )
                                    .value
                        })
                }
            );

        const data =
            await response.json();

        if (data.success) {
            showToast(
                "Loan applied successfully",
                "success"
            );

            bootstrap.Modal
                .getInstance(
                    document.getElementById(
                        "applyLoanModal"
                    )
                )
                .hide();

            loadMyLoans();
        }
        else {
            showToast(
                data.message,
                "danger"
            );
        }
    }
    catch (error) {
        console.error(error);

        showToast(
            "Loan application failed",
            "danger"
        );
    }
}
// ====================================
// RENDER ACCOUNTS
// ====================================

function renderAccounts(accounts) {

    const container =
        document.getElementById(
            "accountsContainer"
        );

    container.innerHTML = "";

    if (accounts.length === 0) {

        container.innerHTML = `

            <div class="col-12">

                <div class="alert alert-info">

                    No accounts found

                </div>

            </div>
        `;

        return;
    }

    accounts.forEach(account => {

        let cardClass =
            "savings-card";

        if (
            account.accountType ===
            "CURRENT"
        ) {

            cardClass =
                "current-card";
        }

        else if (
            account.accountType ===
            "LOAN"
        ) {

            cardClass =
                "loan-card";
        }

        container.innerHTML += `

            <div class="col-lg-4">

                <div class="account-card ${cardClass}">

                    <div class="account-type">

                        ${account.accountType}

                    </div>

                    <div class="balance">

                        ₹${account.balance}

                    </div>

                    <div class="account-number mb-3">

                        Account No:
                        ${account.accountNumber}

                    </div>

                    <div class="action-buttons">

                        <button
                            class="btn btn-success btn-sm"
                            onclick="openDepositModal(${account.id})"
                        >
                            Deposit
                        </button>

                        <button
                            class="btn btn-warning btn-sm"
                            onclick="openWithdrawModal(${account.id})"
                        >
                            Withdraw
                        </button>

                        <button
                            class="btn btn-dark btn-sm"
                            onclick="openTransferModal(${account.id})"
                        >
                            Transfer
                        </button>

                        <button
                            class="btn btn-info btn-sm"
                            onclick="loadTransactions(
                                ${account.id},
                                '${account.accountNumber}'
                            )"
                        >
                            History
                        </button>

                    </div>

                </div>

            </div>
        `;
    });
}

// ====================================
// MODALS
// ====================================

function openDepositModal(accountId) {

    document.getElementById(
        "depositAccountId"
    ).value = accountId;

    new bootstrap.Modal(
        document.getElementById(
            "depositModal"
        )
    ).show();
}

function openWithdrawModal(accountId) {

    document.getElementById(
        "withdrawAccountId"
    ).value = accountId;

    new bootstrap.Modal(
        document.getElementById(
            "withdrawModal"
        )
    ).show();
}

function openTransferModal(accountId) {

    document.getElementById(
        "fromAccountId"
    ).value = accountId;

    document.getElementById(
        "transferBtn"
    ).disabled = true;

    document.getElementById(
        "accountInfo"
    ).classList.add("d-none");

    new bootstrap.Modal(
        document.getElementById(
            "transferModal"
        )
    ).show();
}

// ====================================
// VERIFY ACCOUNT
// ====================================

document
    .getElementById(
        "verifyAccountBtn"
    )
    .addEventListener(
        "click",
        verifyAccount
    );

async function verifyAccount() {

    const accountNumber =
        document
            .getElementById(
                "targetAccountNumber"
            )
            .value
            .trim();

    if (!accountNumber) {

        showToast(
            "Enter account number",
            "warning"
        );

        return;
    }

    try {

        const response =
            await fetch(

                `http://localhost:8080/api/accounts/account-number/${accountNumber}`,

                {

                    headers: {

                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {

            document
                .getElementById(
                    "verifiedAccountId"
                ).value = data.data.id;

            document
                .getElementById(
                    "accountInfo"
                ).innerHTML = `

                <strong>
                    Account Found
                </strong><br>

                Holder:
                ${data.data.accountHolderName}<br>

                Account Number:
                ${data.data.accountNumber}<br>

                Type:
                ${data.data.accountType}
                `;

            document
                .getElementById(
                    "accountInfo"
                )
                .classList.remove(
                    "d-none"
                );

            document
                .getElementById(
                    "transferBtn"
                )
                .disabled = false;
        }
        else {

            showToast(
                "Account not found",
                "danger"
            );
        }

    }
    catch (error) {

        console.error(error);

        showToast(
            "Verification failed",
            "danger"
        );
    }
}

// ====================================
// FORMS
// ====================================

document
    .getElementById("depositForm")
    .addEventListener(
        "submit",
        async (event) => {

            event.preventDefault();

            await transactionRequest(
                "/deposit",
                {
                    accountId:
                        document.getElementById(
                            "depositAccountId"
                        ).value,

                    amount:
                        document.getElementById(
                            "depositAmount"
                        ).value
                },
                "Deposit successful"
            );
        }
    );

document
    .getElementById("withdrawForm")
    .addEventListener(
        "submit",
        async (event) => {

            event.preventDefault();

            await transactionRequest(
                "/withdraw",
                {
                    accountId:
                        document.getElementById(
                            "withdrawAccountId"
                        ).value,

                    amount:
                        document.getElementById(
                            "withdrawAmount"
                        ).value
                },
                "Withdrawal successful"
            );
        }
    );

document
    .getElementById("transferForm")
    .addEventListener(
        "submit",
        async (event) => {

            event.preventDefault();

            await transactionRequest(
                "/transfer",
                {
                    fromAccountId:
                        document.getElementById(
                            "fromAccountId"
                        ).value,

                    toAccountId:
                        document.getElementById(
                            "verifiedAccountId"
                        ).value,

                    amount:
                        document.getElementById(
                            "transferAmount"
                        ).value
                },
                "Transfer successful"
            );
        }
    );

// ====================================
// TRANSACTION REQUEST
// ====================================

async function transactionRequest(
    endpoint,
    requestData,
    successMessage
) {

    try {

        const response =
            await fetch(

                `http://localhost:8080/api/transactions${endpoint}`,

                {

                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/json",

                        "Authorization":
                            `Bearer ${token}`
                    },

                    body:
                        JSON.stringify(
                            requestData
                        )
                }
            );

        const data =
            await response.json();

        if (data.success) {

            showToast(
                successMessage,
                "success"
            );

            loadAccounts();

            if (
                selectedAccountId
            ) {

                loadTransactions(
                    selectedAccountId
                );
            }
        }
        else {

            showToast(
                data.message,
                "danger"
            );
        }

    }
    catch (error) {

        console.error(error);

        showToast(
            "Operation failed",
            "danger"
        );
    }
}

// ====================================
// HISTORY
// ====================================

async function loadTransactions(
    accountId,
    accountNumber,
    page = 0
) {
    currentTransactionPage = page;

    currentTransactionAccountId =
        accountId;

    currentTransactionAccountNumber =
        accountNumber;

    selectedAccountId =
        accountId;

    document.getElementById(
        "selectedAccountInfo"
    ).innerText =
        `Account Number: ${accountNumber}`;

    try {

        const response =
            await fetch(

                `http://localhost:8080/api/transactions/history/${accountId}?page=${page}&size=10`,

                {
                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {
            renderTransactions(
                data.data.content
            );

            renderPagination(
                data.data
            );
        }
    }
    catch (error) {
        console.error(error);
    }
}

function renderTransactions(
    transactions
) {

    const tbody =
        document.getElementById(
            "transactionTableBody"
        );

    tbody.innerHTML = "";

    if (
        transactions.length === 0
    ) {

        tbody.innerHTML = `

            <tr>

                <td colspan="4"
                    class="text-center">

                    No transactions found

                </td>

            </tr>
        `;

        return;
    }

    transactions.forEach(tx => {

        let badgeClass =
            "tx-transfer";

        if (
            tx.type === "DEPOSIT"
        ) {

            badgeClass =
                "tx-deposit";
        }

        else if (
            tx.type === "WITHDRAWAL"
        ) {

            badgeClass =
                "tx-withdraw";
        }

        tbody.innerHTML += `

            <tr>

                <td>

                    <span class="${badgeClass}">

                        ${tx.type}

                    </span>

                </td>

                <td>

                    ₹${tx.amount}

                </td>

                <td>

                    ${tx.description}

                </td>

                <td>

                    ${tx.timeStamp}

                </td>

            </tr>
        `;
    });
}

function renderPagination(pageData) {
    let paginationContainer =
        document.getElementById(
            "paginationContainer"
        );

    if (!paginationContainer) {
        return;
    }

    paginationContainer.innerHTML = `

        <button
            class="btn btn-outline-primary btn-sm"
            ${pageData.first ? "disabled" : ""}
            onclick="
                loadTransactions(
                    ${currentTransactionAccountId},
                    '${currentTransactionAccountNumber}',
                    ${pageData.number - 1}
                )
            "
        >
            Previous
        </button>

        <span class="mx-3">

            Page
            ${pageData.number + 1}
            of
            ${pageData.totalPages}

        </span>

        <button
            class="btn btn-outline-primary btn-sm"
            ${pageData.last ? "disabled" : ""}
            onclick="
                loadTransactions(
                    ${currentTransactionAccountId},
                    '${currentTransactionAccountNumber}',
                    ${pageData.number + 1}
                )
            "
        >
            Next
        </button>
    `;
}

function showSection(sectionName) {
    document.getElementById(
        "accountsSection"
    ).style.display = "none";

    document.getElementById(
        "loanSection"
    ).style.display = "none";

    if (sectionName === "accounts") {
        document.getElementById(
            "accountsSection"
        ).style.display = "block";
    }

    if (sectionName === "loan") {
        document.getElementById(
            "loanSection"
        ).style.display = "block";
    }
}

document
    .getElementById("accountsLink")
    .addEventListener(
        "click",
        () => showSection("accounts")
    );

document
    .getElementById("loanLink")
    .addEventListener(
        "click",
        () => {

            showSection("loan");

            loadMyLoans();
        }
    );

// ====================================
// INIT
// ====================================
showSection("accounts");
loadAccounts();


// ====================================
// LOAN MODULE
// ====================================

async function loadMyLoans() {

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/loans/my-loans",
                {
                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {

            renderLoanCards(data.data);
        }
        else {

            document.getElementById(
                "loanContainer"
            ).innerHTML = `
                <div class="alert alert-info">
                    No loans found
                </div>
            `;
        }
    }
    catch (error) {

        console.error(error);

        document.getElementById(
            "loanContainer"
        ).innerHTML = `
            <div class="alert alert-info">
                No loans found
            </div>
        `;
    }
}

function renderLoanCards(loans) {

    const container =
        document.getElementById(
            "loanContainer"
        );

    container.innerHTML = "";

    if (!loans || loans.length === 0) {

        container.innerHTML = `
            <div class="alert alert-info">
                No loans found
            </div>
        `;

        return;
    }

    loans.forEach(loan => {

        let statusBadge = "secondary";

        if (loan.status === "PENDING") {
            statusBadge = "warning";
        }
        else if (loan.status === "ACTIVE") {
            statusBadge = "success";
        }
        else if (loan.status === "CLOSED") {
            statusBadge = "primary";
        }

        let emiButton = "";

        if (loan.status === "ACTIVE") {

            emiButton = `
                <button
                    class="btn btn-success"
                    onclick="payEmi()">

                    Pay EMI

                </button>
            `;
        }

        container.innerHTML += `

            <div class="card shadow-sm mb-3">

                <div class="card-body">

                    <div class="d-flex justify-content-between">

                        <h5>
                            Loan #${loan.loanId}
                        </h5>

                        <span
                            class="badge bg-${statusBadge}">

                            ${loan.status}

                        </span>

                    </div>

                    <hr>

                    <p>
                        <strong>
                            Loan Amount:
                        </strong>

                        ₹${loan.loanAmount}
                    </p>

                    <p>
                        <strong>
                            Remaining Amount:
                        </strong>

                        ₹${loan.remainingAmount}
                    </p>

                    <p>
                        <strong>
                            Monthly EMI:
                        </strong>

                        ₹${loan.monthlyEmi}
                    </p>

                    <p>
                        <strong>
                            Interest Rate:
                        </strong>

                        ${loan.interestRate}%
                    </p>

                    <p>
                        <strong>
                            Duration:
                        </strong>

                        ${loan.durationMonths} Months
                    </p>

                    <p>
                        <strong>
                            Purpose:
                        </strong>

                        ${loan.purpose}
                    </p>

                    ${loan.status !== "CLOSED"
                ?
                `
    <p>
        <strong>
            Next Due Date:
        </strong>

        ${loan.nextDueDate ?? "-"}
    </p>
    `
                :
                `
    <p class="text-success fw-bold">
        Loan Closed Successfully
    </p>
    `

            }


                    ${emiButton}

                </div>

            </div>
        `;
    });
}

async function payEmi() {

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/loans/pay-emi",
                {
                    method: "POST",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {

            showToast(
                data.data,
                "success"
            );

            loadAccounts();

            loadMyLoans();
        }
        else {

            showToast(
                data.message,
                "danger"
            );
        }
    }
    catch (error) {

        console.error(error);

        showToast(
            "EMI payment failed",
            "danger"
        );
    }
}