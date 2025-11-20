// src/pages/LoginPage.jsx
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "./api/authApi";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
    const [info, setInfo] = useState("");
  const navigate = useNavigate();
  const [step, setStep] = useState("login"); // 'login' or 'verify'
  const [otp, setOtp] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      setError("");
      console.log("Submitting login", { username, password });
      const res = await authApi.login({ username, password, rememberMe: false });
      console.log("Login response", res);
      // backend sends OTP to user email, and returns some user info (no token yet)
      setStep("verify");
      setInfo("OTP sent to your email. Please check your inbox.");
    } catch (err) {
      const msg = err?.response?.data?.message || err?.response?.data || err.message || "Invalid username or password";
      setError(msg);
      setInfo("");
    }
    setLoading(false);
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
      setInfo("");
    try {
      console.log("Verifying OTP", { username, otp });
      const res = await authApi.verify({ username, otpCode: otp });
      console.log("Verify response", res);
      localStorage.setItem("accessToken", res.data.token);
      // set token in axios interceptor already, so next requests include it
      navigate(`/patients/${res.data.userId || 1}`);
    } catch (err) {
      const msg = err?.response?.data?.message || err?.response?.data || err.message || "Invalid OTP or verification failed";
      setError(msg);
    }
    setLoading(false);
  };

  const handleResend = async () => {
    setError("");
    try {
      await authApi.resendOtp({ username });
    } catch (err) {
      setError(err.response?.data || "Failed to resend OTP");
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
            {info && <p className="upm-login-info">{info}</p>}
            {error && <p className="upm-login-error">{error}</p>}

            {step === "login" && (
                <div className="upm-login-buttons">
                  <button type="submit" className="upm-login-btn-primary" disabled={loading}>
                  Continue (send OTP)
                </button>
                <button
                  type="button"
                  className="upm-login-btn-secondary"
                  onClick={() => navigate("/register")}
                >
                  Register
                </button>
              </div>
            )}

            {step === "verify" && (
              <>
                <label className="upm-login-label">
                  <span>OTP Code</span>
                  <input
                    className="upm-login-input"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    placeholder="123456"
                    autoComplete="one-time-code"
                  />
                </label>
                <div className="upm-login-buttons">
                  <button type="button" onClick={handleVerify} className="upm-login-btn-primary">
                    Verify OTP
                  </button>
                  <button
                    onClick={() => setStep("login")}
                    type="button"
                    className="upm-login-btn-secondary"
                  >
                    Back
                  </button>
                </div>
                <div style={{ marginTop: 12 }}>
                  <button className="upm-login-forgot" onClick={handleResend}>
                    Resend OTP
                  </button>
                </div>
              </>
            )}

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
