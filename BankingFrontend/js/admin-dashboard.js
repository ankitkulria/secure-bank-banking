const token = localStorage.getItem("token");

const role =
    localStorage.getItem(
        "role"
    );

if (role !== "ADMIN") {
    window.location.href =
        "dashboard.html";
}

if (!token) {
    window.location.href = "login.html";
}

// ======================
// NAVIGATION
// ======================

function showSection(section) {

    document.getElementById(
        "dashboardSection"
    ).style.display = "none";

    document.getElementById(
        "usersSection"
    ).style.display = "none";

    document.getElementById(
        "loansSection"
    ).style.display = "none";

    document.getElementById(
        "accountsSection"
    ).style.display = "none";

    if (section === "dashboard") {

        document.getElementById(
            "dashboardSection"
        ).style.display = "block";
    }

    if (section === "users") {

        document.getElementById(
            "usersSection"
        ).style.display = "block";
    }

    if (section === "loans") {

        document.getElementById(
            "loansSection"
        ).style.display = "block";
    }

    if (section === "accounts") {

        document.getElementById(
            "accountsSection"
        ).style.display = "block";
    }
}

document
    .getElementById("dashboardLink")
    .addEventListener(
        "click",
        () => showSection("dashboard")
    );

document
    .getElementById("usersLink")
    .addEventListener(
        "click",
        () => {

            showSection("users");

            loadUsers();
        }
    );

document
    .getElementById("loansLink")
    .addEventListener(
        "click",
        () => {

            showSection("loans");

            loadAllLoans();
        }
    );

document
    .getElementById("accountsLink")
    .addEventListener(
        "click",
        () => {

            showSection("accounts");

            loadAccounts();
        }
    );

// ======================
// LOGOUT
// ======================

document
    .getElementById("logoutBtn")
    .addEventListener(
        "click",
        () => {

            localStorage.clear();

            window.location.href =
                "login.html";
        }
    );


async function loadReserveAccount() {

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/admin/reserve-account",
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {

            document.getElementById(
                "reserveAccountNumber"
            ).innerText =
                `Account Number : ${data.data.accountNumber}`;

            document.getElementById(
                "reserveBalance"
            ).innerText =
                `₹${data.data.balance}`;
        }

    }
    catch (error) {

        console.error(
            "Reserve account error",
            error
        );
    }
}

async function loadUsers() {

    try {

        const response = await fetch(
            "http://localhost:8080/api/admin/users",
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );

        const data = await response.json();

        console.log(data); // <-- add this

        if (data.success) {

            renderUsers(data.data);

            document.getElementById(
                "totalUsers"
            ).innerText =
                data.data.length;
        }

    } catch (error) {

        console.error(error);
    }
}

function renderUsers(users) {

    const tbody =
        document.getElementById(
            "userTableBody"
        );

    tbody.innerHTML = "";

    users.forEach(user => {

        tbody.innerHTML += `

            <tr>

                <td>
                    ${user.id}
                </td>

                <td>
                    ${user.name}
                </td>

                <td>
                    ${user.email}
                </td>

                <td>
                    ${user.role}
                </td>

            </tr>

        `;
    });
}

async function loadAccounts() {

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/admin/accounts",
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {

            renderAccounts(
                data.data
            );

            document.getElementById(
                "totalAccounts"
            ).innerText =
                data.data.length;
        }

    }
    catch (error) {

        console.error(error);
    }
}

function renderAccounts(accounts) {

    const tbody =
        document.getElementById(
            "accountTableBody"
        );

    tbody.innerHTML = "";

    accounts.forEach(account => {

        tbody.innerHTML += `

            <tr>

                <td>
                    ${account.accountNumber}
                </td>

                <td>
                    ${account.accountHolderName}
                </td>

                <td>
                    ${account.accountType}
                </td>

                <td>
                    ₹${account.balance}
                </td>

            </tr>

        `;
    });
}

