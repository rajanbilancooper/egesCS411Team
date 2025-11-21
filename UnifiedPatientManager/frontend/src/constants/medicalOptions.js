// Centralized lists for allergy substances and prescription medications
// These lists are intentionally limited to the medications/substances
// that the backend conflict-checker understands (so the UI can restrict
// selections to items that will be validated).

export const ALLERGY_SUBSTANCES = [
  // Keys used in backend ALLERGY_CONFLICTS map
  "Penicillin",
  "Cephalosporins",
  "Sulfa",
  "NSAIDs",
  "Aspirin",
  "Opioids",
  "StatinIntolerance",
  "BetaBlockers",
  "ThyroidHormone",
  "Insulin",
];

export const PRESCRIPTION_MEDICATIONS = [
  // Union of medicines referenced by allergy/conflict rules in the backend
  // (keeps the front-end restricted to items that are checked server-side)
  "Penicillin",
  "Amoxicillin",
  "Ampicillin",
  "Oxacillin",
  "Piperacillin",
  "Cephalexin",
  "Ceftriaxone",
  "Cefuroxime",
  "Sulfamethoxazole",
  "Trimethoprim-Sulfamethoxazole",
  "Sulfasalazine",
  "Ibuprofen",
  "Naproxen",
  "Aspirin",
  "Diclofenac",
  "Indomethacin",
  "Morphine",
  "Codeine",
  "Oxycodone",
  "Hydrocodone",
  "Hydromorphone",
  "Atorvastatin",
  "Simvastatin",
  "Rosuvastatin",
  "Pravastatin",
  "Metoprolol",
  "Atenolol",
  "Propranolol",
  "Levothyroxine",
  "Liothyronine",
  "Insulin Glargine",
  "Insulin Lispro",
  "Insulin Aspart",
  "Warfarin",
  "Lisinopril",
  "Losartan",
  "Amlodipine",
  "Diazepam",
  "Gabapentin",
  "Sertraline",
  "Gemfibrozil",
];

export default {
  ALLERGY_SUBSTANCES,
  PRESCRIPTION_MEDICATIONS,
};
