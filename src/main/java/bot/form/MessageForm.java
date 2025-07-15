package bot.form;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class MessageForm {
	private String name;
	private String message;
	private String referencedMessageId;
	private List<MultipartFile> multipartFileList;
	private String fileName;
}
