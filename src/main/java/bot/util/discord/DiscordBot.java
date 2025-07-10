package bot.util.discord;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bot.dto.MemberDto;
import bot.entity.AllianceMember;
import bot.repository.AllianceMemberRepository;
import bot.util.prop.AppriCationProperties;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

@Component
public class DiscordBot {
	private static final Logger log = LoggerFactory.getLogger(DiscordBot.class);
	private JDA jda;
	private TextChannel textChannel;
	private TextChannel webTextChannel;
	private Guild guild;
	private List<MemberDto> memberDtoList = new ArrayList<>();
	@Autowired
	private AppriCationProperties appriCationProperties;
	@Autowired
	private AllianceMemberRepository discoMemberRepository;

	public void init(ListenerAdapter listenerAdapter) {
		jda = JDABuilder.createDefault(System.getenv("DISCORD_BOT_TOKEN"))
				.setRawEventsEnabled(true)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.enableIntents(GatewayIntent.MESSAGE_CONTENT)
				.enableIntents(GatewayIntent.GUILD_MESSAGES)
				.enableIntents(GatewayIntent.GUILD_MEMBERS)
				.setActivity(Activity.playing("ステータス"))
				.build();
		jda.addEventListener(listenerAdapter);

		jda.updateCommands().queue();
		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			log.error("DiscordBot初期化エラー.", e);
		}

		guild = jda.getGuildById(appriCationProperties.getGuildId());
		textChannel = guild.getTextChannelById(appriCationProperties.getChannelId());
		webTextChannel = guild.getTextChannelById(appriCationProperties.getWebChannelId());

		getGuild().loadMembers().onSuccess(members -> {
			if (members.isEmpty()) {
				log.error("このギルドにはメンバーがいません（または取得できませんでした）。");
				return;
			}

			for (Member member : members) {
				MemberDto memberDto = new MemberDto();
				memberDto.setMemeber(member);
				String nickname = member.getNickname();
				String effectiveName = member.getEffectiveName();
				if (nickname == null) {
					nickname = effectiveName;
				} else {
				}
				AllianceMember allianceMember = discoMemberRepository.findByDiscordName(nickname);
				if (allianceMember == null) {
					allianceMember = new AllianceMember();
					allianceMember.setDiscordName(nickname);
					allianceMember.setStatementCount(0);
					discoMemberRepository.save(allianceMember);
				}
				memberDto.setId(allianceMember.getId());
				memberDto.setCount(allianceMember.getStatementCount());
				memberDto.setDiscordName(nickname);

				memberDtoList.add(memberDto);
				log.info("メンバー:" + nickname);
			}

		}).onError(throwable -> {
			if (throwable instanceof InsufficientPermissionException) {
			} else {
			}
		});
		try {
			// 同期がわからんので暫定
			Thread.sleep(500L);
		} catch (InterruptedException e) {
		}
		log.info("Discordメンバー取得完了");
	}

	public Guild getGuild() {
		return guild;
	}

	public TextChannel getTextChannel() {
		return textChannel;
	}

	public TextChannel getWebTextChannel() {
		return webTextChannel;
	}

	public void sendMessage(String name, String message, String referencedMessageId, InputStream inputStream, String fileName) {
		sendMessage(name.trim() + "さんの発言：\n" + message, referencedMessageId, inputStream, fileName);
	}
	public void sendMessage(String message, String referencedMessageId, InputStream inputStream, String fileName) {
		MessageCreateAction messageCreateAction = getWebTextChannel().sendMessage(message);
		if (referencedMessageId != null && !referencedMessageId.isEmpty())
			messageCreateAction.setMessageReference(referencedMessageId.trim());
		if (inputStream != null) 
			messageCreateAction.setFiles(FileUpload.fromData(inputStream, fileName));
		messageCreateAction.queue();
	}

	public List<MemberDto> getMemberDtoList() {
		return memberDtoList;
	}

	public MemberDto getMemberDto(String discordMemberId) {
		for (MemberDto memberDto : memberDtoList) {
			if (memberDto.getMemeber().getId().equals(discordMemberId))
				return memberDto;
		}
		return null;
	}
}