async function loadPendingLoans() {

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/admin/loans/pending",
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {

            renderPendingLoans(
                data.data
            );

            document.getElementById(
                "pendingLoansCount"
            ).innerText =
                data.data.length;
        }

    }
    catch (error) {

        console.error(error);
    }
}



function renderPendingLoans(loans) {
    const tbody =
        document.getElementById(
            "pendingLoanTableBody"
        );

    tbody.innerHTML = "";

    loans
        .filter(
            loan =>
                loan.status === "PENDING"
        )
        .forEach(loan => {

            tbody.innerHTML += `
                <tr>

                    <td>${loan.loanId}</td>

                    <td>${loan.customerName}</td>

                    <td>₹${loan.loanAmount}</td>

                    <td>${loan.purpose}</td>

                    <td>${loan.status}</td>

                    <td>

                        <button
                            class="btn btn-success btn-sm"
                            onclick="approveLoan(${loan.loanId})">

                            Approve

                        </button>

                        <button
                            class="btn btn-danger btn-sm"
                            onclick="rejectLoan(${loan.loanId})">

                            Reject

                        </button>

                    </td>

                </tr>
            `;
        });
}


function renderLoanHistory(loans) {
    const tbody =
        document.getElementById(
            "allLoanTableBody"
        );

    tbody.innerHTML = "";

    loans
        .filter(
            loan =>
                loan.status !== "PENDING"
        )
        .forEach(loan => {

            tbody.innerHTML += `
                <tr>

                    <td>${loan.loanId}</td>

                    <td>${loan.customerName}</td>

                    <td>₹${loan.loanAmount}</td>

                    <td>${loan.status}</td>

                </tr>
            `;
        });
}
async function loadAllLoans() {

    try {

        const response =
            await fetch(
                "http://localhost:8080/api/admin/loans",
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {

            renderPendingLoans(
                data.data
            );

            renderLoanHistory(
                data.data
            );

            document.getElementById(
                "activeLoansCount"
            ).innerText =
                data.data.filter(
                    loan =>
                        loan.status === "ACTIVE"
                ).length;


            document.getElementById(
                "pendingLoansCount"
            ).innerText =
                data.data.filter(
                    loan =>
                        loan.status === "PENDING"
                ).length;
        }

    }
    catch (error) {

        console.error(error);
    }
}



async function approveLoan(loanId) {

    try {

        const response =
            await fetch(
                `http://localhost:8080/api/admin/loans/${loanId}/approve`,
                {
                    method: "POST",

                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();

        if (data.success) {

            showToast("Loan approved successfully");

            loadAllLoans();

            loadReserveAccount();
        }
        else {

            alert(
                data.message
            );
        }

    }
    catch (error) {

        console.error(error);

        alert(
            "Loan approval failed"
        );
    }
}


async function rejectLoan(loanId) {

    try {

        const response =
            await fetch(
                `http://localhost:8080/api/admin/loans/${loanId}/reject`,
                {
                    method: "POST",

                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );

        const data =
            await response.json();



        console.log("Reject Response:", data);

        if (data.success) {

            console.log(
                document.getElementById(
                    "toastContainer"
                )
            );

            showToast("Loan rejected successfully", "warning");

            loadAllLoans();
        }
        else {

            alert(
                data.message
            );
        }

    }
    catch (error) {

        console.error(error);

        alert(
            "Loan rejection failed"
        );
    }
}

function showToast(message, type = "success") {

    const container =
        document.getElementById(
            "toastContainer"
        );

    const toastId =
        "toast" + Date.now();

    container.innerHTML += `

        <div
            id="${toastId}"
            class="toast align-items-center text-bg-${type} border-0">

            <div class="d-flex">

                <div class="toast-body">
                    ${message}
                </div>

                <button
                    type="button"
                    class="btn-close btn-close-white me-2 m-auto"
                    data-bs-dismiss="toast">
                </button>

            </div>

        </div>
    `;

    const toast =
        new bootstrap.Toast(
            document.getElementById(
                toastId
            )
        );

    toast.show();
}

showSection("dashboard");

loadReserveAccount();

loadUsers();

loadAllLoans();

loadAccounts();