import React, { useEffect, useState } from "react";
import { patientApi } from "./api/patientApi"; // API wrapper

export default function NotesPanel({ patientId }) {
  const [notes, setNotes] = useState([]);
  const [newNote, setNewNote] = useState("");
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(false);
  const [error, setError] = useState(null);

  // load existing notes from real backend
  useEffect(() => {
    const load = async () => {
      setFetching(true);
      setError(null);
      try {
        const res = await patientApi.getNotes(patientId);
        console.log("NOTES FROM API:", res.data);
        // axios returns data under res.data
        setNotes(Array.isArray(res.data) ? res.data : []);
      } catch (e) {
        console.error("Failed to load notes", e);
        setError(
          e?.response?.data?.message ||
            e?.response?.statusText ||
            e?.message ||
            "Failed to load notes"
        );
      } finally {
        setFetching(false);
      }
    };

    if (patientId != null) load();
  }, [patientId]);

  const handleSave = async () => {
    if (!newNote.trim()) return;

    setLoading(true);
    setError(null);
    try {
      const payload = {
        content: newNote,
        noteType: "GENERAL",
        doctorId: 1, // TODO: replace with real logged-in doctor/user id when available
        timestamp: new Date().toISOString(),
      };

      const res = await patientApi.createNote(patientId, payload);
      const saved = res?.data ?? { ...payload, id: Math.random() };
      setNotes((prev) => [saved, ...prev]);
      setNewNote("");
    } catch (e) {
      console.error("Failed to save note", e);
      setError(
        e?.response?.data?.message ||
          e?.response?.statusText ||
          e?.message ||
          "Failed to save note"
      );
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
        {error && (
          <div style={{ color: "red", marginTop: 8 }}>Error: {error}</div>
        )}


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
          {fetching && <p>Loading notes…</p>}

          {!fetching && notes.length === 0 && (
            <p className="upm-notes-empty">No notes yet for this patient.</p>
          )}

          {notes.map((note) => (
            <article key={note.id || note.timestamp || Math.random()} className="upm-notes-item">
              <div className="upm-notes-item-meta">
                <span className="upm-notes-item-date">
                  {note.timestamp
                    ? new Date(note.timestamp).toLocaleString()
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
