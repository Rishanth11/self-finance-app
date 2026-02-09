function register() {
  apiRequest("/auth/register", "POST", {
    name: name.value,
    email: email.value,
    password: password.value,
    role: role.value
  });
}


function login() {
  apiRequest("/auth/login", "POST", {
    email: email.value,
    password: password.value
  })
    .then(res => res.json())
    .then(data => {
      if (data.token) {
        localStorage.setItem("token", data.token);
        window.location.href = "dashboard.html";
      } else {
        msg.innerText = data.error;
      }
    });
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "login.html";
}
