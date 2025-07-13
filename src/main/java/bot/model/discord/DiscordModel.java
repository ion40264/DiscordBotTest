package bot.model.discord;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bot.dto.AllianceMemberDto;
import bot.entity.ChatAttachment;
import bot.entity.ChatMessage;
import bot.repository.ChatAttachmentRepository;
import bot.repository.ChatMessageRepository;
import bot.util.discord.DiscordBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class DiscordModel extends ListenerAdapter {
	private static final Logger log = LoggerFactory.getLogger(DiscordModel.class);
	@Autowired
	private DiscordBot discordBot;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	@Autowired
	private ChatAttachmentRepository chatAttachmentRepository;
	private List<DIscordEventListener> dIscordEventListenerList = new ArrayList<DIscordEventListener>();

	public void init() {
		getHistory(100);
	}

	private boolean endFlag = false;
	private List<Message> messageList;

	private void getHistory(int limit) {
		endFlag = true;
		String messageId;
		sendMessage("ボット", "ボット起動", null, null, null);
		// TODO 気に食わない
		try {
			Thread.sleep(500L);
		} catch (InterruptedException e) {
		}
		Optional<ChatMessage> optional = chatMessageRepository.findById(1L);
		if (!optional.isEmpty()) {
			messageId = optional.get().getDiscordMessageId();
			discordBot.getWebTextChannel().getHistoryBefore(messageId, limit).queue(
					history -> {
						messageList = history.getRetrievedHistory();
						log.info("基準メッセージ (" + messageId + ") より前のメッセージ " + messageList.size()
								+ " 件を取得しました。");
						endFlag = false;

					});
			// TODO すっげーいやな書き方。。
			while (endFlag) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		} else {
			messageList = discordBot.getWebTextChannel().getHistory().getRetrievedHistory();
			messageId = "";
		}

		// 取得したメッセージのリストは、古い順に並んでいるのでソート
		List<Message> sortMessageList = new ArrayList<>();
		sortMessageList.addAll(messageList);
		sortMessageList.sort(Comparator.comparing(Message::getIdLong));
		for (Message message : sortMessageList) {
			if (chatMessageRepository.findByDiscordMessageId(message.getId()) != null)
				continue;
			ChatMessage chatMessage = new ChatMessage();
			// TODO 日付の文字列型変換は共通に抜き出したい
			chatMessage
					.setCreateDate(message.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
			chatMessage.setDiscordMessageId(message.getId());
			chatMessage.setMessage(message.getContentRaw());
			chatMessage.setName(message.getAuthor().getName());
			if (message.getReferencedMessage() != null)
				chatMessage.setQuoteDiscordId(message.getReferencedMessage().getId());
			chatMessage.setQuoteId(null);
			chatMessageRepository.save(chatMessage);
			message.getAttachments().forEach((attachment) -> {
				ChatAttachment chatAttachment = new ChatAttachment();
				chatAttachment.setAttachmentUrl(attachment.getUrl());
				chatAttachment.setChatMessage(chatMessage);
				chatAttachment.setAttachmentFileName(attachment.getFileName());
				chatAttachmentRepository.save(chatAttachment);
			});
			ChatMessage quoteChatMessage = chatMessageRepository
					.findByDiscordMessageId(chatMessage.getQuoteDiscordId());
			if (quoteChatMessage != null)
				chatMessage.setQuoteId(quoteChatMessage.getId().toString());
			chatMessageRepository.save(chatMessage);
		}
	}
	
	public void adddIscordEventListener(DIscordEventListener dIscordEventListener) {
		dIscordEventListenerList.add(dIscordEventListener);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Member member = event.getMember();
		Message discoMessage = event.getMessage();
		List<Attachment> attachmentList = discoMessage.getAttachments();
		List<String> urlList = new ArrayList<String>();
		if (attachmentList != null && !attachmentList.isEmpty()) {
			attachmentList.forEach((s) -> {
				urlList.add(s.getUrl());
			});
		}
		AllianceMemberDto allianceMemberDto = discordBot.getAllianceMemberDto(member.getId());
		if (allianceMemberDto == null)
			allianceMemberDto = new AllianceMemberDto();
		allianceMemberDto.setBot(event.getAuthor().isBot());
		String message = event.getMessage().getContentDisplay();
		Message referencedMessage = discoMessage.getReferencedMessage();
		String referencedMessageId = null;
		if (referencedMessage != null)
			referencedMessageId = referencedMessage.getId();
		for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
			dIscordEventListener.onMessageReceived(allianceMemberDto, discoMessage.getId(), message,
					referencedMessageId,
					urlList);

		}
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		Member member = event.getMember();
		Message discoMessage = event.getMessage();
		List<Attachment> attachmentList = discoMessage.getAttachments();
		List<String> urlList = new ArrayList<String>();
		if (attachmentList != null && !attachmentList.isEmpty()) {
			attachmentList.forEach((s) -> {
				urlList.add(s.getUrl());
			});
		}
		AllianceMemberDto allianceMemberDto = discordBot.getAllianceMemberDto(member.getId());
		allianceMemberDto.setBot(event.getAuthor().isBot());
		String message = event.getMessage().getContentRaw();
		Message referencedMessage = discoMessage.getReferencedMessage();
		String referencedMessageId = null;
		if (referencedMessage != null)
			referencedMessageId = referencedMessage.getId();
		for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
			dIscordEventListener.onMessageUpdate(allianceMemberDto, discoMessage.getId(), message,
					referencedMessageId,
					urlList);

		}

	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
			dIscordEventListener
					.onMessageDelete(event.getMessageId());
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Member member = event.getMember();
		AllianceMemberDto allianceMemberDto = discordBot.getAllianceMemberDto(member.getId());
		allianceMemberDto.setBot(false);
		for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
			dIscordEventListener
					.onGuildMemberJoin(allianceMemberDto);
		}
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		Member member = event.getMember();
		AllianceMemberDto allianceMemberDto = discordBot.getAllianceMemberDto(member.getId());
		allianceMemberDto.setBot(false);
		for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
			dIscordEventListener
					.onGuildMemberRemove(allianceMemberDto);
		}
	}

	public void sendMessage(String name, String message, String referencedMessageId, InputStream inputStream,
			String fileName) {
		discordBot.sendMessage(name, message, referencedMessageId, inputStream, fileName);
	}
}
