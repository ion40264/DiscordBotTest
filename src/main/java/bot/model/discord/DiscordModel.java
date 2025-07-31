package bot.model.discord;

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
import bot.dto.ChatAttachmentDto;
import bot.dto.ChatMessageDto;
import bot.entity.Channel;
import bot.entity.ChatAttachment;
import bot.entity.ChatMessage;
import bot.model.MemberModel;
import bot.repository.ChannelRepository;
import bot.repository.ChatAttachmentRepository;
import bot.repository.ChatMessageRepository;
import bot.util.discord.DiscordBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
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
	private MemberModel memberModel;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	@Autowired
	private ChatAttachmentRepository chatAttachmentRepository;
	@Autowired
	private ChannelRepository channelRepository;
	private List<DIscordEventListener> dIscordEventListenerList = new ArrayList<DIscordEventListener>();

	private boolean endFlag = false;
	private List<Message> messageList;

	public void initDiscordMember() {
		endFlag = true;
		discordBot.getGuild().loadMembers().onSuccess(members -> {
			if (members.isEmpty()) {
				log.error("このギルドにはメンバーがいません（または取得できませんでした）。");
				endFlag = false;
				return;
			}

			try {
				for (Member member : members) {
					memberModel.init(getName(member), member.getId(), member.getUser().isBot());
				}
			} catch (Exception e) {
				log.error("メンバー取得でエラー", e);
				throw e;
			}
			endFlag = false;

		}).onError(throwable -> {
			log.error("メンバー取得でエラー", throwable);
			endFlag = false;
		});
		// TODO すっげー嫌な書き方
		while (endFlag) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				endFlag = false;
			}
		}
		endFlag = false;
		log.info("Discordメンバー取得完了");
	}

	public void getHistory(int limit) {
		List<bot.entity.Channel> channelList = channelRepository.findAll();
		channelList.forEach(channel->{
			endFlag = true;
			String messageId;
			ChatMessageDto chatMessageDto = new ChatMessageDto();
			chatMessageDto.setChannelId(channel.getChannelId());
			chatMessageDto.setChannelName(channel.getChannelName());
			chatMessageDto.setName("ボット");
			chatMessageDto.setMessage("ボット起動");
			sendMessage(chatMessageDto);
			// TODO 気に食わない
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
			}
			Optional<ChatMessage> optional = chatMessageRepository.findById(1L);
			if (!optional.isEmpty()) {
				messageId = optional.get().getDiscordMessageId();
				discordBot.getChannel(channel.getChannelId()).getHistoryBefore(messageId, limit).queue(
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
			}

			// 取得したメッセージのリストは、古い順に並んでいるのでソート
			List<Message> sortMessageList = new ArrayList<>();
			sortMessageList.addAll(messageList);
			sortMessageList.sort(Comparator.comparing(Message::getIdLong));
			for (Message message : sortMessageList) {
				if (chatMessageRepository.findByDiscordMessageId(message.getId()) != null)
					continue;
				Channel channel2 = channelRepository.findByChannelId(message.getChannelId());
				ChatMessage chatMessage = new ChatMessage();
				// TODO 日付の文字列型変換は共通に抜き出したい
				chatMessage
						.setCreateDate(message.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
				chatMessage.setDiscordMessageId(message.getId());
				chatMessage.setMessage(message.getContentDisplay().replace("\n", "<br>"));
				chatMessage.setName(getName(message.getMember()));
				chatMessage.setChannelId(channel2.getChannelId());
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
			
		});
	}

	public void adddIscordEventListener(DIscordEventListener dIscordEventListener) {
		dIscordEventListenerList.add(dIscordEventListener);
	}

	public void removeIscordEventListener(DIscordEventListener dIscordEventListener) {
		dIscordEventListenerList.remove(dIscordEventListener);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		try {
			ChatMessageDto chatMessageDto = createChatMessageDto(event, event.getMember(), event.getMessage()); 
			for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
				dIscordEventListener
						.onMessageReceived(chatMessageDto								);

			}
		} catch (Exception e) {
			log.error("error.", e);
			throw e;
		}
	}

	private ChatMessageDto createChatMessageDto(GenericMessageEvent event, Member member, Message discoMessage) {
		List<Attachment> attachmentList = discoMessage.getAttachments();

		List<ChatAttachmentDto> attachmentDtoList = new ArrayList<ChatAttachmentDto>();
		if (attachmentList != null && !attachmentList.isEmpty()) {
			attachmentList.forEach((attachment) -> {
				ChatAttachmentDto chatAttachmentDto = new ChatAttachmentDto();
				chatAttachmentDto.setAttachmentFileInputStream(null);
				chatAttachmentDto.setAttachmentFileName(attachment.getFileName());
				chatAttachmentDto.setAttachmentUrl(attachment.getUrl());
				attachmentDtoList.add(chatAttachmentDto);
			});
		}
		AllianceMemberDto allianceMemberDto = memberModel.getAllianceMemberDto(member.getId());
		if (allianceMemberDto == null) {
			allianceMemberDto = new AllianceMemberDto();
			log.warn("メンバーにいない人からメッセージ message=" + discoMessage);
		}
		String message = discoMessage.getContentDisplay();
		Message referencedMessage = discoMessage.getReferencedMessage();
		String referencedMessageId = null;
		if (referencedMessage != null)
			referencedMessageId = referencedMessage.getId();
		MessageChannelUnion messageChannelUnion = event.getChannel();

		ChatMessageDto chatMessageDto = new ChatMessageDto();
		chatMessageDto.setChatAttachmentDtoList(attachmentDtoList);
		chatMessageDto.setCreateDate(
				discoMessage.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
		chatMessageDto.setDiscordMessageId(discoMessage.getId());
		chatMessageDto.setMessage(message);
		chatMessageDto.setName(getName(member));
		chatMessageDto.setQuoteDiscordId(referencedMessageId);
		chatMessageDto.setChannelId(messageChannelUnion.getId());
		chatMessageDto.setChannelName(messageChannelUnion.getName());

		ChatMessage chatMessage = chatMessageRepository.findByDiscordMessageId(referencedMessageId);
		if (chatMessage != null)
			chatMessageDto.setQuoteId(chatMessage.getId().toString());

		return chatMessageDto;
	}

	private String getName(Member member) {
		String nickname = member.getNickname();
		String effectiveName = member.getEffectiveName();
		if (nickname == null) {
			nickname = effectiveName;
		}
		return nickname;
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event) {
		try {
			ChatMessageDto chatMessageDto = createChatMessageDto(event, event.getMember(), event.getMessage()); 
			for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
				dIscordEventListener.onMessageUpdate(chatMessageDto);

			}
		} catch (Exception e) {
			log.error("error.", e);
			throw e;
		}
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		try {
			for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
				dIscordEventListener
						.onMessageDelete(event.getMessageId());
			}
		} catch (Exception e) {
			log.error("error.", e);
			throw e;
		}
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		try {
			Member member = event.getMember();
			AllianceMemberDto allianceMemberDto = memberModel.getAllianceMemberDto(member.getId());
			for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
				dIscordEventListener
						.onGuildMemberJoin(allianceMemberDto);
			}
		} catch (Exception e) {
			log.error("error.", e);
			throw e;
		}
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		try {
			Member member = event.getMember();
			AllianceMemberDto allianceMemberDto = memberModel.getAllianceMemberDto(member.getId());
			allianceMemberDto.setBot(false);
			for (DIscordEventListener dIscordEventListener : dIscordEventListenerList) {
				dIscordEventListener
						.onGuildMemberRemove(allianceMemberDto);
			}
		} catch (Exception e) {
			log.error("error.", e);
			throw e;
		}
	}

	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		String customId = event.getComponentId();
		if (customId.equals("selectId1")) {
			String selectedValue = event.getValues().get(0);
			event.reply(selectedValue+"が選択されました。").setEphemeral(true).queue();
		}
		event.deferReply(true).queue();
	}
	public void sendMessage(ChatMessageDto chatMessageDto) {
		discordBot.sendMessage(chatMessageDto, memberModel.getAllianceMemberDtoList());
	}
}
