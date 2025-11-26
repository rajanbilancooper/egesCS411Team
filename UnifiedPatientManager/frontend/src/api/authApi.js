import client from "./axiosClient";

// Wrapper around backend authentication endpoints
// Backend has a two-step login: POST /auth/login (username + password -> OTP sent)
// then POST /auth/verify (username + otpCode -> token returned)
export const authApi = {
  // Step 1: Send username+password -> triggers OTP send
  login: (payload) => client.post("/auth/login", payload),

  // Step 2: Verify the OTP -> returns LoginResponse with token
  verify: (payload) => client.post("/auth/verify", payload),

  // Resend OTP for username
  resendOtp: (payload) => client.post("/auth/resend-otp", payload),

  // Logout (requires Authorization header)
  logout: () => client.post("/auth/logout"),

  // Forgot password - Step 1: Send OTP to user's email
  forgotPassword: (payload) => client.post("/auth/forgot-password", payload),

  // Reset password - Step 2: Verify OTP and set new password
  resetPassword: (payload) => client.post("/auth/reset-password", payload),

  // Convenience helper for integration tests: login then verify with given OTP
  loginAndVerify: async ({ username, password, otpCode }) => {
    await client.post("/auth/login", { username, password });
    const res = await client.post("/auth/verify", { username, otpCode });
    return res;
  },
};