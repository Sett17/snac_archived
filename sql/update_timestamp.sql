-- FUNCTION: public.update_timestamp()

-- DROP FUNCTION IF EXISTS public.update_timestamp();

CREATE OR REPLACE FUNCTION public.update_timestamp()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
   NEW.timestamp = now(); 
   RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.update_timestamp()
    OWNER TO snac;
