import React, { useEffect, useRef, useState } from "react";
import { patientApi } from "./api/patientApi"; // API wrapper

export default function NotesPanel({ patientId }) {
  const [notes, setNotes] = useState([]);
  const [newNote, setNewNote] = useState("");
  const [noteType, setNoteType] = useState("TEXT"); // TEXT or FILE
  const [file, setFile] = useState(null);
  const [attachmentName, setAttachmentName] = useState("");
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

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
    setError(null);

    // Validation per type
    if (noteType === "TEXT") {
      if (!newNote.trim()) {
        setError("Please enter note content for a TEXT note.");
        return;
      }
    } else if (noteType === "FILE") {
      if (!file) {
        setError("Please choose a file to upload.");
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        setError("File exceeds 5MB size limit.");
        return;
      }
    }

    setLoading(true);
    setError(null);
    try {
      const noteDto = {
        content: noteType === "TEXT" ? newNote : null,
        noteType,
        doctorId: 1, // TODO: use real user id/role when available
        timestamp: new Date().toISOString(),
        attachmentName: noteType === "FILE" && attachmentName.trim() ? attachmentName.trim() : undefined,
      };

      // Always use multipart to align with backend @RequestPart
      const res = await patientApi.createNoteMultipart(patientId, noteDto, file);
      const saved = res?.data ?? { ...noteDto, id: Math.random(), attachmentName: file?.name };
      setNotes((prev) => [saved, ...prev]);
      // reset inputs
      setNewNote("");
      setFile(null);
      setAttachmentName("");
      setNoteType("TEXT");
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

  const onUploadClick = () => {
    if (fileInputRef.current) fileInputRef.current.click();
  };

  const onFileChange = (e) => {
    const f = e.target.files?.[0];
    if (f) {
      setFile(f);
      setAttachmentName(f.name || "");
      setNoteType("FILE");
    }
  };

  const handleDownload = async (n) => {
    try {
      const res = await patientApi.downloadNoteAttachment(patientId, n.id);
      const blob = new Blob([res.data], { type: res.headers["content-type"] || "application/octet-stream" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = n.attachmentName || "attachment.bin";
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      setError("Failed to download attachment");
    }
  };

  const handleDelete = async (n) => {
    if (!n?.id) {
      setError("Cannot delete unsaved note");
      return;
    }
    try {
      await patientApi.deleteNote(patientId, n.id);
      setNotes((prev) => prev.filter((x) => x.id !== n.id));
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || "Failed to delete note");
    }
  };

  return (
    <div className="upm-notes-layout">
      {/* LEFT: create note */}
      <section className="upm-card upm-notes-editor">
        <div className="upm-notes-editor-header">
          <span>Create Appointment Notes Here…</span>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 8 }}>
          <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <input
              type="radio"
              name="noteType"
              value="TEXT"
              checked={noteType === "TEXT"}
              onChange={() => { setNoteType("TEXT"); setFile(null); }}
            />
            <span>Text</span>
          </label>
          <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <input
              type="radio"
              name="noteType"
              value="FILE"
              checked={noteType === "FILE"}
              onChange={() => setNoteType("FILE")}
            />
            <span>File</span>
          </label>
          {noteType === "FILE" && file && (
            <span style={{ fontSize: 12, color: "#666" }}>{file.name}</span>
          )}
        </div>

        {noteType === "TEXT" ? (
          <textarea
            className="upm-notes-textarea"
            value={newNote}
            onChange={(e) => setNewNote(e.target.value)}
            placeholder="Create Appointment Notes Here…"
          />
        ) : (
          <div className="upm-notes-filepanel" style={{ border: "1px solid #ddd", borderRadius: 8, padding: 12 }}>
            <div style={{ marginBottom: 8, color: "#555" }}>
              Supported types: PDF, DOCX, PNG, JPG • Max size: 5MB
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <input
                type="text"
                className="upm-notes-input"
                placeholder="File name (optional)"
                value={attachmentName}
                onChange={(e) => setAttachmentName(e.target.value)}
                style={{ flex: 1 }}
              />
              <button className="upm-notes-btn-secondary" type="button" onClick={onUploadClick}>
                Choose File
              </button>
            </div>
            {file && (
              <div style={{ marginTop: 8, fontSize: 12, color: "#666" }}>
                Selected: {file.name} ({Math.round(file.size / 1024)} KB)
              </div>
            )}
            <input
              type="file"
              ref={fileInputRef}
              onChange={onFileChange}
              accept=".pdf,.doc,.docx,.png,.jpg,.jpeg"
              style={{ display: "none" }}
            />
          </div>
        )}
        {error && (
          <div style={{ color: "red", marginTop: 8 }}>Error: {error}</div>
        )}


        <div className="upm-notes-buttons">
          {/* Upload control embedded in file panel; no separate button here */}
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

        <div className="upm-notes-history-list" style={{ maxHeight: 320, overflowY: "auto", paddingRight: 8 }}>
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
              {note.noteType === "FILE" || note.noteType === 'FILE' ? (
                <div className="upm-notes-item-content">
                  <div>Attachment: {note.attachmentName || "attachment.bin"}</div>
                  {note.id && (
                    <button className="upm-notes-btn-outline" type="button" onClick={() => handleDownload(note)}>
                      Download
                    </button>
                  )}
                  {note.id && (
                    <button className="upm-notes-btn-secondary" type="button" onClick={() => handleDelete(note)} style={{ marginLeft: 8 }}>
                      Delete
                    </button>
                  )}
                </div>
              ) : (
                <div className="upm-notes-item-content">
                  <p>{note.content}</p>
                  {note.id && (
                    <button className="upm-notes-btn-secondary" type="button" onClick={() => handleDelete(note)} style={{ marginTop: 6 }}>
                      Delete
                    </button>
                  )}
                </div>
              )}
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
