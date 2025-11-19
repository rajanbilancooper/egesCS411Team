import client from "./axiosClient";

export const authApi = {
  login: (payload) =>
    client.post("/auth/login", payload),

  register: (payload) =>
    client.post("/auth/register", payload),

  sendOtp: (payload) =>
    client.post("/auth/send-otp", payload),

  verifyOtp: (payload) =>
    client.post("/auth/verify-otp", payload),
};