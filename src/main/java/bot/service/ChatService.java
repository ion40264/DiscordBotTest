package bot.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bot.dto.AllianceMemberDto;
import bot.dto.ChatAttachmentDto;
import bot.dto.ChatMessageDto;
import bot.entity.Channel;
import bot.entity.ChatAttachment;
import bot.entity.ChatMessage;
import bot.model.discord.DIscordEventListener;
import bot.model.discord.DiscordModel;
import bot.repository.ChannelRepository;
import bot.repository.ChatAttachmentRepository;
import bot.repository.ChatMessageRepository;

@Service
public class ChatService implements DIscordEventListener {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	@Autowired
	private DiscordModel discordModel;
	@Autowired
	private ChannelRepository channelRepository;
	@Autowired
	private ChatAttachmentRepository chatAttachmentRepository;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	// TODO DBメモリともに無制限はまずい
	private List<ChatMessageDto> chatMessageDtoList = new ArrayList<>();

	public void init() {
		ModelMapper modelMapper = new ModelMapper();
		List<Channel> channelList = channelRepository.findAll();
		channelList.forEach(channel->{
			PageRequest pageable = PageRequest.of(0, 200);
			Page<ChatMessage> page = chatMessageRepository.findByChannelIdContaining(channel.getChannelId(), pageable);
			List<ChatMessageDto> chatMessageDtoList = modelMapper.map(page.getContent(),new TypeToken<List<ChatMessageDto>>() {
					}.getType());
			chatMessageDtoList.forEach(chatMessageDto->{
				chatMessageDto.setChannelId(channel.getChannelId());
				chatMessageDto.setChannelName(channel.getChannelName());
			});
			this.chatMessageDtoList.addAll(chatMessageDtoList);
		});
	}

	@Override
	@Transactional
	public void onMessageReceived(ChatMessageDto chatMessageDto) {
		Channel channel = channelRepository.findByChannelId(chatMessageDto.getChannelId());
		String message = chatMessageDto.getMessage().replace("\n", "<br>");
		chatMessageDto.setMessage(message);
		log.info("ChatService:メッセージ:" + chatMessageDto);

		// DB保存
		ModelMapper modelMapper = new ModelMapper();
		ChatMessage chatMessage = modelMapper.map(chatMessageDto, ChatMessage.class);
		chatMessage.setChannelId(channel.getChannelId());
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

	public List<ChatMessageDto> getChatMessageDtoList(String channelId) {
		List<ChatMessageDto> result = new ArrayList<ChatMessageDto>();
		chatMessageDtoList.forEach(chatMessageDto->{
			if (chatMessageDto.getChannelId().equals(channelId))
				result.add(chatMessageDto);
		});
		return result;
	}

	@Transactional
	public List<ChatMessageDto> getChatMessageDtoList(String channelId, Pageable pageable) {
		Channel channel = channelRepository.findByChannelId(channelId);
		List<ChatMessageDto> chatMessageDtoList = new ArrayList<>();
		Page<ChatMessage> chatMessagePage = chatMessageRepository.findByChannelIdContaining(channelId, pageable);
		ModelMapper modelMapper = new ModelMapper();
		for (ChatMessage chatMessage : chatMessagePage) {
			List<ChatAttachmentDto> chatAttachmentDtoList = new ArrayList<ChatAttachmentDto>();
			for (ChatAttachment chatAttachment : chatMessage.getChatAttachmentList()) {
				ChatAttachmentDto chatAttachmentDto = new ChatAttachmentDto();
				chatAttachmentDto.setAttachmentFileName(chatAttachment.getAttachmentFileName());
				chatAttachmentDto.setAttachmentUrl(chatAttachment.getAttachmentUrl());
				chatAttachmentDtoList.add(chatAttachmentDto);
			}
			ChatMessageDto chatMessageDto = modelMapper.map(chatMessage, ChatMessageDto.class);
			chatMessageDto.setChannelId(channelId);
			chatMessageDto.setChannelName(channel.getChannelName());
			chatMessageDto.setChatAttachmentDtoList(chatAttachmentDtoList);
			chatMessageDtoList.add(chatMessageDto);
		}
		chatMessageDtoList.sort(Comparator.comparing(ChatMessageDto::getDiscordMessageId).reversed());

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