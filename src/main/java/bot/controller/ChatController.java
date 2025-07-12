package bot.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import bot.dto.ChatMessageDto;
import bot.dto.MessageSizeDto;
import bot.form.MessageForm;
import bot.service.ChatService;

@RestController
@RequestMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatController {
	private static final Logger log = LoggerFactory.getLogger(ChatController.class);
	@Autowired
	private ChatService chatService;

	@GetMapping("/pageable")
	public List<ChatMessageDto> getAllChatsPageable(@PageableDefault(size = 20) Pageable pageable) {
		return chatService.getChatMessageDtoList(pageable);
	}
	@GetMapping
	public List<ChatMessageDto> getAllChats() {
		return chatService.getChatMessageDtoList();
	}
	@GetMapping("/size")
	public MessageSizeDto getSize() {
		MessageSizeDto messageSizeDto = new MessageSizeDto();
		messageSizeDto.setSize( chatService.getChatMessageDtoList().size());
		return messageSizeDto;
	}


	@PostMapping
	public void postMessage(MessageForm messageForm) {
		try {
			Long referencedMessageId = 0L;
			MultipartFile multipartFile = messageForm.getMultipartFile();
			if (messageForm.getReferencedMessageId() != null && !messageForm.getReferencedMessageId().isEmpty())
				referencedMessageId = Long.parseLong(messageForm.getReferencedMessageId().trim());
			if (multipartFile == null) {
				chatService.sendMessage(messageForm.getName(), messageForm.getMessage(),
						referencedMessageId, null, null);
			} else {
				chatService.sendMessage(messageForm.getName(), messageForm.getMessage(),
						referencedMessageId, multipartFile.getInputStream(), messageForm.getFileName());
			}
		} catch (IOException e) {
			log.error("メッセージの送信で失敗しました。messageForm=" + messageForm, e);
		}
	}
}
