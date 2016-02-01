-- # --- !Ups


CREATE TABLE public.temp(
  id serial NOT NULL,
  -- partner
  firstname text,
  lastname text,
  mobile text,
  phone text,
  email text,
  partner_avatarurl text,
  --address
  street text NOT NULL,
  zip text NOT NULL,
  state text NOT NULL,
  city text NOT NULL,
  country text NOT NULL,
  longitude decimal(9,6),
  latitude decimal(9,6),
  -- studio
  studio_name text NOT NULL,
  sporttype text,
  studio_description text,
  studio_avatarurl text,
  -- clazz def
  start_from timestamp NOT NULL,
  end_at timestamp NOT NULL,
  active_from timestamp NOT NULL DEFAULT NOW(),
  active_till timestamp,
  clazzdef_name text NOT NULL,
  recurrence text NOT NULL,
  contingent smallint NOT NULL,
  clazz_def_avatarurl text,
  clazzdef_description text
);

DO $$
DECLARE
  tables varchar[] := ARRAY[
  'subscription','registration','clazz','clazz_definition', 'studio','partner_password_info','partner_login_info',
  'partner','trainee_password_info','trainee_login_info','trainee','address','login_info',
  'bill', 'offer'];
  t varchar;

  lorem_ipsum varchar := 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.';

  nr_of_partners int;
  partner temp;

  sid uuid;

