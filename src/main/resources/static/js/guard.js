function requireAuth() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/pages/login.html";
        return;
    }

    try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        const expiry = payload.exp * 1000;

        if (Date.now() > expiry) {
            localStorage.removeItem("token");
            window.location.href = "/pages/login.html";
        }
    } catch (e) {
        localStorage.removeItem("token");
        window.location.href = "/pages/login.html";
    }
}

function requireAdmin() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/pages/login.html";
        return;
    }

    const payload = JSON.parse(atob(token.split(".")[1]));
    const roles =
        payload.authorities ||
        payload.roles ||
        (payload.role ? [payload.role] : []);

    if (!roles.includes("ROLE_ADMIN")) {
        alert("Admin access only");
        window.location.href = "/pages/dashboard.html";
    }
}