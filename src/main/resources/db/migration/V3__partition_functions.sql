CREATE OR REPLACE FUNCTION create_logs_partition(partition_name TEXT, start_date DATE, end_date DATE)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF logs FOR VALUES FROM (%L) TO (%L)',
        partition_name,
        start_date,
        end_date
    );
END;
$$;

CREATE OR REPLACE FUNCTION drop_logs_partition(partition_name TEXT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE format('DROP TABLE IF EXISTS %I', partition_name);
END;
$$;
