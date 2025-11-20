// import React from "react";
// import "./UnifiedPatientManager.css";
// import NotesPanel from "./NotesPanel";

// export default function UnifiedPatientManager() {
//   const patientId = 3; // change as needed

//   return (
//     <div className="upm-root">
//       <header className="upm-header">
//         <div className="upm-logo-circle"><span className="upm-logo-cross">+</span></div>
//         <span className="upm-title">Unified Patient Manager</span>
//       </header>

//       <main className="upm-main">
//         <div className="upm-search-wrapper">
//           <span className="upm-search-icon">🔍</span>
//           <input className="upm-search-input" placeholder="Search or Enter Patient Name" />
//         </div>

//         <div className="upm-tabs">
//           <button className="upm-tab upm-tab--active">Basic Information</button>
//           <button className="upm-tab">Prescription History</button>
//           <button className="upm-tab">Vaccine record</button>
//           <button className="upm-tab">Appointment history</button>
//           <button className="upm-tab">Notes</button>
//           <button className="upm-tab upm-tab--grid">Patient History</button>
//         </div>

//         <div className="upm-grid">
//           <div className="upm-column">
//             <section className="upm-card upm-profile-card">
//               <div className="upm-profile-left">
//                 <div className="upm-avatar-wrapper">
//                   <img className="upm-avatar" src="https://via.placeholder.com/160x160.png?text=Photo" alt="Patient" />
//                 </div>
//                 <h2 className="upm-patient-name">Rajan Bilan-Cooper</h2>
//               </div>
//               <div className="upm-profile-divider" />
//               <div className="upm-profile-details">
//                 <p><span className="upm-label">DOB:</span> 1/1/98</p>
//                 <p><span className="upm-label">Gender:</span> MALE</p>
//                 <p><span className="upm-label">Contact:</span> 408-382-3049</p>
//               </div>
//             </section>

//             <section className="upm-card">
//               <h3 className="upm-card-title">Basic Information</h3>
//               <div className="upm-horizontal-line" />
//               <div className="upm-basic-grid">
//                 <p><span className="upm-label">Height:</span> 6’4</p>
//                 <p><span className="upm-label">Weight:</span> 180 lbs</p>
//               </div>
//               <p><span className="upm-label">Insurance:</span> Cigna</p>
//               <p><span className="upm-label">Allergies:</span> Peanuts, Penicillin, Grass</p>
//               <p><span className="upm-label">Current medication:</span> N/A</p>
//             </section>
//           </div>

//           <div className="upm-column">
//             {/* Right column: put Notes UI here */}
//             <NotesPanel patientId={patientId} />
//           </div>
//         </div>
//       </main>
//     </div>
//   );
// }
