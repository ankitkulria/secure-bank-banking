const email =
    sessionStorage.getItem("email");

if (!email) {

    window.location.href =
        "register.html";
}

document.getElementById(
    "emailText"
).innerText =
    `OTP sent to ${email}`;

const otpForm =
    document.getElementById(
        "otpForm"
    );

const alertBox =
    document.getElementById(
        "alertBox"
    );

const resendBtn =
    document.getElementById(
        "resendBtn"
    );

const timerElement =
    document.getElementById(
        "timer"
    );

// ======================================
// ALERT
// ======================================

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

// ======================================
// OTP EXPIRY SETUP
// ======================================

let expiryTime =
    Number(
        localStorage.getItem(
            "otpExpiryTime"
        )
    );

if (!expiryTime) {

    expiryTime =
        Date.now() + 300000;

    localStorage.setItem(
        "otpExpiryTime",
        expiryTime
    );
}

// ======================================
// TIMER
// ======================================

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
                    .padStart(2, "0")}:${String(seconds)
                        .padStart(2, "0")}`;

        }, 1000);
}

// ======================================
// VERIFY OTP
// ======================================

otpForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const otp =
            document
                .getElementById("otp")
                .value
                .trim();

        try {

            const response =
                await fetch(

                    "http://localhost:8080/api/auth/verify-otp",

                    {

                        method: "POST",

                        headers: {

                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify({

                            email,
                            otp
                        })
                    }
                );

            const data =
                await response.json();

            if (data.success) {

                showAlert(
                    "Registration Successful",
                    "success"
                );

                sessionStorage.removeItem(
                    "email"
                );

                localStorage.removeItem(
                    "otpExpiryTime"
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
                "Verification Failed",
                "danger"
            );
        }
    }
);

// ======================================
// RESEND OTP
// ======================================

resendBtn.addEventListener(
    "click",
    resendOtp
);

async function resendOtp() {

    try {

        const response =
            await fetch(

                "http://localhost:8080/api/auth/resend-otp",

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

                "otpExpiryTime",

                Date.now() + 300000
            );

            location.reload();
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
            "Failed To Resend OTP",
            "danger"
        );
    }
}

// ======================================
// INIT
// ======================================

startTimer();