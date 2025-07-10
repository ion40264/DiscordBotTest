package bot.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChatMessageDto {
	private Long id;
	private String discordMessageId;
	private String quoteId;
	// 引用元のdiscordIDね
	private String quoteDiscordId;
	private String name;
	private String message;
	private List<String> attachmentUrlList;
	private String createDate;
}
