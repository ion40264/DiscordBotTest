package bot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

import bot.DiscordBotTestApplication;
import bot.dto.ChatAttachmentDto;
import bot.dto.ChatMessageDto;
import bot.dto.MessageSizeDto;
import bot.form.MessageForm;
import bot.service.ChatService;

@RestController
@RequestMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatRestController {
	private static final Logger log = LoggerFactory.getLogger(ChatRestController.class);
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
			chatService.sendMessage(toChatMessageDtoFromForm(messageForm));
		} catch (Exception e) {
			log.error("メッセージの送信で失敗しました。messageForm=" + messageForm, e);
		}
	}

	private ChatMessageDto toChatMessageDtoFromForm(MessageForm messageForm) {
		ChatMessageDto chatMessageDto = new ChatMessageDto();
		chatMessageDto.setCreateDate(DiscordBotTestApplication.sdf.format(new Date()));
		chatMessageDto.setDiscordMessageId(null);
		chatMessageDto.setMessage(messageForm.getMessage());
		chatMessageDto.setName(messageForm.getName());
		if (messageForm.getReferencedMessageId() != null && !messageForm.getReferencedMessageId().isEmpty()) {
			ChatMessageDto refChatMessageDto = chatService.getChatMessageDto(Long.parseLong(messageForm.getReferencedMessageId().trim()));
			chatMessageDto.setQuoteDiscordId(refChatMessageDto.getDiscordMessageId());
			chatMessageDto.setQuoteId(refChatMessageDto.getId().toString());
		}
		
		if (messageForm.getMultipartFileList() == null)
			messageForm.setMultipartFileList(new ArrayList<>());
		List<ChatAttachmentDto> chatAttachmentDtoList = new ArrayList<ChatAttachmentDto>();
		messageForm.getMultipartFileList().forEach(multipartFile ->{
			ChatAttachmentDto chatAttachmentDto = new ChatAttachmentDto();
			try {
				chatAttachmentDto.setAttachmentFileInputStream(multipartFile.getInputStream());
				chatAttachmentDto.setAttachmentFileName(multipartFile.getName());
				chatAttachmentDto.setAttachmentUrl(null);
				chatAttachmentDtoList.add(chatAttachmentDto);
			} catch (IOException e) {
				log.error("添付ファイルの処理に失敗しました。", e);
			}
		});
		
		return chatMessageDto;
	}
}
