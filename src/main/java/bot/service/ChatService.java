package bot.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bot.dto.ChatMessageDto;
import bot.dto.MemberDto;
import bot.entity.ChatMessage;
import bot.repository.ChatMessageRepository;
import bot.util.discord.DiscordBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Service
public class ChatService extends ListenerAdapter {
	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	@Autowired
	private DiscordBot discordBot;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	private List<ChatMessageDto> chatMessageDtoList;

	public void init() {
		ModelMapper modelMapper = new ModelMapper();
		chatMessageDtoList = modelMapper.map(chatMessageRepository.findAll(),
				new TypeToken<List<ChatMessageDto>>() {
				}.getType());
	}

	@Override
	@Transactional
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;

		Member member = event.getMember();
		Message discoMessage = event.getMessage();
		List<Attachment> attachments = discoMessage.getAttachments();
		String url = null;
		if (attachments != null && !attachments.isEmpty()) {
			Attachment attachment = attachments.getFirst();
			url = attachment.getUrl();
		}
		MemberDto memberDto = discordBot.getMemberDto(member.getId());
		String message = event.getMessage().getContentRaw();
		ChatMessageDto chatMessageDto = new ChatMessageDto();
		chatMessageDto.setAttachmentUrl(url);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/DD HH:mm:ss");
		chatMessageDto.setCreateDate(sdf.format(new Date()));
		chatMessageDto.setMessageId(event.getMessageId());
		chatMessageDto.setName(memberDto.getNickname());
		chatMessageDto.setMessage(message);
		log.info("メッセージ:" + chatMessageDto);
		chatMessageDtoList.add(chatMessageDto);
		ModelMapper modelMapper = new ModelMapper();
		chatMessageRepository.save(modelMapper.map(chatMessageDto, ChatMessage.class));
	}

	public List<ChatMessageDto> getChatMessageDtoList() {
		return chatMessageDtoList;
	}

}
