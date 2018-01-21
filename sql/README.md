 - Database setup

A Postgres database is used for storing configuration data and for persisting scored flows and scored flow details. The following commands should suffice to create the database:

```SQL
CREATE ROLE nfs_user2 LOGIN
  NOSUPERUSER INHERIT CREATEDB NOCREATEROLE NOREPLICATION
  PASSWORD 'nfs_user';

CREATE DATABASE "NetflowScore"
  WITH OWNER = nfs_user
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_ZA.UTF-8'
       LC_CTYPE = 'en_ZA.UTF-8'
       CONNECTION LIMIT = -1;

ALTER DATABASE "NetflowScore"
  SET search_path = """$user"", public, topology";
GRANT CONNECT, TEMPORARY ON DATABASE "NetflowScore" TO public;
GRANT ALL ON DATABASE "NetflowScore" TO nfs_user;
```

If you want to use different credentials then the corresponding values should be set in the Storm topology source code. Once the database has been created then the required database schema can be created using the SQL script 'NetflowScore.sql'.
