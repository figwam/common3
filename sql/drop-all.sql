-- # --- !Ups



-- # --- !Downs

DROP VIEW IF EXISTS public.clazz_view CASCADE;
DROP TABLE IF EXISTS public.transaction CASCADE;
DROP TABLE IF EXISTS public.logger CASCADE;
DROP TABLE IF EXISTS public.registration CASCADE;
DROP TABLE IF EXISTS public.clazz CASCADE;
DROP TABLE IF EXISTS public.clazz_definition CASCADE;
DROP TABLE IF EXISTS public.studio CASCADE;
DROP TABLE IF EXISTS public.trainee_login_info CASCADE;
DROP TABLE IF EXISTS public.partner_login_info CASCADE;
DROP TABLE IF EXISTS public.bill CASCADE;
DROP TABLE IF EXISTS public.time_stop CASCADE;
DROP TABLE IF EXISTS public.subscription CASCADE;
DROP TABLE IF EXISTS public.trainee CASCADE;
DROP TABLE IF EXISTS public.partner CASCADE;
DROP TABLE IF EXISTS public.address CASCADE;
DROP TABLE IF EXISTS public.offer CASCADE;
DROP TABLE IF EXISTS public.trainee_password_info CASCADE;
DROP TABLE IF EXISTS public.partner_password_info CASCADE;
DROP TABLE IF EXISTS public.oauth1_info CASCADE;
DROP TABLE IF EXISTS public.oauth2_info CASCADE;
DROP TABLE IF EXISTS public.openidinfo CASCADE;
DROP TABLE IF EXISTS public.openidattributes CASCADE;
DROP TABLE IF EXISTS public.login_info CASCADE;

