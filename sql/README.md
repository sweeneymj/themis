# Themis - Database setup

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
