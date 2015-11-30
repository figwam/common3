-- # --- !Ups

--
--         d8888      888      888
--        d88888      888      888
--       d88P888      888      888
--      d88P 888  .d88888  .d88888 888d888 .d88b.  .d8888b  .d8888b
--     d88P  888 d88" 888 d88" 888 888P"  d8P  Y8b 88K      88K
--    d88P   888 888  888 888  888 888    88888888 "Y8888b. "Y8888b.
--   d8888888888 Y88b 888 Y88b 888 888    Y8b.          X88      X88
--  d88P     888  "Y88888  "Y88888 888     "Y8888   88888P'  88888P'
--
--
--
CREATE TABLE public.address(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	street text NOT NULL,
	zip text NOT NULL,
	state text NOT NULL,
	city text NOT NULL,
	country text NOT NULL,
	created_on timestamp NOT NULL DEFAULT NOW(),
	updated_on timestamp NOT NULL DEFAULT NOW(),
	is_deleted boolean NOT NULL DEFAULT false,
	longitude decimal(9,6),
	latitude decimal(9,6)
);


--
--
--  88888888888              d8b
--      888                  Y8P
--      888
--      888  888d888 8888b.  888 88888b.   .d88b.   .d88b.
--      888  888P"      "88b 888 888 "88b d8P  Y8b d8P  Y8b
--      888  888    .d888888 888 888  888 88888888 88888888
--      888  888    888  888 888 888  888 Y8b.     Y8b.
--      888  888    "Y888888 888 888  888  "Y8888   "Y8888
--
--
--
CREATE TABLE public.trainee(
  id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	firstname text,
	lastname text,
	mobile text,
	phone text,
	email text,
	email_verified bool NOT NULL DEFAULT false,
	created_on timestamp NOT NULL DEFAULT NOW(),
	updated_on timestamp NOT NULL DEFAULT NOW(),
	ptoken text,
	is_deleted bool NOT NULL DEFAULT false,
	delete_reason text,
	is_active bool NOT NULL DEFAULT true,
	inactive_reason text,
	id_address uuid NOT NULL,
	username text,
	fullname text,
	avatarurl text
);

-- DROP INDEX IF EXISTS public.trainee_username_idx CASCADE;
CREATE INDEX trainee_username_idx ON public.trainee
USING btree
(
	username ASC NULLS LAST
);

-- ALTER TABLE public.trainee DROP CONSTRAINT IF EXISTS trainee_uq CASCADE;
ALTER TABLE public.trainee ADD CONSTRAINT trainee_uq UNIQUE (id_address);

