// src/pages/LoginPage.jsx
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "./api/authApi"; // you already made this

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const res = await authApi.login({ username, password });
      // adjust to whatever your backend returns
      localStorage.setItem("accessToken", res.token);
      // temp: just go to patient with id 1 (you can make this dynamic later)
      navigate("/patients/1");
    } catch (err) {
      setError("Invalid username or password");
    }
  };

 return (
    <div className="upm-login-root">
      {/* Top blue bar with logo + title */}
      <header className="upm-login-header">
        <div className="upm-login-logo-circle" />
        <span className="upm-login-header-title">Unified Patient Manager</span>
      </header>

      {/* Centered login card */}
      <main className="upm-login-main">
        <div className="upm-login-card">
          <h2 className="upm-login-title">LOGIN</h2>

          <form onSubmit={handleSubmit} className="upm-login-form">
            <label className="upm-login-label">
              <span>Username</span>
              <input
                className="upm-login-input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
              />
            </label>

            <label className="upm-login-label">
              <span>Password</span>
              <input
                className="upm-login-input"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </label>

            <div className="upm-login-row">
              <label className="upm-login-remember">
                <input type="checkbox" />
                <span>Remember Me</span>
              </label>
            </div>

            {error && <p className="upm-login-error">{error}</p>}

            <div className="upm-login-buttons">
              <button type="submit" className="upm-login-btn-primary">
                Log In
              </button>
              <button
                type="button"
                className="upm-login-btn-secondary"
                onClick={() => navigate("/register")}
              >
                Register
              </button>
            </div>

            <button
              type="button"
              className="upm-login-forgot"
              onClick={() => alert("Forgot password flow not wired yet")}
            >
              Forgot Password?
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}
