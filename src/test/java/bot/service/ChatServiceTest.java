package bot.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import bot.dto.ChatAttachmentDto;
import bot.dto.ChatMessageDto;

@Sql(scripts = "classpath:ddl.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:default.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
class ChatServiceTest {
	
	@Autowired
	private ChatService chatService;
	
	@Test
	public void testInit() {
		chatService.init();
		List<ChatMessageDto> chatMessageDtoList = chatService.getChatMessageDtoList("1392405356076073004");
		ChatMessageDto chatMessageDto = chatMessageDtoList.get(0);
		assertEquals("1392405356076073004", chatMessageDto.getChannelId());
		assertEquals("web同期用チャンネル", chatMessageDto.getChannelName());
		assertEquals("2025/07/30 09:03:56", chatMessageDto.getCreateDate());
		assertEquals("10000", chatMessageDto.getDiscordMessageId());
		assertEquals(1L, chatMessageDto.getId().longValue());
		assertEquals("ボットさんの発言：<br>ボット起動", chatMessageDto.getMessage());
		assertEquals("test用bot", chatMessageDto.getName());
		assertEquals(null, chatMessageDto.getQuoteDiscordId());
		assertEquals(null, chatMessageDto.getQuoteId());
	}

	@Test
	void testOnMessageReceived() {
		chatService.onMessageDelete("1");
	}

	@Test
	void testGetChatMessageDto() {
		ChatMessageDto chatMessageDto = chatService.getChatMessageDto(1);
		assertEquals("1392405356076073004", chatMessageDto.getChannelId());
		assertEquals("web同期用チャンネル", chatMessageDto.getChannelName());
		assertEquals("2025/07/30 09:03:56", chatMessageDto.getCreateDate());
		assertEquals("10000", chatMessageDto.getDiscordMessageId());
		assertEquals(1L, chatMessageDto.getId().longValue());
		assertEquals("ボットさんの発言：<br>ボット起動", chatMessageDto.getMessage());
		assertEquals("test用bot", chatMessageDto.getName());
		assertEquals(null, chatMessageDto.getQuoteDiscordId());
		assertEquals(null, chatMessageDto.getQuoteId());
	}

	@Test
	void testSendMessage() throws Exception {
		ChatMessageDto chatMessageDto = new ChatMessageDto();
		chatMessageDto.setChannelId("1392405356076073004");
		chatMessageDto.setChannelName("web同期用チャンネル");
		List<ChatAttachmentDto> attachmentDtoList = new ArrayList<ChatAttachmentDto>();
		ChatAttachmentDto chatAttachmentDto;
		chatAttachmentDto = new ChatAttachmentDto();
		chatAttachmentDto.setAttachmentFileInputStream(new FileInputStream(new File("src\\test\\java\\bot\\service\\test.png")));
		chatAttachmentDto.setAttachmentFileName("test.png");
		chatAttachmentDto.setAttachmentUrl("https://www.gstatic.com/marketing-cms/assets/images/f5/d3/a7f9db7045429cb6dc6be56bdcbe/google-logo-about.svg");
		attachmentDtoList.add(chatAttachmentDto);
		chatAttachmentDto = new ChatAttachmentDto();
		chatAttachmentDto.setAttachmentFileInputStream(new FileInputStream(new File("src\\test\\java\\bot\\service\\test.png")));
		chatAttachmentDto.setAttachmentFileName("test.png");
		chatAttachmentDto.setAttachmentUrl("https://www.gstatic.com/marketing-cms/assets/images/f5/d3/a7f9db7045429cb6dc6be56bdcbe/google-logo-about.svg");
		attachmentDtoList.add(chatAttachmentDto);
		chatMessageDto.setChatAttachmentDtoList(attachmentDtoList);
		chatMessageDto.setCreateDate("2025/07/31 00:00:00");
		chatMessageDto.setDiscordMessageId("1");
		chatMessageDto.setMessage("テスト");
		chatMessageDto.setName("テスト名");
		chatMessageDto.setQuoteDiscordId(null);
		chatMessageDto.setQuoteId(null);
		chatService.sendMessage(chatMessageDto);
		Thread.sleep(1000);
		ChatMessageDto result = chatService.getChatMessageDto(4L);
		assertEquals("テスト名さんの発言：<br>テスト", result.getMessage());
	}

	@Test
	void testOnMessageUpdate() {
		chatService.onMessageDelete("1");
	}

	@Test
	void testGetChatMessageDtoList() {
		List<ChatMessageDto> chatMessageDtoList = chatService.getChatMessageDtoList("1392405356076073004");
		assertEquals(3, chatMessageDtoList.size());
	}

	@Test
	void testGetChatMessageDtoListStringPageable() {
		PageRequest pageable = PageRequest.of(0, 2);
		List<ChatMessageDto> chatMessageDtoList = chatService.getChatMessageDtoList("1392405356076073004", pageable);
		assertEquals(2, chatMessageDtoList.size());
		ChatMessageDto chatMessageDto;
		chatMessageDto = chatMessageDtoList.get(0);
		assertEquals("サテライザーさんの発言：<br>テスト", chatMessageDto.getMessage());
		chatMessageDto = chatMessageDtoList.get(1);
		assertEquals("ボットさんの発言：<br>ボット起動", chatMessageDto.getMessage());
	}

	@Test
	void testOnMessageDelete() {
	}

	@Test
	void testOnGuildMemberJoin() {
	}

	@Test
	void testOnGuildMemberRemove() {
	}

}
