
/* Drop Tables */

DROP TABLE IF EXISTS fighting_strength;
DROP TABLE IF EXISTS alliance_member;
DROP TABLE IF EXISTS channel_master;
DROP TABLE IF EXISTS chat_attachment;
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS level_master;




/* Create Tables */

CREATE TABLE alliance_member
(
	id serial NOT NULL UNIQUE,
	discord_member_id varchar(200),
	discord_name varchar(200),
	ayarabu_id varchar(200),
	ayarabu_name varchar(200),
	alliance varchar(200),
	statement_count int,
	create_date varchar(200),
	member_role varchar(200),
	bot int,
	PRIMARY KEY (id)
) WITHOUT OIDS;


CREATE TABLE channel_master
(
	id serial NOT NULL,
	channel_name varchar(200),
	channel_id varchar(200),
	PRIMARY KEY (id)
) WITHOUT OIDS;


CREATE TABLE chat_attachment
(
	id serial NOT NULL,
	attachment_url varchar(500),
	chat_message_id int NOT NULL,
	attachment_file_name varchar(200),
	PRIMARY KEY (id)
) WITHOUT OIDS;


CREATE TABLE chat_message
(
	id serial NOT NULL,
	discord_message_id varchar(200) UNIQUE,
	quote_discord_id varchar(200),
	quote_id varchar(200),
	name varchar(200),
	message varchar(5000),
	create_date varchar(200),
	channel_master_id int,
	PRIMARY KEY (id)
) WITHOUT OIDS;


CREATE TABLE fighting_strength
(
	id serial NOT NULL,
	color varchar(100),
	enemy_level int,
	point int,
	member_id int NOT NULL,
	update_date varchar(200),
	PRIMARY KEY (id)
) WITHOUT OIDS;


CREATE TABLE level_master
(
	id serial NOT NULL,
	enemy_level int,
	PRIMARY KEY (id)
) WITHOUT OIDS;



/* Create Foreign Keys */

ALTER TABLE fighting_strength
	ADD FOREIGN KEY (member_id)
	REFERENCES alliance_member (id)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE chat_attachment
	ADD FOREIGN KEY (chat_message_id)
	REFERENCES chat_message (id)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;



