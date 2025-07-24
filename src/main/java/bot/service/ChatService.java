package bot.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bot.dto.AllianceMemberDto;
import bot.dto.ChatAttachmentDto;
import bot.dto.ChatMessageDto;
import bot.entity.ChatAttachment;
import bot.entity.ChatMessage;
import bot.model.discord.DIscordEventListener;
import bot.model.discord.DiscordModel;
import bot.repository.ChatAttachmentRepository;
import bot.repository.ChatMessageRepository;
import bot.util.prop.AppriCationProperties;

@Service
public class ChatService implements DIscordEventListener {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	@Autowired
	private DiscordModel discordModel;
	@Autowired
	private ChatAttachmentRepository chatAttachmentRepository;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	@Autowired
	private AppriCationProperties appriCationProperties;
	private List<ChatMessageDto> chatMessageDtoList = new ArrayList<>();

	public void init() {
		ModelMapper modelMapper = new ModelMapper();
		chatMessageDtoList = modelMapper.map(chatMessageRepository.findAllByOrderByIdDesc(),
				new TypeToken<List<ChatMessageDto>>() {
				}.getType());
		if (chatMessageDtoList == null)
			chatMessageDtoList = new ArrayList<>();
	}

	@Override
	@Transactional
	public void onMessageReceived(ChatMessageDto chatMessageDto) {
		if (appriCationProperties.getWebChannelId().equals(chatMessageDto.getChannelId())) {
			String message = chatMessageDto.getMessage().replace("\n", "<br>");
			chatMessageDto.setMessage(message);
			log.info("ChatService:メッセージ:" + chatMessageDto);

			// DB保存
			ModelMapper modelMapper = new ModelMapper();
			ChatMessage chatMessage = modelMapper.map(chatMessageDto, ChatMessage.class);
			// TODO ChatMessageを保存 配下のChatAttachmentも同時に保存できるはず。うまくいかなかったので暫定
			ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);
			chatMessageDto.setId(savedChatMessage.getId());
			chatMessageDtoList.addFirst(chatMessageDto);

			if (chatMessageDto.getChatAttachmentDtoList().size() != 0) {
				chatMessageDto.getChatAttachmentDtoList().forEach((chatAttachmentDto) -> {
					ChatAttachment chatAttachment = new ChatAttachment();
					chatAttachment.setAttachmentUrl(chatAttachmentDto.getAttachmentUrl());
					chatAttachment.setChatMessage(chatMessage);
					chatAttachment.setAttachmentFileName(chatAttachmentDto.getAttachmentFileName());
					savedChatMessage.getChatAttachmentList().add(chatAttachment);
					chatAttachmentRepository.save(chatAttachment);
				});
			}
		}
	}

	public ChatMessageDto getChatMessageDto(long id) {
		for (ChatMessageDto chatMessageDto : chatMessageDtoList) {
			if (chatMessageDto.getId() == id)
				return chatMessageDto;
		}
		return null;
	}

	public void sendMessage(ChatMessageDto chatMessageDto) {
		discordModel.sendMessage(chatMessageDto);
	}

	@Override
	public void onMessageUpdate(ChatMessageDto chatMessageDto) {
	}

	public List<ChatMessageDto> getChatMessageDtoList() {
		return chatMessageDtoList;
	}

	@Transactional
	public List<ChatMessageDto> getChatMessageDtoList(Pageable pageable) {
		List<ChatMessageDto> chatMessageDtoList = new ArrayList<>();
		List<ChatMessage> chatMessageList = chatMessageRepository.findAllByOrderByIdDesc(pageable).getContent();
		ModelMapper modelMapper = new ModelMapper();
		for (ChatMessage chatMessage : chatMessageList) {
			List<ChatAttachmentDto> chatAttachmentDtoList = new ArrayList<ChatAttachmentDto>();
			for (ChatAttachment chatAttachment : chatMessage.getChatAttachmentList()) {
				ChatAttachmentDto chatAttachmentDto = new ChatAttachmentDto();
				chatAttachmentDto.setAttachmentFileName(chatAttachment.getAttachmentFileName());
				chatAttachmentDto.setAttachmentUrl(chatAttachment.getAttachmentUrl());
				chatAttachmentDtoList.add(chatAttachmentDto);
			}
			ChatMessageDto chatMessageDto = modelMapper.map(chatMessage, ChatMessageDto.class);
			chatMessageDto.setChatAttachmentDtoList(chatAttachmentDtoList);
			chatMessageDtoList.add(chatMessageDto);
		}
		return chatMessageDtoList;
	}

	@Override
	public void onMessageDelete(String messageId) {
	}

	@Override
	public void onGuildMemberJoin(AllianceMemberDto allianceMemberDto) {
	}

	@Override
	public void onGuildMemberRemove(AllianceMemberDto allianceMemberDto) {
	}

}