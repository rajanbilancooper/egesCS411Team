import React, { useEffect, useState } from "react";
import client from "./api/axiosClient";

export default function ApiConnectivityBadge() {
  const [status, setStatus] = useState("checking");

  useEffect(() => {
    let cancelled = false;
    const ping = async () => {
      try {
        const res = await client.get("/health");
        if (!cancelled) setStatus(res.status === 200 ? "ok" : "error");
      } catch (e) {
        if (!cancelled) setStatus("error");
      }
    };
    ping();
    const id = setInterval(ping, 15000); // re-ping every 15s
    return () => { cancelled = true; clearInterval(id); };
  }, []);

  const color = status === "ok" ? "#2e7d32" : status === "error" ? "#c62828" : "#555";
  const text = status === "ok" ? "API Connected" : status === "error" ? "API Error" : "API Checking";

  return (
    <div style={{
      position: "absolute",
      top: 8,
      right: 8,
      padding: "4px 10px",
      borderRadius: 16,
      fontSize: 12,
      fontWeight: 600,
      background: color,
      color: "white",
      letterSpacing: 0.5,
    }} aria-label="API connectivity status">
      {text}
    </div>
  );
}
