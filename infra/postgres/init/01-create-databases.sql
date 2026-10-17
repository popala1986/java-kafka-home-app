-- Uruchamiany automatycznie TYLKO przy pierwszym starcie Postgresa (pusty wolumen).
-- Każdy mikroserwis ma własną bazę (database per service).

CREATE DATABASE auth_db;
CREATE DATABASE planner_db;
CREATE DATABASE notification_db;