BEGIN

  INSERT INTO temp (firstname, lastname, mobile, phone, email, partner_avatarurl,
                    street, zip, state, city, country, longitude, latitude,
                    studio_name, sporttype, studio_description, studio_avatarurl,
                    active_from, active_till, start_from, end_at, clazzdef_name, recurrence, contingent, clazz_def_avatarurl, clazzdef_description)
    VALUES (
      'Alexander','Schamne', '0791112233','0312223333','alexander.schamne@gmail.com','https://lh3.googleusercontent.com/-tIf-c0HRvWo/AAAAAAAAAAI/AAAAAAAAAHc/sgaIELQzwcQ/photo.jpg',
      'Gesellschaftsstrasse 73','3012','Bern','Bern','Switzerland','46.954995','7.432121',
      'Alex Outdoor Crossfit','Crossfit',lorem_ipsum, 'http://www.leoneacademy.ch/wp-content/uploads/2013/02/logo.gif',
      '2014-09-23 14:00:00.000000','2250-09-23 15:00:00.000000','2015-10-10 14:00:00.000000','2015-10-10 15:00:00.000000','Calistenics 4 All','onetime','20','https://www.ywcampls.org/_asset/x953xn/Fitness-page-image.jpg', lorem_ipsum
    ),
      (
      'Stefan','Waldner','0791112233','0312223333','stefan@leone.ch','http://www.leoneacademy.ch/wp-content/uploads/2013/02/stefu_001.jpg',
      'Hallerstrasse 1','3012','Bern','Bern','Switzerland', '46.951679', '7.434954',
      'Leone Academy','Kickboxen',lorem_ipsum,'http://www.leoneacademy.ch/wp-content/uploads/2013/02/logo.gif',
      '2015-09-28 17:00:00.000000','2217-09-27 17:45:00.000000','2015-09-28 17:00:00.000000','2015-09-28 17:45:00.000000','Sandsack','weekly','3','https://www.ywcampls.org/_asset/x953xn/Fitness-page-image.jpg', lorem_ipsum
    ),
      (
      'Andreas','Bolt','0791112233','0312223333','andreas.bolt@gmail.com','https://bern.emchberger.ch/sites/default/files/styles/132x174sc/public/bolt_andreas_web.jpg',
      'Speichergasse 4','3011','Bern','Bern','Switzerland', '46.950300', '7.442698',
      'Bolt Muscle Gym','Crossfit',lorem_ipsum, 'http://www.leoneacademy.ch/wp-content/uploads/2013/02/logo.gif',
      '2015-10-05 17:00:00.000000','2218-09-27 17:45:00.000000','2015-09-25 16:00:00.000000','2015-09-25 17:30:00.000000', 'Crossfit','weekly','3','https://www.ywcampls.org/_asset/x953xn/Fitness-page-image.jpg', lorem_ipsum);

  select count(*) into nr_of_partners from temp;

  -- Delete all content first
  FOREACH t IN ARRAY tables
  LOOP
    --RAISE NOTICE 'DELETE FROM %', t;
    EXECUTE format('DELETE FROM %I', t);
  END LOOP;

  -- Angebote auf dem Portal
  INSERT INTO offer(id, name,nr_access,price, price_timestop)
    VALUES  ('e4011d11-208f-4495-837c-ede2be5918dd','small',4,59,9),
            ('3eb69ea1-8a74-4147-9eb1-4aa43dc0b96a','medium',6,89,9),
            ('cc131560-6678-4f5a-8765-8e30ca96fd04','large',8,119,9);

  --INSERT PARTNERS
  FOR i IN 1..nr_of_partners LOOP
    SELECT * INTO partner FROM temp WHERE id = i;
    WITH adr01 AS(
      INSERT INTO address (street, zip, state, city, country,longitude, latitude)
      VALUES (partner.street, partner.zip, partner.city, partner.state, partner.country,partner.longitude, partner.latitude)
      RETURNING id
    ), adr02 AS(
      INSERT INTO address (street, zip, state, city, country,longitude, latitude)
      VALUES (partner.street, partner.zip, partner.city, partner.state, partner.country,partner.longitude, partner.latitude)
      RETURNING id
    ), adr03 AS(
      INSERT INTO address (street, zip, state, city, country,longitude, latitude)
      VALUES (partner.street, partner.zip, partner.city, partner.state, partner.country,partner.longitude, partner.latitude)
      RETURNING id
    ), par01 AS(
      INSERT INTO partner (id_address, firstname, lastname, mobile, phone, email,username,fullname, avatarurl, revenue)
      SELECT id, partner.firstname,partner.lastname,partner.mobile,partner.phone,partner.email,partner.email,partner.firstname ||' ' || partner.lastname,partner.partner_avatarurl, 2
      FROM adr01
      RETURNING id
    ), stu01 AS (INSERT INTO studio (id_address, id_partner, name, mobile, phone, email, sporttype, avatarurl,description)
      SELECT a.id, p.id, partner.studio_name,partner.mobile,partner.phone,partner.email,
        partner.sporttype,partner.studio_avatarurl,partner.studio_description
      FROM par01 p, adr02 a
      RETURNING id
    ), cld01 AS (
      INSERT INTO clazz_definition (id_studio, active_from, active_till, start_from, end_at, name, recurrence,  contingent, avatarurl, description, amount, vat, currency)
      SELECT id, partner.active_from,partner.active_till,partner.start_from,partner.end_at,partner.clazzdef_name,partner.recurrence,partner.contingent,partner.clazz_def_avatarurl, partner.clazzdef_description, 10,0.8, 'CHF'
      FROM stu01
      RETURNING id
    ),lin01 AS (
      INSERT INTO login_info (provider_id, provider_key)
      VALUES ('credentials', partner.email)
      RETURNING id
    ), plin01 AS (
      INSERT INTO partner_login_info (id_partner, id_login_info)
      SELECT p.id, l.id
      FROM par01 p, lin01 l
      RETURNING id
    ), ppw01 AS(
      INSERT INTO partner_password_info (id_login_info, hasher, password)
      SELECT id, 'bcrypt', '$2a$10$IJsMs8yzNR4Twwh1JpQBUej8bKRUtnOLX2IVVU9JiGN06ru2keyLy'
      FROM lin01
    ), tra01 AS(
      INSERT INTO trainee (id_address, firstname, lastname, mobile, phone, email,username,fullname, avatarurl)
        SELECT id, partner.firstname,partner.lastname,partner.mobile,partner.phone,partner.email,partner.email,partner.firstname ||' ' || partner.lastname,partner.partner_avatarurl
        FROM adr03
      RETURNING id
    ),lin02 AS (
      INSERT INTO login_info (provider_id, provider_key)
      VALUES ('credentials', partner.email)
      RETURNING id
    ), tlin01 AS (
      INSERT INTO trainee_login_info (id_trainee, id_login_info)
        SELECT t.id, l.id
        FROM tra01 t, lin02 l
      RETURNING id
    ), tpw01 AS(
      INSERT INTO trainee_password_info (id_login_info, hasher, password)
        SELECT id, 'bcrypt', '$2a$10$IJsMs8yzNR4Twwh1JpQBUej8bKRUtnOLX2IVVU9JiGN06ru2keyLy'
        FROM lin02
    ), sub01 AS(
      INSERT INTO subscription(id_offer, id_trainee)
        SELECT o.id, t.id
        FROM tra01 t, offer o
        WHERE o.nr_access = ((i-1)*2)+4
      RETURNING id
    ), bil01 AS(INSERT INTO bill(id_subscription, amount, vat, period_start, period_end, paid_at, payment_transaction_id,payment_status)
        SELECT id, 59, 4.72, current_timestamp - 2 * interval '1 month', current_timestamp - 1 * interval '1 month', NOW(), 'transaction_id_1234', 'paid'
        FROM sub01
    ), bil02 AS(INSERT INTO bill(id_subscription, amount, vat, period_start, period_end, paid_at, payment_transaction_id,payment_status)
        SELECT id, 59, 4.72, current_timestamp - 3 * interval '1 month', current_timestamp - 2 * interval '1 month', NOW(), 'transaction_id_1234', 'paid'
        FROM sub01
    )
    INSERT INTO bill(id_subscription, amount, vat, period_start, period_end, paid_at, payment_transaction_id,payment_status)
        SELECT id, 59, 4.72, NOW(), current_timestamp + 1 * interval '1 month', NOW(), 'transaction_id_1234', 'paid'
        FROM sub01;
  END LOOP;


  select s.id into sid from partner p, studio s where p.id = s.id_partner limit 1;

  FOR i IN 1..10 LOOP
    SELECT * INTO partner FROM temp WHERE id = ((i % nr_of_partners)+1);
    INSERT INTO clazz_definition (id_studio, active_from, active_till, start_from, end_at, name, recurrence,  contingent, avatarurl, description, amount, vat, currency)
    VALUES (sid, partner.active_from,partner.active_till,partner.start_from,partner.end_at,partner.clazzdef_name,partner.recurrence,partner.contingent,partner.clazz_def_avatarurl, partner.clazzdef_description, 10, 0.8, 'CHF');
  END LOOP;

END;
$$;


DROP TABLE public.temp CASCADE;



-- # --- !Downs

-- DELETE FROM studio;
-- DELETE FROM partner;
-- DELETE FROM address;
