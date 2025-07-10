package bot.service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bot.dto.ChatMessageDto;
import bot.dto.MemberDto;
import bot.entity.ChatAttachment;
import bot.entity.ChatMessage;
import bot.model.discord.DiscordModel;
import bot.repository.ChatAttachmentRepository;
import bot.repository.ChatMessageRepository;
import bot.util.discord.DiscordBot;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

@Service
public class ChatService extends DiscordModel {
	private static final Logger log = LoggerFactory.getLogger(ChatService.class);

	@Autowired
	private ChatAttachmentRepository chatAttachmentRepository;
	@Autowired
	private ChatMessageRepository chatMessageRepository;

	private List<ChatMessageDto> chatMessageDtoList= new ArrayList<>();

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
	public void onMessageReceived(MemberDto memberDto, String messageId, String message, String referencedMessageId,
			List<String> attachmentUrlList) {
		ChatMessageDto chatMessageDto = new ChatMessageDto();
		chatMessageDto.setAttachmentUrlList(attachmentUrlList);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/DD HH:mm:ss");
		chatMessageDto.setCreateDate(sdf.format(new Date()));
		chatMessageDto.setDiscordMessageId(messageId);
		chatMessageDto.setName(memberDto.getDiscordName());
		message = message.replace("\n", "<br>");
		chatMessageDto.setMessage(message);
		chatMessageDto.setQuoteDiscordId(referencedMessageId);
		chatMessageDto.setQuoteId(getReferencedMessageIdById(referencedMessageId));
		log.info("メッセージ:" + chatMessageDto);
		chatMessageDtoList.add(chatMessageDto);

		ModelMapper modelMapper = new ModelMapper();
		ChatMessage chatMessage = modelMapper.map(chatMessageDto, ChatMessage.class);

		// 添付ファイルを ChatMessage オブジェクトに追加
		if (attachmentUrlList != null && !attachmentUrlList.isEmpty()) {
			attachmentUrlList.forEach((url) -> {
				ChatAttachment chatAttachment = new ChatAttachment();
				chatAttachment.setAttachmentUrl(url);
				chatAttachment.setChatMessage(chatMessage);
				chatMessage.getChatAttachmentList().add(chatAttachment);
			});
		}

		// ChatMessage を保存
		ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

		if (attachmentUrlList != null && !attachmentUrlList.isEmpty()) {
			attachmentUrlList.forEach((url) -> {
				ChatAttachment chatAttachment = new ChatAttachment();
				chatAttachment.setAttachmentUrl(url);
				chatAttachment.setChatMessage(chatMessage);
				savedChatMessage.getChatAttachmentList().add(chatAttachment);
				chatAttachmentRepository.save(chatAttachment);
			});
		}

	}

	public List<ChatMessageDto> getChatMessageDtoList() {
		return chatMessageDtoList;
	}
	
	private String getIdByReferencedMessageId(Long id) {
		String result = null;
		for (ChatMessageDto chatMessageDto : chatMessageDtoList) {
			if (id != null && chatMessageDto.getId() == id) {
				result = chatMessageDto.getDiscordMessageId();
			}
		}
		return result;
	}
	
	private String getReferencedMessageIdById(String referencedMessageId) {
		String result = null;
		for (ChatMessageDto chatMessageDto : chatMessageDtoList) {
			if (chatMessageDto.getDiscordMessageId().equals(referencedMessageId)) {
				result = chatMessageDto.getId().toString();
			}
		}
		return result;
	}
	

	public void sendMessage(String name, String message, long referencedMessageId, InputStream inputStream,
			String fileName) {
		super.sendMessage(name, message, getIdByReferencedMessageId(referencedMessageId), inputStream, fileName);
	}

	@Transactional
	public List<ChatMessageDto> getChatMessageDtoList(Pageable pageable) {
		List<ChatMessageDto> chatMessageDtoList = new ArrayList<>();
		List<ChatMessage> chatMessageList = chatMessageRepository.findAllByOrderByIdDesc(pageable).getContent();
		ModelMapper modelMapper = new ModelMapper();
		for (ChatMessage chatMessage : chatMessageList) {
			List<String> urlList = new ArrayList<String>();
			for (ChatAttachment chatAttachment : chatMessage.getChatAttachmentList()) {
				urlList.add(chatAttachment.getAttachmentUrl());
			}
			ChatMessageDto chatMessageDto = modelMapper.map(chatMessage, ChatMessageDto.class);
			chatMessageDto.setAttachmentUrlList(urlList);
			chatMessageDtoList.add(chatMessageDto);
		}
		return chatMessageDtoList;
	}

	@Override
	public void setDiscordModel(DiscordBot discordBot) {
		super.setDiscordModel(discordBot);
	}


	@Override
	public void onMessageUpdate(MemberDto memberDto, String messageId, String message, String referencedMessageId,
			List<String> attachmentUrlList) {
		super.onMessageUpdate(memberDto, messageId, message, referencedMessageId, attachmentUrlList);
	}


	@Override
	public void onMessageDelete(String messageId) {
		super.onMessageDelete(messageId);
	}


	@Override
	public void onGuildMemberJoin(MemberDto memberDto) {
		super.onGuildMemberJoin(memberDto);
	}


	@Override
	public void onGuildMemberRemove(MemberDto memberDto) {
		super.onGuildMemberRemove(memberDto);
	}

	@Override
	public void sendMessage(String name, String message, String referencedMessageId, InputStream inputStream,
			String fileName) {
		super.sendMessage(name, message, referencedMessageId, inputStream, fileName);
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		super.onMessageDeleteModel(event);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		super.onMessageReceivedModel(event);
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		super.onMessageUpdateModel(event);
	}
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		super.onGuildMemberJoinModel(event);
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		super.onGuildMemberRemoveModel(event);
	}

}