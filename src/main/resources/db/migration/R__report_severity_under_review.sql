-- Existing installations may have Hibernate's original enum CHECK constraint.
-- On a fresh database Hibernate creates the table after Flyway runs.
DO $$
BEGIN
    IF to_regclass('waste_report') IS NOT NULL THEN
        ALTER TABLE waste_report DROP CONSTRAINT IF EXISTS waste_report_severity_level_check;
        ALTER TABLE waste_report ADD CONSTRAINT waste_report_severity_level_check
            CHECK (severity_level IN ('LOW', 'MEDIUM', 'HIGH', 'UNDER_REVIEW'));
    END IF;
END $$;
