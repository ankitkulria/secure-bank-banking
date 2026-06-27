const email =
    sessionStorage.getItem(
        "resetEmail"
    );

if (!email) {

    window.location.href =
        "forgot-password.html";
}

document.getElementById(
    "emailText"
).innerText =
    `OTP sent to ${email}`;

const alertBox =
    document.getElementById(
        "alertBox"
    );

const timerElement =
    document.getElementById(
        "timer"
    );

const resendBtn =
    document.getElementById(
        "resendBtn"
    );

let expiryTime =
    Number(
        localStorage.getItem(
            "resetOtpExpiryTime"
        )
    );

function showAlert(
    message,
    type
) {

    alertBox.innerHTML = `

        <div
            class="alert alert-${type}">

            ${message}

        </div>
    `;
}

// =====================
// TIMER
// =====================

function startTimer() {

    const interval =
        setInterval(() => {

            const remainingSeconds =

                Math.floor(

                    (expiryTime - Date.now())
                    / 1000
                );

            if (
                remainingSeconds <= 0
            ) {

                clearInterval(
                    interval
                );

                timerElement.innerText =
                    "OTP Expired";

                resendBtn.disabled =
                    false;

                return;
            }

            const minutes =
                Math.floor(
                    remainingSeconds / 60
                );

            const seconds =
                remainingSeconds % 60;

            timerElement.innerText =

                `${String(minutes)
                    .padStart(2, '0')}:${String(seconds)
                        .padStart(2, '0')}`;

        }, 1000);
}

// =====================
// RESET PASSWORD
// =====================

document
    .getElementById("resetForm")
    .addEventListener(
        "submit",
        async (event) => {

            event.preventDefault();

            const otp =
                document
                    .getElementById(
                        "otp"
                    )
                    .value
                    .trim();

            const newPassword =
                document
                    .getElementById(
                        "newPassword"
                    )
                    .value
                    .trim();

            const confirmPassword =
                document
                    .getElementById(
                        "confirmPassword"
                    )
                    .value
                    .trim();

            if (
                newPassword !==
                confirmPassword
            ) {

                showAlert(
                    "Passwords do not match",
                    "danger"
                );

                return;
            }

            try {

                const response =
                    await fetch(

                        "http://localhost:8080/api/auth/reset-password",

                        {

                            method: "POST",

                            headers: {

                                "Content-Type":
                                    "application/json"
                            },

                            body: JSON.stringify({

                                email,
                                otp,
                                newPassword
                            })
                        }
                    );

                const data =
                    await response.json();

                if (data.success) {

                    showAlert(
                        "Password Reset Successful",
                        "success"
                    );

                    sessionStorage.removeItem(
                        "resetEmail"
                    );

                    localStorage.removeItem(
                        "resetOtpExpiryTime"
                    );

                    setTimeout(() => {

                        window.location.href =
                            "login.html";

                    }, 1500);
                }
                else {

                    showAlert(
                        data.message,
                        "danger"
                    );
                }

            }
            catch (error) {

                console.error(error);

                showAlert(
                    "Reset Failed",
                    "danger"
                );
            }
        }
    );

// =====================
// RESEND OTP
// =====================

resendBtn.addEventListener(
    "click",
    resendOtp
);

async function resendOtp() {

    try {

        const response =
            await fetch(

                "http://localhost:8080/api/auth/forgot-password/send-otp",

                {

                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        email
                    })
                }
            );

        const data =
            await response.json();

        if (data.success) {

            showAlert(
                "OTP Sent Again",
                "success"
            );

            localStorage.setItem(

                "resetOtpExpiryTime",

                Date.now() + 300000
            );

            location.reload();
        }
    }
    catch (error) {

        console.error(error);
    }
}

startTimer();