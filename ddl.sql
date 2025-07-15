
/* Drop Tables */

DROP TABLE [alliance_member];
DROP TABLE [chat_attachment];
DROP TABLE [chat_message];




/* Create Tables */

CREATE TABLE [alliance_member]
(
	[id] integer NOT NULL UNIQUE PRIMARY KEY AUTOINCREMENT,
	[discord_member_id] text,
	[discord_name] text,
	[ayarabu_id] text,
	[ayarabu_name] text,
	[alliance] text,
	[statement_count] integer,
	[create_date] text,
	-- リーダーとか
	[member_role] text,
	-- 1がボット、0が普通
	[bot] integer
);


CREATE TABLE [chat_message]
(
	[id] integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	[discord_message_id] text UNIQUE,
	[quote_discord_id] text,
	[quote_id] text,
	[name] text,
	[message] text,
	[create_date] text,
	[channel_name] text,
	[channel_id] text
);


CREATE TABLE [chat_attachment]
(
	[id] integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	[attachment_url] text,
	[chat_message_id] integer NOT NULL,
	[attachment_file_name] text,
	FOREIGN KEY ([chat_message_id])
	REFERENCES [chat_message] ([id])
);



