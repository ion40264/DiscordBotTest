INSERT INTO alliance_member (discord_member_id,discord_name,ayarabu_id,ayarabu_name,alliance,statement_count,create_date,member_role,bot) VALUES
	 ('100','ion','90896709','イオン','HONTO_HOKKORI',0,'2025/07/30 18:03:56','SUB_LEADER',0),
	 ('101','サテライザー','','サテライザー','NONE',0,'2025/07/30 18:03:56','LEADER',0),
	 ('102','Fine','','Fine','HONTO_HOKKORI',0,'2025/07/30 18:03:56','SUB_LEADER',0),
	 ('103','test用bot','','test用bot','NONE',0,'2025/07/30 18:03:56','LEADER',0);
INSERT INTO channel (channel_name,channel_id) VALUES
	 ('web同期用チャンネル','1392405356076073004');
INSERT INTO chat_message (discord_message_id,quote_discord_id,quote_id,name,message,create_date,channel_id) VALUES
	 ('10000',NULL,NULL,'test用bot','ボットさんの発言：<br>ボット起動','2025/07/30 09:03:56','1392405356076073004'),
	 ('10001',NULL,NULL,'サテライザー','サテライザーさんの発言：<br>テスト','2025/07/15 03:09:45','1392405356076073004'),
	 ('10002',NULL,NULL,'サテライザー','こっちに書いたらどうなるかテスト','2025/07/15 06:32:35','1392405356076073004');
INSERT INTO chat_attachment (attachment_url,chat_message_id,attachment_file_name) VALUES
	 ('a.png',2,'name.png');