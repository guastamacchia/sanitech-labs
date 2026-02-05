-- V11__fix_admission_enum_values.sql
-- Correzione valori enum per allineamento con AdmissionType e AdmissionStatus Java
--
-- AdmissionType enum: INPATIENT, DAY_HOSPITAL, OBSERVATION
-- AdmissionStatus enum: ACTIVE, DISCHARGED, CANCELLED

-- Correzione admission_type: ORDINARY -> INPATIENT
UPDATE admissions
SET admission_type = 'INPATIENT'
WHERE admission_type = 'ORDINARY';

-- Correzione status: ADMITTED -> ACTIVE
UPDATE admissions
SET status = 'ACTIVE'
WHERE status = 'ADMITTED';
