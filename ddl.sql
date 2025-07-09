
/* Drop Tables */

DROP TABLE [chat_message];
DROP TABLE [DISCO_MEMBER];




/* Create Tables */

CREATE TABLE [chat_message]
(
	[id] integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	[message_id] text NOT NULL UNIQUE,
	[quote_id] text,
	[name] text NOT NULL,
	[message] text NOT NULL,
	[attachment_url] text,
	[create_date] text
);


CREATE TABLE [DISCO_MEMBER]
(
	[id] integer NOT NULL UNIQUE PRIMARY KEY AUTOINCREMENT,
	[name] text,
	[mcount] integer
);



