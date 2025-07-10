package bot.model.discord;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import bot.dto.MemberDto;
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

public class DiscordModel extends ListenerAdapter {
	private DiscordBot discordBot;

	public void setDiscordModel(DiscordBot discordBot) {
		this.discordBot = discordBot;
	}

	public void onMessageReceivedModel(MessageReceivedEvent event) {
		Member member = event.getMember();
		Message discoMessage = event.getMessage();
		List<Attachment> attachmentList = discoMessage.getAttachments();
		List<String> urlList = new ArrayList<String>();
		if (attachmentList != null && !attachmentList.isEmpty()) {
			attachmentList.forEach((s) -> {
				urlList.add(s.getUrl());
			});
		}
		MemberDto memberDto = discordBot.getMemberDto(member.getId());
		memberDto.setBot(event.getAuthor().isBot());
		String message = event.getMessage().getContentRaw();
		Message referencedMessage = discoMessage.getReferencedMessage();
		if (referencedMessage != null) {
		onMessageReceived(memberDto, discoMessage.getId(), message, referencedMessage.getId(),
				urlList);
		} else {
			onMessageReceived(memberDto, discoMessage.getId(), message, null,
					urlList);
		}
	}

	public void onMessageReceived(MemberDto memberDto, String messageId, String message, String referencedMessageId,
			List<String> attachmentUrlList) {
	}

	public void onMessageUpdateModel(MessageUpdateEvent event) {
		Member member = event.getMember();
		Message discoMessage = event.getMessage();
		List<Attachment> attachmentList = discoMessage.getAttachments();
		List<String> urlList = new ArrayList<String>();
		if (attachmentList != null && !attachmentList.isEmpty()) {
			attachmentList.forEach((s) -> {
				urlList.add(s.getUrl());
			});
		}
		MemberDto memberDto = discordBot.getMemberDto(member.getId());
		memberDto.setBot(event.getAuthor().isBot());
		String message = event.getMessage().getContentRaw();
		onMessageUpdate(memberDto, discoMessage.getId(), message, discoMessage.getReferencedMessage().getId(), urlList);

	}

	public void onMessageUpdate(MemberDto memberDto, String messageId, String message, String referencedMessageId,
			List<String> attachmentUrlList) {
	}

	public void onMessageDeleteModel(MessageDeleteEvent event) {
		onMessageDelete(event.getMessageId());
	}

	public void onMessageDelete(String messageId) {
	}

	public void onGuildMemberJoinModel(GuildMemberJoinEvent event) {
		Member member = event.getMember();
		MemberDto memberDto = discordBot.getMemberDto(member.getId());
		memberDto.setBot(false);
		onGuildMemberJoin(memberDto);
	}

	public void onGuildMemberJoin(MemberDto memberDto) {
	}

	public void onGuildMemberRemoveModel(GuildMemberRemoveEvent event) {
		Member member = event.getMember();
		MemberDto memberDto = discordBot.getMemberDto(member.getId());
		memberDto.setBot(false);
		onGuildMemberRemove(memberDto);
	}

	public void onGuildMemberRemove(MemberDto memberDto) {
	}
	
	public void sendMessage(String name, String message, String referencedMessageId, InputStream inputStream, String fileName) {
		discordBot.sendMessage(name, message, referencedMessageId, inputStream, fileName);
	}
}
