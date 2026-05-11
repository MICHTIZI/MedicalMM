-- UTF-8. Run on medical_db after creating medical_patient_diagnosis table.
-- Inserts an initial diagnosis row for existing patients missing one.

INSERT INTO medical_patient_diagnosis (
    patient_id, has_image, has_medical_record, has_lab_result,
    diagnosis_status, has_diagnosis_report, is_deleted, create_time, update_time
)
SELECT p.patient_id, 0, 0, 0, 0, 0, 0, NOW(), NOW()
FROM medical_patient p
WHERE NOT EXISTS (
    SELECT 1 FROM medical_patient_diagnosis d WHERE d.patient_id = p.patient_id AND (d.is_deleted = 0 OR d.is_deleted IS NULL)
);
