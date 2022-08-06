-- FUNCTION: public.new_id()

-- DROP FUNCTION IF EXISTS public.new_id();

CREATE OR REPLACE FUNCTION public.new_id(
	)
    RETURNS text
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
declare
  chars text[] := '{1,2,3,4,5,6,7,8,9,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z}';
  result text := '';
  i integer := 0;
  isUnique bool := false;
begin
  while isUnique <> true loop
    for i in 1..5 loop
      result := result || chars[1+random()*(array_length(chars, 1)-1)];
    end loop;
	isUnique := not exists(select id from snippets where id = result);
  end loop;
  return result;
end;
$BODY$;

ALTER FUNCTION public.new_id()
    OWNER TO snac;
