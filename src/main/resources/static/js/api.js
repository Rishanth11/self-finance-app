function apiRequest(url, options = {}) {
    const token = localStorage.getItem("token");

    return fetch("/api" + url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        }
    });
}
