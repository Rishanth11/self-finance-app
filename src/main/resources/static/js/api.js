function apiRequest(url, options = {}) {
    const token = localStorage.getItem("token");

    const headers = {
        "Content-Type": "application/json",
        ...options.headers
    };

    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    return fetch("/api" + url, {
        ...options,
        headers
    });
}