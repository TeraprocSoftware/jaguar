-- // create Jaguar baseline schema
-- Migration SQL that makes the change goes here.

--
-- Name: jaguar_user; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE jaguar_user (
    id character varying(255) NOT NULL,
    account character varying(255),
    email character varying(255)
  );

--
-- Name: application; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE application (
    id bigint NOT NULL,
    name character varying(255),
    provider character varying(255),
    enabled integer,
    user_id character varying(255)
);

--
-- Name: policy; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE policy (
    id bigint NOT NULL,
    name character varying(255),
    description character varying(255),
    enabled integer,
    interval integer,
    scope character varying(255),
    time_zone character varying(255),
    cron character varying(255),
    start_time character varying(255),
    duration character varying(255),
    alert_definition character varying(4096),
    action_definition character varying(4096),
    application_id bigint
);

--
-- Name: history; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE history (
    id bigint NOT NULL,
    application_id bigint,
    policy_id bigint,
    application character varying(255),
    policy character varying(255),
    scope character varying(255),
    "timestamp" bigint NOT NULL,
    user_id character varying(255)
);

--
-- Name: sequence_table; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE sequence_table
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: jaguar_user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY jaguar_user
    ADD CONSTRAINT jaguar_user_pkey PRIMARY KEY (id);

--
-- Name: application_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY application
    ADD CONSTRAINT application_pkey PRIMARY KEY (id);

--
-- Name: policy_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY policy
    ADD CONSTRAINT policy_pkey PRIMARY KEY (id);

--
-- Name: history_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY history
    ADD CONSTRAINT history_pkey PRIMARY KEY (id);

--
-- Name: fk_application_user_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY application
    ADD CONSTRAINT fk_application_user_id FOREIGN KEY (user_id) REFERENCES jaguar_user(id);

--
-- Name: fk_policy_application_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY policy
    ADD CONSTRAINT fk_policy_application_id FOREIGN KEY (application_id) REFERENCES application(id);


ALTER TABLE policy
    ALTER COLUMN alert_definition type text;

ALTER TABLE policy
    ALTER COLUMN action_definition type text;

-- //@UNDO
-- SQL to undo the change goes here.