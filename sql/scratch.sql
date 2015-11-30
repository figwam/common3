select x2.x3, x2.x4, x2.x5, x2.x6, x2.x7, x2.x8, x2.x9, x2.x10, x2.x11, x2.x12, x2.x13, x2.x14 from (select x15."nr_of_regs" as x12, x15."end_at" as x5, x15."search_meta" as x11, x15."description" as x9, x15."contingent" as x7, x15."tags" as x10, x15."start_from" as x4, x15."avatarurl" as x8, x15."id" as x3, x15."id_studio" as x14, x15."id_clazzdef" as x13, x15."name" as x6 from (select x16."nr_of_regs" as "nr_of_regs", x16."end_at" as "end_at", x16."search_meta" as "search_meta", x16."description" as "description", x16."contingent" as "contingent", x16."tags" as "tags", x16."start_from" as "start_from", x16."avatarurl" as "avatarurl", x16."id" as "id", x16."id_studio" as "id_studio", x16."id_clazzdef" as "id_clazzdef", x16."name" as "name" from "clazz_view" x16 order by x16."start_from") x15 where ({fn lcase(x15."search_meta")} like '%%') limit 10) x2;

select * from clazz;

select * from trainee;

select * from clazz_definition;


INSERT INTO clazz_definition (id, active_from, active_till, start_from, end_at, name, recurrence,  contingent, avatarurl, description, id_studio)
VALUES (uuid_generate_v1(),'2014-09-23 14:00:00.000000','2250-09-23 15:00:00.000000',
        '2015-10-27 14:00:00.000000','2015-10-27 15:00:00.000000',
        'Calistenics 4 All','ONETIME','20',
        'https://www.ywcampls.org/_asset/x953xn/Fitness-page-image.jpg',
        'Calistencis outdor ist eine vielseitige Sportart mit eigenem Körpergewicht. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.',
        '28bf9ca3-9440-4ab1-9832-c5a72e734103');

INSERT INTO clazz_definition (id, active_from, active_till, start_from, end_at, name, recurrence,  contingent, avatarurl, description, id_studio)
VALUES (uuid_generate_v1(),'2014-09-23 14:00:00.000000','2250-09-23 15:00:00.000000',
        '2015-10-27 12:00:00.000000','2015-10-27 13:00:00.000000',
        'Tanzen','ONETIME','2',
        'https://www.ywcampls.org/_asset/x953xn/Fitness-page-image.jpg',
        'Tanzen zz',
        '28bf9ca3-9440-4ab1-9832-c5a72e734103');


INSERT INTO clazz_definition (id, active_from, active_till, start_from, end_at, name, recurrence,  contingent, avatarurl, description, id_studio)
VALUES (uuid_generate_v1(),'2014-09-23 14:00:00.000000','2250-09-23 15:00:00.000000',
        '2015-10-28 18:00:00.000000','2015-10-28 20:00:00.000000',
        'Mix Martial Arts / MMA','WEEKLY','20',
        'https://www.ywcampls.org/_asset/x953xn/Fitness-page-image.jpg',
        'MAA ist nur für gestörte',
        '9f0a9940-d923-4f43-8ab0-026f50326f5a');



INSERT INTO clazz_definition (id, active_from, active_till, start_from, end_at, name, recurrence,  contingent, avatarurl, description, id_studio)
VALUES (uuid_generate_v1(),'2015-11-01 14:00:00.000000','2250-09-23 15:00:00.000000',
        '2015-11-03 10:00:00.000000','2015-10-28 11:00:00.000000',
        'Zumba','WEEKLY','10',
        'https://www.ywcampls.org/_asset/x953xn/Fitness-page-image.jpg',
        'Zumba für pussies',
        '9f0a9940-d923-4f43-8ab0-026f50326f5a');


select * from subscription;

select * from offer;

UPDATE subscription set created_on = '2012-02-29 03:55:20.568219' WHERE id_trainee = 'bd5728fe-e720-496c-8975-aff46bfc5ef9';

select * from subscription;

Insert INTO time_stop (id, stop_on, reason, id_subscription) VALUES
  (uuid_generate_v1(), '2015-12-01 14:00:00.000000', 'urlaub', 'aee709fd-dade-44f6-868d-66b93f186365'),
  (uuid_generate_v1(), '2016-01-01 14:00:00.000000', 'urlaub2', 'aee709fd-dade-44f6-868d-66b93f186365');