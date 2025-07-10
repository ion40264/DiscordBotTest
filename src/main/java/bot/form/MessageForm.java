package bot.form;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class MessageForm {
	private String name;
	private String message;
	private String referencedMessageId;
	private MultipartFile multipartFile;
	private String fileName;
}
