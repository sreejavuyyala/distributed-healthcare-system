-- Schema layout:
--   staging   : landing zone for validated-but-unmodeled feed data (idempotent upserts by natural key)
--   analytics : query-optimized, partitioned, indexed analytics layer served by the REST API
--   audit     : feed_execution tracking used for pipeline health / fault-isolation observability
CREATE SCHEMA IF NOT EXISTS staging;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS audit;