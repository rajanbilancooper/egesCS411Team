// src/components/NotesPanel.jsx
import React, { useEffect, useState } from "react";
import { patientApi } from "../api/patientApi";

export default function NotesPanel({ patientId }) {
  const [text, setText] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const loadNotes = async () => {
      const res = await patientApi.getNotes(patientId);
      const notes = res.data;
      if (notes && notes.length > 0) {
        // assuming each note has a "content" field
        setText(notes[notes.length - 1].content);
      }
    };
    loadNotes();
  }, [patientId]);

  const handleSave = async () => {
    setSaving(true);
    try {
      await patientApi.createNote(patientId, {
        content: text, // must match your NoteRequestDTO field name
      });
      // you could show a "Saved!" toast
    } finally {
      setSaving(false);
    }
  };

  const handleUpload = (e) => {
    const files = Array.from(e.target.files);
    console.log("Uploaded files:", files);
    // later: send to backend endpoint for note attachments
  };

  return (
    <div className="card notes-panel">
      <textarea
        className="notes-textarea"
        placeholder="Create Appointment Notes Here..."
        value={text}
        onChange={(e) => setText(e.target.value)}
      />

      <div className="notes-footer">
        <label className="btn-secondary">
          Upload Files
          <input
            type="file"
            multiple
            style={{ display: "none" }}
            onChange={handleUpload}
          />
        </label>

        <div className="notes-actions">
          <button className="btn-secondary">Record</button>
          <button
            className="btn-primary"
            onClick={handleSave}
            disabled={saving}
          >
            {saving ? "Saving..." : "Save"}
          </button>
        </div>
      </div>
    </div>
  );
}
