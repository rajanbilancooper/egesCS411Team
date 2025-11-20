// src/NotesPanel.jsx
import React, { useEffect, useState } from "react";
import { patientApi } from "./api/patientApi"; // adjust path if needed

export default function NotesPanel({ patientId }) {
  const [notes, setNotes] = useState([]);
  const [newNote, setNewNote] = useState("");
  const [loading, setLoading] = useState(false);

  // load existing notes (mocked API for now)
  useEffect(() => {
    const load = async () => {
      try {
        const res = await patientApi.getNotes(patientId);
        setNotes(res.data || []);
      } catch (e) {
        console.error("Failed to load notes", e);
      }
    };
    load();
  }, [patientId]);

  const handleSave = async () => {
    if (!newNote.trim()) return;

    setLoading(true);
    try {
      const res = await patientApi.createNote(patientId, {
        content: newNote,
        createdAt: new Date().toISOString(),
      });

      // append to history
      setNotes((prev) => [res.data, ...prev]);
      setNewNote("");
    } catch (e) {
      console.error("Failed to save note", e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upm-notes-layout">
      {/* LEFT: create note */}
      <section className="upm-card upm-notes-editor">
        <div className="upm-notes-editor-header">
          <span>Create Appointment Notes Here…</span>
        </div>

        <textarea
          className="upm-notes-textarea"
          value={newNote}
          onChange={(e) => setNewNote(e.target.value)}
          placeholder="Create Appointment Notes Here…"
        />

        <div className="upm-notes-buttons">
          <button className="upm-notes-btn-secondary" type="button">
            Upload Files
          </button>
          <button className="upm-notes-btn-outline" type="button">
            Record
          </button>
          <button
            className="upm-notes-btn-primary"
            type="button"
            onClick={handleSave}
            disabled={loading}
          >
            {loading ? "Saving…" : "Save"}
          </button>
        </div>
      </section>

      {/* RIGHT: note history */}
      <section className="upm-card upm-notes-history">
        <h3 className="upm-notes-history-title">Note History</h3>
        <div className="upm-divider" />

        <div className="upm-notes-history-list">
          {notes.length === 0 && (
            <p className="upm-notes-empty">No notes yet for this patient.</p>
          )}

          {notes.map((note) => (
            <article key={note.id || note.createdAt} className="upm-notes-item">
              <div className="upm-notes-item-meta">
                <span className="upm-notes-item-date">
                  {note.createdAt
                    ? new Date(note.createdAt).toLocaleString()
                    : "New note"}
                </span>
              </div>
              <p className="upm-notes-item-content">{note.content}</p>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
