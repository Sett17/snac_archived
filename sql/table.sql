-- Table: public.snippets

-- DROP TABLE IF EXISTS public.snippets;

CREATE TABLE IF NOT EXISTS public.snippets
(
    id text COLLATE pg_catalog."default" NOT NULL DEFAULT new_id(),
    title text COLLATE pg_catalog."default" NOT NULL,
    content text COLLATE pg_catalog."default",
    "timestamp" timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    tags text[] COLLATE pg_catalog."default",
    "desc" text COLLATE pg_catalog."default",
    CONSTRAINT snippets_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.snippets
    OWNER to snac;
-- Index: TagsIndex

-- DROP INDEX IF EXISTS public."TagsIndex";

CREATE INDEX IF NOT EXISTS "TagsIndex"
    ON public.snippets USING gin
    (tags COLLATE pg_catalog."default")
    TABLESPACE pg_default;

-- Trigger: timestamp

-- DROP TRIGGER IF EXISTS "timestamp" ON public.snippets;

CREATE TRIGGER "timestamp"
    BEFORE INSERT OR UPDATE 
    ON public.snippets
    FOR EACH ROW
    EXECUTE FUNCTION public.update_timestamp();