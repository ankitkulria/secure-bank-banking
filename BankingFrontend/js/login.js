const loginForm = document.getElementById("loginForm");
const alertBox = document.getElementById("alertBox");

loginForm.addEventListener("submit", async (event) => {

    event.preventDefault();

    // Values
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();

    // DTO Object
    const loginData = {
        email,
        password
    };

    try {

        const response = await fetch("http://localhost:8080/api/auth/login", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(loginData)

        });

        const data = await response.json();

        // Success
        if (data.success) {

            showAlert("Login successful", "success");

            // Save JWT token
            localStorage.setItem("token", data.data.token);

            // Save user email
            localStorage.setItem("name", data.data.name);
            localStorage.setItem("email", data.data.email);

            localStorage.setItem(
                "role",
                data.data.role
            );

            setTimeout(() => {

                if (
                    data.data.role === "ADMIN"
                ) {
                    window.location.href =
                        "admin-dashboard.html";
                }
                else {
                    window.location.href =
                        "dashboard.html";
                }

            }, 200);

        }
        else {
            showAlert(data.message, "danger");
        }

    }
    catch (error) {

        console.error(error);

        showAlert("Server error occurred", "danger");
    }

});

// Alert
function showAlert(message, type) {

    alertBox.innerHTML = `
        <div class="alert alert-${type}">
            ${message}
        </div>
    `;
}