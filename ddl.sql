
/* Drop Tables */

DROP TABLE IF EXISTS  [DISCO_MEMBER];




/* Create Tables */

CREATE TABLE [DISCO_MEMBER]
(
	[id] integer NOT NULL UNIQUE PRIMARY KEY AUTOINCREMENT,
	[name] text,
	[mcount] integer
);



