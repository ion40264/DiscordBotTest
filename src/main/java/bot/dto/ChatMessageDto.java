package bot.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
	private Long id;
	private String messageId;
	// 引用元のdiscordIDね
	private String quoteId;
	private String name;
	private String message;
	private String attachmentUrl;
	private String createDate;
}