-- ALTER TABLE public.trainee DROP CONSTRAINT IF EXISTS address_fk CASCADE;
ALTER TABLE public.trainee ADD CONSTRAINT trainee_address_fk FOREIGN KEY (id_address)
REFERENCES public.address (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

--
--  8888888b.                  888
--  888   Y88b                 888
--  888    888                 888
--  888   d88P 8888b.  888d888 888888 88888b.   .d88b.  888d888
--  8888888P"     "88b 888P"   888    888 "88b d8P  Y8b 888P"
--  888       .d888888 888     888    888  888 88888888 888
--  888       888  888 888     Y88b.  888  888 Y8b.     888
--  888       "Y888888 888      "Y888 888  888  "Y8888  888
--
--
--
CREATE TABLE public.partner(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	firstname text,
	lastname text,
	mobile text,
	phone text,
	email text,
	email_verified bool NOT NULL DEFAULT false,
	created_on timestamp NOT NULL DEFAULT NOW(),
	updated_on timestamp NOT NULL DEFAULT NOW(),
	ptoken text,
	is_deleted bool NOT NULL DEFAULT false,
	delete_reason text,
	is_active bool NOT NULL DEFAULT true,
	inactive_reason text,
	id_address uuid NOT NULL,
	username text,
	fullname text,
	avatarurl text,
	revenue decimal(5,2) NOT NULL
);

-- DROP INDEX IF EXISTS public.partner_username_idx CASCADE;
CREATE INDEX partner_username_idx ON public.partner
USING btree
(
	username ASC NULLS LAST
);

-- ALTER TABLE public.partner DROP CONSTRAINT IF EXISTS partner_uq CASCADE;
ALTER TABLE public.partner ADD CONSTRAINT partner_uq UNIQUE (id_address);

-- ALTER TABLE public.partner DROP CONSTRAINT IF EXISTS address_fk CASCADE;
ALTER TABLE public.partner ADD CONSTRAINT partner_address_fk FOREIGN KEY (id_address)
REFERENCES public.address (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

--   .d8888b.  888                  888 d8b
--  d88P  Y88b 888                  888 Y8P
--  Y88b.      888                  888
--   "Y888b.   888888 888  888  .d88888 888  .d88b.
--      "Y88b. 888    888  888 d88" 888 888 d88""88b
--        "888 888    888  888 888  888 888 888  888
--  Y88b  d88P Y88b.  Y88b 888 Y88b 888 888 Y88..88P
--   "Y8888P"   "Y888  "Y88888  "Y88888 888  "Y88P"
--
--
CREATE TABLE public.studio(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	name text NOT NULL,
	mobile text,
	phone text,
	email text,
	sporttype text,
	description text,
	created_on timestamp NOT NULL DEFAULT NOW(),
	updated_on timestamp NOT NULL DEFAULT NOW(),
	is_deleted bool NOT NULL DEFAULT false,
	deleted_reason text,
	id_address uuid NOT NULL,
	id_partner uuid NOT NULL,
	avatarurl text
);

-- ALTER TABLE public.studio DROP CONSTRAINT IF EXISTS address_fk CASCADE;
ALTER TABLE public.studio ADD CONSTRAINT address_fk FOREIGN KEY (id_address)
REFERENCES public.address (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.studio DROP CONSTRAINT IF EXISTS studio_uq CASCADE;
ALTER TABLE public.studio ADD CONSTRAINT studio_uq UNIQUE (id_address);

-- ALTER TABLE public.studio DROP CONSTRAINT IF EXISTS trainee_fk CASCADE;
ALTER TABLE public.studio ADD CONSTRAINT partner_fk FOREIGN KEY (id_partner)
REFERENCES public.partner (id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;

--   .d88888b.   .d888  .d888
--  d88P" "Y88b d88P"  d88P"
--  888     888 888    888
--  888     888 888888 888888 .d88b.  888d888
--  888     888 888    888   d8P  Y8b 888P"
--  888     888 888    888   88888888 888
--  Y88b. .d88P 888    888   Y8b.     888
--   "Y88888P"  888    888    "Y8888  888
--
--
--
CREATE TABLE public.offer(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	created_on timestamp NOT NULL DEFAULT NOW(),
	updated_on timestamp NOT NULL DEFAULT NOW(),
	name text NOT NULL,
	nr_access smallint NOT NULL,
	price decimal(5,2) NOT NULL,
	price_timestop decimal(5,2) NOT NULL,
	is_deleted bool NOT NULL DEFAULT false
);


--   .d8888b.           888                                d8b          888    d8b
--  d88P  Y88b          888                                Y8P          888    Y8P
--  Y88b.               888                                             888
--   "Y888b.   888  888 88888b.  .d8888b   .d8888b 888d888 888 88888b.  888888 888  .d88b.  88888b.
--      "Y88b. 888  888 888 "88b 88K      d88P"    888P"   888 888 "88b 888    888 d88""88b 888 "88b
--        "888 888  888 888  888 "Y8888b. 888      888     888 888  888 888    888 888  888 888  888
--  Y88b  d88P Y88b 888 888 d88P      X88 Y88b.    888     888 888 d88P Y88b.  888 Y88..88P 888  888
--   "Y8888P"   "Y88888 88888P"   88888P'  "Y8888P 888     888 88888P"   "Y888 888  "Y88P"  888  888
--                                                             888
--                                                             888
--                                                             888
CREATE TABLE public.subscription(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	created_on timestamp NOT NULL DEFAULT NOW(),
	updated_on timestamp NOT NULL DEFAULT NOW(),
	canceled_on timestamp,
	deleted_on timestamp,
	id_offer uuid NOT NULL,
	id_trainee uuid NOT NULL
);

-- ALTER TABLE public.subscription DROP CONSTRAINT IF EXISTS trainee_fk CASCADE;
ALTER TABLE public.subscription ADD CONSTRAINT trainee_fk FOREIGN KEY (id_trainee)
REFERENCES public.trainee (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.subscription DROP CONSTRAINT IF EXISTS offer_fk CASCADE;
ALTER TABLE public.subscription ADD CONSTRAINT offer_fk FOREIGN KEY (id_offer)
REFERENCES public.offer (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.subscription DROP CONSTRAINT IF EXISTS subscription_uq1 CASCADE;
ALTER TABLE public.subscription ADD CONSTRAINT subscription_uq UNIQUE (id_offer,id_trainee);


--  88888888888 d8b                                 888
--      888     Y8P                                 888
--      888                                         888
--      888     888 88888b.d88b.   .d88b.  .d8888b  888888 .d88b.  88888b.
--      888     888 888 "888 "88b d8P  Y8b 88K      888   d88""88b 888 "88b
--      888     888 888  888  888 88888888 "Y8888b. 888   888  888 888  888
--      888     888 888  888  888 Y8b.          X88 Y88b. Y88..88P 888 d88P
--      888     888 888  888  888  "Y8888   88888P'  "Y888 "Y88P"  88888P"
--                                                                 888
--                                                                 888
--                                                                 888
CREATE TABLE public.time_stop(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	stop_on timestamp NOT NULL,
	reason text NOT NULL,
	created_on timestamp NOT NULL DEFAULT NOW(),
	id_subscription uuid NOT NULL
);


-- ALTER TABLE public.time_stop DROP CONSTRAINT IF EXISTS subscription_fk CASCADE;
ALTER TABLE public.time_stop ADD CONSTRAINT subscription_fk FOREIGN KEY (id_subscription)
REFERENCES public.subscription (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;



--   .d8888b.  888                                 8888888b.            .d888 d8b          d8b 888    d8b
--  d88P  Y88b 888                                 888  "Y88b          d88P"  Y8P          Y8P 888    Y8P
--  888    888 888                                 888    888          888                     888
--  888        888  8888b.  88888888 88888888      888    888  .d88b.  888888 888 88888b.  888 888888 888  .d88b.  88888b.
--  888        888     "88b    d88P     d88P       888    888 d8P  Y8b 888    888 888 "88b 888 888    888 d88""88b 888 "88b
--  888    888 888 .d888888   d88P     d88P        888    888 88888888 888    888 888  888 888 888    888 888  888 888  888
--  Y88b  d88P 888 888  888  d88P     d88P         888  .d88P Y8b.     888    888 888  888 888 Y88b.  888 Y88..88P 888  888
--   "Y8888P"  888 "Y888888 88888888 88888888      8888888P"   "Y8888  888    888 888  888 888  "Y888 888  "Y88P"  888  888
--
--
--
CREATE TABLE public.clazz_definition(
  id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
  start_from timestamp NOT NULL,
  end_at timestamp NOT NULL,
  active_from timestamp NOT NULL DEFAULT NOW(),
  active_till timestamp,
  name text NOT NULL,
  recurrence text NOT NULL,
  contingent smallint NOT NULL,
  created_on timestamp NOT NULL DEFAULT NOW(),
  updated_on timestamp NOT NULL DEFAULT NOW(),
  id_studio uuid NOT NULL,
  avatarurl text,
  description text,
  tags text,
  deleted_on timestamp,
	is_active boolean NOT NULL DEFAULT false,
	amount decimal(5,2) NOT NULL,
	vat decimal(5,2) NOT NULL,
	currency text NOT NULL
);

-- TODO
-- Partner Contract
-- ClazzDef Address (maxPriceClazz [3 Kategorien], revenueSingle %, revenueAbo CHF)
-- Anmelden sollte eine email auslösen (Vielen Dank für Ihre buchung Template)
-- Bis x Stunden vor dem Kurs Anmeldung möglich
-- Geld wird sofort abgebucht
-- Partner muss uns jeden Monat eine Rechnung stellen (könnte automatisch über Partner Tool erfolgen)

-- ALTER TABLE public.clazz DROP CONSTRAINT IF EXISTS studio_fk CASCADE;
ALTER TABLE public.clazz_definition ADD CONSTRAINT studio_fk FOREIGN KEY (id_studio)
REFERENCES public.studio (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

--   .d8888b.  888
--  d88P  Y88b 888
--  888    888 888
--  888        888  8888b.  88888888 88888888
--  888        888     "88b    d88P     d88P
--  888    888 888 .d888888   d88P     d88P
--  Y88b  d88P 888 888  888  d88P     d88P
--   "Y8888P"  888 "Y888888 88888888 88888888
--
--
--
CREATE TABLE public.clazz(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	start_from timestamp NOT NULL,
	end_at timestamp NOT NULL,
	created_on timestamp NOT NULL DEFAULT NOW(),
	updated_on timestamp NOT NULL DEFAULT NOW(),
	id_clazzdef uuid NOT NULL,
  CONSTRAINT uniq_clazz UNIQUE (start_from,end_at, id_clazzdef)
);

-- ALTER TABLE public.clazz DROP CONSTRAINT IF EXISTS clazzdef_fk CASCADE;
ALTER TABLE public.clazz ADD CONSTRAINT clazzdef_fk FOREIGN KEY (id_clazzdef)
REFERENCES public.clazz_definition (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;


-- DROP INDEX IF EXISTS public.clazz_clazz_def_id_idx CASCADE;
CREATE INDEX clazz_clazz_def_id_idx ON public.clazz
USING btree
(
  id_clazzdef ASC NULLS LAST
);

--  8888888b.                   d8b          888                    888    d8b
--  888   Y88b                  Y8P          888                    888    Y8P
--  888    888                               888                    888
--  888   d88P .d88b.   .d88b.  888 .d8888b  888888 888d888 8888b.  888888 888  .d88b.  88888b.
--  8888888P" d8P  Y8b d88P"88b 888 88K      888    888P"      "88b 888    888 d88""88b 888 "88b
--  888 T88b  88888888 888  888 888 "Y8888b. 888    888    .d888888 888    888 888  888 888  888
--  888  T88b Y8b.     Y88b 888 888      X88 Y88b.  888    888  888 Y88b.  888 Y88..88P 888  888
--  888   T88b "Y8888   "Y88888 888  88888P'  "Y888 888    "Y888888  "Y888 888  "Y88P"  888  888
--                          888
--                     Y8b d88P
--                      "Y88P"
CREATE TABLE public.registration(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	created_on timestamp NOT NULL DEFAULT NOW(),
	id_trainee uuid NOT NULL,
	id_clazz uuid NOT NULL
);

-- ALTER TABLE public.registration DROP CONSTRAINT IF EXISTS trainee_fk CASCADE;
ALTER TABLE public.registration ADD CONSTRAINT trainee_fk FOREIGN KEY (id_trainee)
REFERENCES public.trainee (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.registration DROP CONSTRAINT IF EXISTS clazz_fk CASCADE;
ALTER TABLE public.registration ADD CONSTRAINT clazz_fk FOREIGN KEY (id_clazz)
REFERENCES public.clazz (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.registration DROP CONSTRAINT IF EXISTS registration_uq CASCADE;
ALTER TABLE public.registration ADD CONSTRAINT registration_uq UNIQUE (id_clazz, id_trainee);


--  88888888888                                                  888    d8b
--      888                                                      888    Y8P
--      888                                                      888
--      888  888d888 8888b.  88888b.  .d8888b   8888b.   .d8888b 888888 888  .d88b.  88888b.
--      888  888P"      "88b 888 "88b 88K          "88b d88P"    888    888 d88""88b 888 "88b
--      888  888    .d888888 888  888 "Y8888b. .d888888 888      888    888 888  888 888  888
--      888  888    888  888 888  888      X88 888  888 Y88b.    Y88b.  888 Y88..88P 888  888
--      888  888    "Y888888 888  888  88888P' "Y888888  "Y8888P  "Y888 888  "Y88P"  888  888
--
--
--
CREATE TABLE public.transaction(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	amount decimal(5,2) NOT NULL,
	created_on timestamp NOT NULL DEFAULT NOW(),
	vat decimal(5,2) NOT NULL,
	paid_at timestamp,
	payment_transaction_id text,
	payment_status text,
	id_trainee uuid,
	id_clazz uuid
);

ALTER TABLE public.transaction ADD CONSTRAINT trainee_fk FOREIGN KEY (id_trainee)
REFERENCES public.trainee (id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE public.transaction ADD CONSTRAINT clazz_fk FOREIGN KEY (id_clazz)
REFERENCES public.clazz (id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;


--  888888b.   d8b 888 888
--  888  "88b  Y8P 888 888
--  888  .88P      888 888
--  8888888K.  888 888 888
--  888  "Y88b 888 888 888
--  888    888 888 888 888
--  888   d88P 888 888 888
--  8888888P"  888 888 888
--
--
--
CREATE TABLE public.bill(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	amount decimal(5,2) NOT NULL,
	created_on timestamp NOT NULL DEFAULT NOW(),
	vat decimal(5,2) NOT NULL,
	period_start timestamp NOT NULL,
	period_end timestamp NOT NULL,
	paid_at timestamp,
	payment_transaction_id text,
	payment_status text,
	id_subscription uuid
);

-- ALTER TABLE public.bill DROP CONSTRAINT IF EXISTS trainee_fk CASCADE;
ALTER TABLE public.bill ADD CONSTRAINT subscription_fk FOREIGN KEY (id_subscription)
REFERENCES public.subscription (id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;

--  888      .d88888b.   .d8888b.  8888888 888b    888      88888888888     d8888 888888b.   888      8888888888 .d8888b.
--  888     d88P" "Y88b d88P  Y88b   888   8888b   888          888        d88888 888  "88b  888      888       d88P  Y88b
--  888     888     888 888    888   888   88888b  888          888       d88P888 888  .88P  888      888       Y88b.
--  888     888     888 888          888   888Y88b 888          888      d88P 888 8888888K.  888      8888888    "Y888b.
--  888     888     888 888  88888   888   888 Y88b888          888     d88P  888 888  "Y88b 888      888           "Y88b.
--  888     888     888 888    888   888   888  Y88888          888    d88P   888 888    888 888      888             "888
--  888     Y88b. .d88P Y88b  d88P   888   888   Y8888          888   d8888888888 888   d88P 888      888       Y88b  d88P
--  88888888 "Y88888P"   "Y8888P88 8888888 888    Y888          888  d88P     888 8888888P"  88888888 8888888888 "Y8888P"
--
--
--
--  888                       d8b          d8b           .d888
--  888                       Y8P          Y8P          d88P"
--  888                                                 888
--  888      .d88b.   .d88b.  888 88888b.  888 88888b.  888888 .d88b.
--  888     d88""88b d88P"88b 888 888 "88b 888 888 "88b 888   d88""88b
--  888     888  888 888  888 888 888  888 888 888  888 888   888  888
--  888     Y88..88P Y88b 888 888 888  888 888 888  888 888   Y88..88P
--  88888888 "Y88P"   "Y88888 888 888  888 888 888  888 888    "Y88P"
--                        888
--                   Y8b d88P
--                    "Y88P"
CREATE TABLE public.login_info(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	provider_id text NOT NULL,
	provider_key text NOT NULL,
	last_used timestamp NOT NULL DEFAULT NOW(),
	expiration timestamp NOT NULL DEFAULT NOW(),
	fingerprint text,
	created_on timestamp NOT NULL DEFAULT NOW()
); 

-- DROP INDEX IF EXISTS public.idx_login_info_provider_key CASCADE;
CREATE INDEX idx_login_info_provider_key ON public.login_info
USING btree
(
	provider_id,
	provider_key
)	WITH (FILLFACTOR = 90);

--  88888888888              d8b                                 888                   d8b          d8b           .d888
--      888                  Y8P                                 888                   Y8P          Y8P          d88P"
--      888                                                      888                                             888
--      888  888d888 8888b.  888 88888b.   .d88b.   .d88b.       888  .d88b.   .d88b.  888 88888b.  888 88888b.  888888 .d88b.
--      888  888P"      "88b 888 888 "88b d8P  Y8b d8P  Y8b      888 d88""88b d88P"88b 888 888 "88b 888 888 "88b 888   d88""88b
--      888  888    .d888888 888 888  888 88888888 88888888      888 888  888 888  888 888 888  888 888 888  888 888   888  888
--      888  888    888  888 888 888  888 Y8b.     Y8b.          888 Y88..88P Y88b 888 888 888  888 888 888  888 888   Y88..88P
--      888  888    "Y888888 888 888  888  "Y8888   "Y8888       888  "Y88P"   "Y88888 888 888  888 888 888  888 888    "Y88P"
--                                                                                 888
--                                                                            Y8b d88P
--                                                                             "Y88P"                                           s
CREATE TABLE public.trainee_login_info(
  id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	created_on timestamp NOT NULL DEFAULT NOW(),
	id_trainee uuid NOT NULL,
	id_login_info uuid NOT NULL
);

-- ALTER TABLE public.trainee_login_info DROP CONSTRAINT IF EXISTS trainee_fk CASCADE;
ALTER TABLE public.trainee_login_info ADD CONSTRAINT trainee_fk FOREIGN KEY (id_trainee)
REFERENCES public.trainee (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.trainee_login_info DROP CONSTRAINT IF EXISTS trainee_login_info_trainee_uq CASCADE;
ALTER TABLE public.trainee_login_info ADD CONSTRAINT trainee_login_info_trainee_uq UNIQUE (id_trainee);

-- ALTER TABLE public.trainee_login_info DROP CONSTRAINT IF EXISTS login_info_fk CASCADE;
ALTER TABLE public.trainee_login_info ADD CONSTRAINT login_info_fk FOREIGN KEY (id_login_info)
REFERENCES public.login_info (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.trainee_login_info DROP CONSTRAINT IF EXISTS trainee_login_info_trainee_li_uq CASCADE;
ALTER TABLE public.trainee_login_info ADD CONSTRAINT trainee_login_info_trainee_li_uq UNIQUE (id_login_info);

--  88888888888              d8b                                                                                                        888      d8b           .d888
--      888                  Y8P                                                                                                        888      Y8P          d88P"
--      888                                                                                                                             888                   888
--      888  888d888 8888b.  888 88888b.   .d88b.   .d88b.       88888b.   8888b.  .d8888b  .d8888b  888  888  888  .d88b.  888d888 .d88888      888 88888b.  888888 .d88b.
--      888  888P"      "88b 888 888 "88b d8P  Y8b d8P  Y8b      888 "88b     "88b 88K      88K      888  888  888 d88""88b 888P"  d88" 888      888 888 "88b 888   d88""88b
--      888  888    .d888888 888 888  888 88888888 88888888      888  888 .d888888 "Y8888b. "Y8888b. 888  888  888 888  888 888    888  888      888 888  888 888   888  888
--      888  888    888  888 888 888  888 Y8b.     Y8b.          888 d88P 888  888      X88      X88 Y88b 888 d88P Y88..88P 888    Y88b 888      888 888  888 888   Y88..88P
--      888  888    "Y888888 888 888  888  "Y8888   "Y8888       88888P"  "Y888888  88888P'  88888P'  "Y8888888P"   "Y88P"  888     "Y88888      888 888  888 888    "Y88P"
--                                                               888
--                                                               888
--
CREATE TABLE public.trainee_password_info(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	id_login_info uuid NOT NULL,
	hasher text NOT NULL,
	password text NOT NULL,
	salt text,
	created_on timestamp NOT NULL DEFAULT NOW()
);

-- ALTER TABLE public.trainee_password_info DROP CONSTRAINT IF EXISTS trainee_login_info_fk CASCADE;
ALTER TABLE public.trainee_password_info ADD CONSTRAINT trainee_login_info_fk FOREIGN KEY (id_login_info)
REFERENCES public.login_info (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.trainee_password_info DROP CONSTRAINT IF EXISTS trainee_password_info_trainee_li_uq CASCADE;
ALTER TABLE public.trainee_password_info ADD CONSTRAINT trainee_password_info_trainee_li_uq UNIQUE (id_login_info);



--  8888888b.                  888                                   888                   d8b          d8b           .d888
--  888   Y88b                 888                                   888                   Y8P          Y8P          d88P"
--  888    888                 888                                   888                                             888
--  888   d88P 8888b.  888d888 888888 88888b.   .d88b.  888d888      888  .d88b.   .d88b.  888 88888b.  888 88888b.  888888 .d88b.
--  8888888P"     "88b 888P"   888    888 "88b d8P  Y8b 888P"        888 d88""88b d88P"88b 888 888 "88b 888 888 "88b 888   d88""88b
--  888       .d888888 888     888    888  888 88888888 888          888 888  888 888  888 888 888  888 888 888  888 888   888  888
--  888       888  888 888     Y88b.  888  888 Y8b.     888          888 Y88..88P Y88b 888 888 888  888 888 888  888 888   Y88..88P
--  888       "Y888888 888      "Y888 888  888  "Y8888  888          888  "Y88P"   "Y88888 888 888  888 888 888  888 888    "Y88P"
--                                                                                     888
--                                                                                Y8b d88P
--                                                                                 "Y88P"                                                                                                                     "Y88P"                                           s
CREATE TABLE public.partner_login_info(
  id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	created_on timestamp NOT NULL DEFAULT NOW(),
	id_partner uuid NOT NULL,
	id_login_info uuid NOT NULL
);

-- ALTER TABLE public.partner_login_info DROP CONSTRAINT IF EXISTS partner_fk CASCADE;
ALTER TABLE public.partner_login_info ADD CONSTRAINT partner_fk FOREIGN KEY (id_partner)
REFERENCES public.partner (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.partner_login_info DROP CONSTRAINT IF EXISTS partner_login_info_partner_uq CASCADE;
ALTER TABLE public.partner_login_info ADD CONSTRAINT partner_login_info_partner_uq UNIQUE (id_partner);

-- ALTER TABLE public.partner_login_info DROP CONSTRAINT IF EXISTS login_info_fk CASCADE;
ALTER TABLE public.partner_login_info ADD CONSTRAINT login_info_fk FOREIGN KEY (id_login_info)
REFERENCES public.login_info (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.partner_login_info DROP CONSTRAINT IF EXISTS partner_login_info_partner_li_uq CASCADE;
ALTER TABLE public.partner_login_info ADD CONSTRAINT partner_login_info_partner_li_uq UNIQUE (id_login_info);

--  8888888b.                  888                                                                                                          888
--  888   Y88b                 888                                                                                                          888
--  888    888                 888                                                                                                          888
--  888   d88P 8888b.  888d888 888888 88888b.   .d88b.  888d888      88888b.   8888b.  .d8888b  .d8888b  888  888  888  .d88b.  888d888 .d88888
--  8888888P"     "88b 888P"   888    888 "88b d8P  Y8b 888P"        888 "88b     "88b 88K      88K      888  888  888 d88""88b 888P"  d88" 888
--  888       .d888888 888     888    888  888 88888888 888          888  888 .d888888 "Y8888b. "Y8888b. 888  888  888 888  888 888    888  888
--  888       888  888 888     Y88b.  888  888 Y8b.     888          888 d88P 888  888      X88      X88 Y88b 888 d88P Y88..88P 888    Y88b 888
--  888       "Y888888 888      "Y888 888  888  "Y8888  888          88888P"  "Y888888  88888P'  88888P'  "Y8888888P"   "Y88P"  888     "Y88888
--                                                                   888
--                                                                   888
--                                                                   888
CREATE TABLE public.partner_password_info(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	id_login_info uuid NOT NULL,
	hasher text NOT NULL,
	password text NOT NULL,
	salt text,
	created_on timestamp NOT NULL DEFAULT NOW()
); 

-- ALTER TABLE public.partner_password_info DROP CONSTRAINT IF EXISTS partner_login_info_fk CASCADE;
ALTER TABLE public.partner_password_info ADD CONSTRAINT partner_login_info_fk FOREIGN KEY (id_login_info)
REFERENCES public.login_info (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.partner_password_info DROP CONSTRAINT IF EXISTS partner_password_info_partner_li_uq CASCADE;
ALTER TABLE public.partner_password_info ADD CONSTRAINT partner_password_info_partner_li_uq UNIQUE (id_login_info);

--   .d88888b.        d8888          888    888       .d8888b.       d8b           .d888
--  d88P" "Y88b      d88888          888    888      d88P  Y88b      Y8P          d88P"
--  888     888     d88P888          888    888             888                   888
--  888     888    d88P 888 888  888 888888 88888b.       .d88P      888 88888b.  888888 .d88b.
--  888     888   d88P  888 888  888 888    888 "88b  .od888P"       888 888 "88b 888   d88""88b
--  888     888  d88P   888 888  888 888    888  888 d88P"           888 888  888 888   888  888
--  Y88b. .d88P d8888888888 Y88b 888 Y88b.  888  888 888"            888 888  888 888   Y88..88P
--   "Y88888P" d88P     888  "Y88888  "Y888 888  888 888888888       888 888  888 888    "Y88P"
--
--
--
-- DROP TABLE IF EXISTS public.oauth2_info CASCADE;
CREATE TABLE public.oauth2_info(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	id_login_info uuid NOT NULL,
	access_token text NOT NULL,
	token_type text,
	expires_in integer,
	refresh_token text,
	created_on timestamp NOT NULL DEFAULT NOW()
); 


-- ALTER TABLE public.oauth2_info DROP CONSTRAINT IF EXISTS login_info_fk CASCADE;
ALTER TABLE public.oauth2_info ADD CONSTRAINT login_info_fk FOREIGN KEY (id_login_info)
REFERENCES public.login_info (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.oauth2_info DROP CONSTRAINT IF EXISTS oauth2_info_trainee_li_uq CASCADE;
ALTER TABLE public.oauth2_info ADD CONSTRAINT oauth2_info_trainee_li_uq UNIQUE (id_login_info);

--   .d88888b.        d8888          888    888       d888        d8b           .d888
--  d88P" "Y88b      d88888          888    888      d8888        Y8P          d88P"
--  888     888     d88P888          888    888        888                     888
--  888     888    d88P 888 888  888 888888 88888b.    888        888 88888b.  888888 .d88b.
--  888     888   d88P  888 888  888 888    888 "88b   888        888 888 "88b 888   d88""88b
--  888     888  d88P   888 888  888 888    888  888   888        888 888  888 888   888  888
--  Y88b. .d88P d8888888888 Y88b 888 Y88b.  888  888   888        888 888  888 888   Y88..88P
--   "Y88888P" d88P     888  "Y88888  "Y888 888  888 8888888      888 888  888 888    "Y88P"
--
--
--
-- DROP TABLE IF EXISTS public.oauth1_info CASCADE;
CREATE TABLE public.oauth1_info(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	token text NOT NULL,
	secret text NOT NULL,
	id_login_info uuid NOT NULL,
	created_on timestamp NOT NULL DEFAULT NOW()
); 

-- ALTER TABLE public.oauth1_info DROP CONSTRAINT IF EXISTS login_info_fk CASCADE;
ALTER TABLE public.oauth1_info ADD CONSTRAINT login_info_fk FOREIGN KEY (id_login_info)
REFERENCES public.login_info (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

-- ALTER TABLE public.oauth1_info DROP CONSTRAINT IF EXISTS oauth1_info_trainee_li_uq CASCADE;
ALTER TABLE public.oauth1_info ADD CONSTRAINT oauth1_info_trainee_li_uq UNIQUE (id_login_info);

--   .d88888b.                             d8b      888             d8888 888    888            d8b 888               888
--  d88P" "Y88b                            Y8P      888            d88888 888    888            Y8P 888               888
--  888     888                                     888           d88P888 888    888                888               888
--  888     888 88888b.   .d88b.  88888b.  888  .d88888          d88P 888 888888 888888 888d888 888 88888b.  888  888 888888 .d88b.  .d8888b
--  888     888 888 "88b d8P  Y8b 888 "88b 888 d88" 888         d88P  888 888    888    888P"   888 888 "88b 888  888 888   d8P  Y8b 88K
--  888     888 888  888 88888888 888  888 888 888  888        d88P   888 888    888    888     888 888  888 888  888 888   88888888 "Y8888b.
--  Y88b. .d88P 888 d88P Y8b.     888  888 888 Y88b 888       d8888888888 Y88b.  Y88b.  888     888 888 d88P Y88b 888 Y88b. Y8b.          X88
--   "Y88888P"  88888P"   "Y8888  888  888 888  "Y88888      d88P     888  "Y888  "Y888 888     888 88888P"   "Y88888  "Y888 "Y8888   88888P'
--              888
--              888
--              888
CREATE TABLE public.openidattributes(
  id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	key text NOT NULL,
	value text NOT NULL
); 


--   .d88888b.                             d8b      888      8888888           .d888
--  d88P" "Y88b                            Y8P      888        888            d88P"
--  888     888                                     888        888            888
--  888     888 88888b.   .d88b.  88888b.  888  .d88888        888   88888b.  888888 .d88b.
--  888     888 888 "88b d8P  Y8b 888 "88b 888 d88" 888        888   888 "88b 888   d88""88b
--  888     888 888  888 88888888 888  888 888 888  888        888   888  888 888   888  888
--  Y88b. .d88P 888 d88P Y8b.     888  888 888 Y88b 888        888   888  888 888   Y88..88P
--   "Y88888P"  88888P"   "Y8888  888  888 888  "Y88888      8888888 888  888 888    "Y88P"
--              888
--              888
--              888

CREATE TABLE public.openidinfo(
  id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	id_login_info uuid NOT NULL
);

-- ALTER TABLE public.openidinfo DROP CONSTRAINT IF EXISTS oidinfo_info_trainee_li_uq CASCADE;
ALTER TABLE public.openidinfo ADD CONSTRAINT oidinfo_info_trainee_li_uq UNIQUE (id_login_info);

-- ALTER TABLE public.openidinfo DROP CONSTRAINT IF EXISTS login_info_fk CASCADE;
ALTER TABLE public.openidinfo ADD CONSTRAINT oidinfo_info_login_info_fk FOREIGN KEY (id_login_info)
REFERENCES public.login_info (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;


--  888
--  888
--  888
--  888      .d88b.   .d88b.   .d88b.   .d88b.  888d888
--  888     d88""88b d88P"88b d88P"88b d8P  Y8b 888P"
--  888     888  888 888  888 888  888 88888888 888
--  888     Y88..88P Y88b 888 Y88b 888 Y8b.     888
--  88888888 "Y88P"   "Y88888  "Y88888  "Y8888  888
--                        888      888
--                   Y8b d88P Y8b d88P
--                    "Y88P"   "Y88P"
-- This Table is for error logging purpose. All errors will be logged here
-- and can be analyzed afterwards. To make the system stable.
CREATE TABLE public.logger(
	id uuid primary key NOT NULL DEFAULT uuid_generate_v4(),
	rootid text NOT NULL,
	title text NOT NULL,
	exception text NOT NULL,
	stacktrace text NOT NULL,
	req_header text NOT NULL,
	req_method text NOT NULL,
	req_address text NOT NULL,
	req_uri text NOT NULL,
	created_on timestamp NOT NULL DEFAULT NOW()
);

--CREATE EXTENSION "uuid-ossp";



--  888     888 8888888 8888888888 888       888  .d8888b.
--  888     888   888   888        888   o   888 d88P  Y88b
--  888     888   888   888        888  d8b  888 Y88b.
--  Y88b   d88P   888   8888888    888 d888b 888  "Y888b.
--   Y88b d88P    888   888        888d88888b888     "Y88b.
--    Y88o88P     888   888        88888P Y88888       "888
--     Y888P      888   888        8888P   Y8888 Y88b  d88P
--      Y8P     8888888 8888888888 888P     Y888  "Y8888P"
--
--
--

--   .d8888b.  888                                 888     888 d8b
--  d88P  Y88b 888                                 888     888 Y8P
--  888    888 888                                 888     888
--  888        888  8888b.  88888888 88888888      Y88b   d88P 888  .d88b.  888  888  888
--  888        888     "88b    d88P     d88P        Y88b d88P  888 d8P  Y8b 888  888  888
--  888    888 888 .d888888   d88P     d88P          Y88o88P   888 88888888 888  888  888
--  Y88b  d88P 888 888  888  d88P     d88P            Y888P    888 Y8b.     Y88b 888 d88P
--   "Y8888P"  888 "Y888888 88888888 88888888          Y8P     888  "Y8888   "Y8888888P"
--
--
--

CREATE VIEW clazz_view AS
  select c.id, c.start_from, c.end_at, cd.name, cd.contingent,
    cd.avatarurl, cd.description, cd.tags,
    concat('{',cd.name,'},',
					 '{',s.name,'},',
					 '{',adrS.city,'},',
					 '{',adrS.zip,'},',
           '{',cd.description,'},',
           '{',cd.tags,'}') as search_meta, nr_of_regs,
    cd.id AS id_clazzdef, s.id AS id_studio
  from (
         select c.id, c.start_from, c.end_at,
           c.created_on, c.updated_on, c.id_clazzdef,
           count(r.id_clazz) as nr_of_regs
         from clazz c
           left join registration r on r.id_clazz = c.id
         group by c.id) as c, clazz_definition cd, studio s, address adrS
  where c.id_clazzdef = cd.id
        and cd.id_studio = s.id
				and s.id_address = adrS.id;


-- # --- !Downs

-- DROP TABLE IF EXISTS public.logger CASCADE;
-- DROP TABLE IF EXISTS public.registration CASCADE;
-- DROP TABLE IF EXISTS public.clazz CASCADE;
-- DROP TABLE IF EXISTS public.studio CASCADE;
-- DROP TABLE IF EXISTS public.trainee_login_info CASCADE;
-- DROP TABLE IF EXISTS public.bill CASCADE;
-- DROP TABLE IF EXISTS public.time_stop CASCADE;
-- DROP TABLE IF EXISTS public.subscription CASCADE;
-- DROP TABLE IF EXISTS public.trainee CASCADE;
-- DROP TABLE IF EXISTS public.address CASCADE;
-- DROP TABLE IF EXISTS public.offer CASCADE;
-- DROP TABLE IF EXISTS public.password_info CASCADE;
-- DROP TABLE IF EXISTS public.oauth1_info CASCADE;
-- DROP TABLE IF EXISTS public.oauth2_info CASCADE;
-- DROP TABLE IF EXISTS public.openidinfo CASCADE;
-- DROP TABLE IF EXISTS public.openidattributes CASCADE;
-- DROP TABLE IF EXISTS public.login_info CASCADE;
