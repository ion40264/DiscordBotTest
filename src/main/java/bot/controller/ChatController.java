package bot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import bot.dto.ChatMessageDto;
import bot.service.ChatService;

@RestController
public class ChatController {
	@Autowired
	private ChatService chatService;
	@GetMapping("/chat")
	public List<ChatMessageDto> getChat() {
		return chatService.getChatMessageDtoList();
	}

}
