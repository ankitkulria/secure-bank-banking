const forgotForm =
    document.getElementById(
        "forgotForm"
    );

const alertBox =
    document.getElementById(
        "alertBox"
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

forgotForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const email =
            document
                .getElementById("email")
                .value
                .trim();

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

                sessionStorage.setItem(
                    "resetEmail",
                    email
                );

                localStorage.setItem(
                    "resetOtpExpiryTime",
                    Date.now() + 300000
                );

                showAlert(
                    "OTP Sent Successfully",
                    "success"
                );

                setTimeout(() => {

                    window.location.href =
                        "reset-password.html";

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
                "Failed To Send OTP",
                "danger"
            );
        }
    }
);