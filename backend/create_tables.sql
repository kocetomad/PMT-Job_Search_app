-- user_tbl

CREATE TABLE public.user_tbl
(
    user_id bigserial NOT NULL UNIQUE,
    first_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    last_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    email character varying(100) COLLATE pg_catalog."default" NOT NULL,
    dob date NOT NULL,
    password_hash character varying(300) COLLATE pg_catalog."default" NOT NULL,
    username character varying(50) COLLATE pg_catalog."default" NOT NULL,
    profile_url character varying(1000) COLLATE pg_catalog."default",
    CONSTRAINT user_tbl_pkey PRIMARY KEY (user_id),
    CONSTRAINT unique_usernames UNIQUE (username)
)
	
-- review_tbl

CREATE TABLE public.review_tbl
(
    employer_id character varying(200) COLLATE pg_catalog."default" NOT NULL,
    user_id integer NOT NULL,
    rating numeric NOT NULL,
    title character varying(100) COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default",
    CONSTRAINT review_tbl_pkey PRIMARY KEY (employer_id, user_id),
    CONSTRAINT fk_user_from_review FOREIGN KEY (user_id)
        REFERENCES public.user_tbl (user_id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID
)
	
-- pinned_tbl

CREATE TABLE public.pinned_tbl
(
    job_id character varying(200) COLLATE pg_catalog."default" NOT NULL,
    user_id integer NOT NULL,
    CONSTRAINT pinned_tbl_pkey PRIMARY KEY (job_id, user_id),
    CONSTRAINT fk_pinned_tbl FOREIGN KEY (user_id)
        REFERENCES public.user_tbl (user_id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID
)