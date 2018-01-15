-- Dumped from database version 9.5.10
-- Dumped by pg_dump version 9.5.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: topology; Type: SCHEMA; Schema: -; Owner: nfs_user
--

CREATE SCHEMA topology;


ALTER SCHEMA topology OWNER TO nfs_user;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: scored_flow_detail; Type: TABLE; Schema: public; Owner: nfs_user
--

CREATE TABLE scored_flow_detail (
    id bigint NOT NULL,
    flow_id bigint,
    score_name character varying(100),
    score integer,
    is_bad_score boolean,
    score_category character varying(100)
);


ALTER TABLE scored_flow_detail OWNER TO nfs_user;

--
-- Name: flow_score_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: nfs_user
--

CREATE SEQUENCE flow_score_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE flow_score_detail_id_seq OWNER TO nfs_user;

--
-- Name: flow_score_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nfs_user
--

ALTER SEQUENCE flow_score_detail_id_seq OWNED BY scored_flow_detail.id;


--
-- Name: list_known_host; Type: TABLE; Schema: public; Owner: nfs_user
--

CREATE TABLE list_known_host (
    id bigint DEFAULT nextval('flow_score_detail_id_seq'::regclass) NOT NULL,
    ip_address inet,
    description character varying(100)
);


ALTER TABLE list_known_host OWNER TO nfs_user;

--
-- Name: list_known_host_service; Type: TABLE; Schema: public; Owner: nfs_user
--

CREATE TABLE list_known_host_service (
    id bigint DEFAULT nextval('flow_score_detail_id_seq'::regclass) NOT NULL,
    list_known_host_id bigint,
    protocol integer,
    port integer,
    is_hosted boolean,
    description character varying(100)
);


ALTER TABLE list_known_host_service OWNER TO nfs_user;

--
-- Name: scored_flow; Type: TABLE; Schema: public; Owner: nfs_user
--

CREATE TABLE scored_flow (
    id bigint NOT NULL,
    src_ip inet,
    dst_ip inet,
    src_asn integer,
    dst_asn integer,
    processing_time integer,
    src_city character varying(100),
    src_country character varying(100),
    src_country_code character varying(10),
    dst_city character varying(100),
    dst_country character varying(100),
    dst_country_code character varying(10),
    good_score integer,
    bad_score integer,
    start_time timestamp without time zone,
    protocol integer,
    src_port integer,
    dst_port integer,
    ins_ts timestamp without time zone DEFAULT now()
);


ALTER TABLE scored_flow OWNER TO nfs_user;

--
-- Name: suspect_country; Type: TABLE; Schema: public; Owner: nfs_user
--

CREATE TABLE suspect_country (
    id bigint DEFAULT nextval('flow_score_detail_id_seq'::regclass) NOT NULL,
    country_code character varying(20),
    weighting numeric(10,6)
);


ALTER TABLE suspect_country OWNER TO nfs_user;

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: nfs_user
--

ALTER TABLE ONLY django_migrations ALTER COLUMN id SET DEFAULT nextval('django_migrations_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: nfs_user
--

ALTER TABLE ONLY scored_flow_detail ALTER COLUMN id SET DEFAULT nextval('flow_score_detail_id_seq'::regclass);


--
-- Name: django_migrations_pkey; Type: CONSTRAINT; Schema: public; Owner: nfs_user
--

ALTER TABLE ONLY django_migrations
    ADD CONSTRAINT django_migrations_pkey PRIMARY KEY (id);


--
-- Name: list_known_host_pkey; Type: CONSTRAINT; Schema: public; Owner: nfs_user
--

ALTER TABLE ONLY list_known_host
    ADD CONSTRAINT list_known_host_pkey PRIMARY KEY (id);


--
-- Name: list_known_host_service_pkey; Type: CONSTRAINT; Schema: public; Owner: nfs_user
--

ALTER TABLE ONLY list_known_host_service
    ADD CONSTRAINT list_known_host_service_pkey PRIMARY KEY (id);


--
-- Name: scored_flow_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: nfs_user
--

ALTER TABLE ONLY scored_flow_detail
    ADD CONSTRAINT scored_flow_detail_pkey PRIMARY KEY (id);


--
-- Name: scored_flow_pkey; Type: CONSTRAINT; Schema: public; Owner: nfs_user
--

ALTER TABLE ONLY scored_flow
    ADD CONSTRAINT scored_flow_pkey PRIMARY KEY (id);


--
-- Name: suspect_country_pkey; Type: CONSTRAINT; Schema: public; Owner: nfs_user
--

ALTER TABLE ONLY suspect_country
    ADD CONSTRAINT suspect_country_pkey PRIMARY KEY (id);


--
-- Name: unique_score_detail; Type: CONSTRAINT; Schema: public; Owner: nfs_user
--

ALTER TABLE ONLY scored_flow_detail
    ADD CONSTRAINT unique_score_detail UNIQUE (flow_id, score_category, score_name);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

