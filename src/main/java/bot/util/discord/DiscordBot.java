package bot.util.discord;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bot.dto.MemberDto;
import bot.entity.DiscoMember;
import bot.repository.DiscoMemberRepository;
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
	private DiscoMemberRepository discoMemberRepository;

	public void init(List<ListenerAdapter> listenerAdapterList) {
		jda = JDABuilder.createDefault(System.getenv("DISCORD_BOT_TOKEN"))
				.setRawEventsEnabled(true)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.enableIntents(GatewayIntent.MESSAGE_CONTENT)
				.enableIntents(GatewayIntent.GUILD_MESSAGES)
				.enableIntents(GatewayIntent.GUILD_MEMBERS)
				.setActivity(Activity.playing("ステータス"))
				.build();
		for (ListenerAdapter listenerAdapter : listenerAdapterList) {
			jda.addEventListener(listenerAdapter);
		}

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
				DiscoMember discoMember = discoMemberRepository.findByName(nickname);
				if (discoMember == null) {
					discoMember = new DiscoMember();
					discoMember.setName(nickname);
					discoMember.setMcount(0);
					discoMemberRepository.save(discoMember);
				}
				memberDto.setId(discoMember.getId());
				memberDto.setCount(discoMember.getMcount());
				memberDto.setNickname(nickname);

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
	
	public void sendMessage(String message) {
		textChannel.sendMessage(message).complete();
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
