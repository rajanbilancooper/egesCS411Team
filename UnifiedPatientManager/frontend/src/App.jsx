import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./loginPage";
import PatientDashboardPage from "./PatientDashboardPage";

const RequireAuth = ({ children }) => {
  const token = localStorage.getItem("accessToken");
  if (!token) return <Navigate to="/login" replace />;
  return children;
};

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/patients/:id"
        element={
          <RequireAuth>
            <PatientDashboardPage />
          </RequireAuth>
        }
      />

      {/* when you go to "/", send to login */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
