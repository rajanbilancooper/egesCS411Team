import { useEffect, useState } from "react";
import { NotesAPI } from "./api/note";

export default function NotesPanel({ patientId = 3 }) {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState(null);

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    setErr(null);
    NotesAPI.list(patientId)
      .then(res => { if (mounted) setNotes(Array.isArray(res.data) ? res.data : []); })
      .catch(e => { console.error(e); if (mounted) setErr("Backend unavailable"); })
      .finally(() => mounted && setLoading(false));
    return () => (mounted = false);
  }, [patientId]);

  return (
    <div className="upm-card" style={{marginTop:12}}>
      <h3 className="upm-card-title">Notes</h3>
      {loading && <div>Loading…</div>}
      {err && <div style={{color:"#b91c1c"}}>{err}</div>}
      {!loading && !err && notes.length === 0 && <div>No notes yet.</div>}
      {!loading && !err && notes.length > 0 && (
        <ul>
          {notes.map(n => (
            <li key={n.id}>
              <b>{n.noteType ?? n.note_type}</b> — {n.content}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}