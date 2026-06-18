

const registerForm =
    document.getElementById("registerForm");

    const alertBox = document.getElementById("alertBox");

    function showAlert(message, type) {

    alertBox.innerHTML = `

        <div class="alert alert-${type}">

            ${message}

        </div>

    `;
}
registerForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const name =
            document.getElementById("name")
                .value.trim();

        const email =
            document.getElementById("email")
                .value.trim();

        const password =
            document.getElementById("password")
                .value.trim();

        try {

            const response =
                await fetch(

                    "http://localhost:8080/api/auth/send-otp",

                    {

                        method: "POST",

                        headers: {

                            "Content-Type":
                                "application/json"
                        },

                        body: JSON.stringify({

                            name,
                            email,
                            password
                        })
                    }
                );

            const data =
                await response.json();

            if (data.success) {

                sessionStorage.setItem(
                    "email",
                    email
                );

                showAlert(
                    "OTP sent successfully",
                    "success"
                );

                setTimeout(() => {

                    window.location.href =
                        "otp.html";

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
                "Failed to send OTP",
                "danger"
            );
        }
    }
);