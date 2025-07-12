package bot.util.discord;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bot.DiscordBotTestApplication;
import bot.dto.AllianceMemberDto;
import bot.entity.AllianceMember;
import bot.repository.AllianceMemberRepository;
import bot.util.prop.AppriCationProperties;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
	private List<AllianceMemberDto> allianceMemberDtoList = new ArrayList<>();
	@Autowired
	private AppriCationProperties appriCationProperties;
	@Autowired
	private AllianceMemberRepository allianceMemberRepository;

	private boolean endFlag = true;

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

		endFlag = true;
		getGuild().loadMembers().onSuccess(members -> {
			if (members.isEmpty()) {
				log.error("このギルドにはメンバーがいません（または取得できませんでした）。");
				endFlag = false;
				return;
			}

			try {
				for (Member member : members) {
					AllianceMemberDto allianceMemberDto = new AllianceMemberDto();
					String nickname = member.getNickname();
					String effectiveName = member.getEffectiveName();
					if (nickname == null) {
						nickname = effectiveName;
					}
					AllianceMember allianceMember = allianceMemberRepository.findByDiscordMemberId(member.getId());
					if (allianceMember == null) {
						allianceMemberDto.setAyarabuId("");
						allianceMemberDto.setAyarabuName(nickname);
						allianceMemberDto.setBot(false);
						allianceMemberDto.setCreateDate(DiscordBotTestApplication.sdf.format(new Date()));
						allianceMemberDto.setStatementCount(0);
						allianceMemberDto.setAllianceName("無所属");
						allianceMemberDto.setDiscordMemberId(member.getId());
						allianceMemberDto.setDiscordName(nickname);

						ModelMapper modelMapper = new ModelMapper();
						allianceMember = modelMapper.map(allianceMemberDto, AllianceMember.class);
						allianceMember = allianceMemberRepository.save(allianceMember);
						allianceMemberDto.setId(allianceMember.getId());
					} else {
						ModelMapper modelMapper = new ModelMapper();
						allianceMember.setDiscordName(nickname);
						allianceMemberDto = modelMapper.map(allianceMember, AllianceMemberDto.class);
					}

					allianceMemberDtoList.add(allianceMemberDto);
					log.info("メンバー:" + nickname);
				}
			} catch (Exception e) {
				log.error("メンバー取得でエラー",e);
				throw e;
			}
			endFlag = false;

		}).onError(throwable -> {
			log.error("メンバー取得でエラー", throwable);
			endFlag = false;
		});
		while (endFlag) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
			}
		}
		log.info("Discordメンバー取得完了");
	}

	public String getMention(String discordUserId) {
		User user = jda.retrieveUserById(discordUserId).complete();
		String mention = null;
		if (user != null) {
			mention = user.getAsMention();
		}
		return mention;
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

	public void sendMessage(String name, String message, String referencedMessageId, InputStream inputStream,
			String fileName) {
		if (name.trim().isEmpty()) {
			sendMessage(message, referencedMessageId, inputStream, fileName);
		} else {
			sendMessage(name.trim() + "さんの発言：\n" + message, referencedMessageId, inputStream, fileName);
		}
	}

	public void sendMessage(String message, String referencedMessageId, InputStream inputStream, String fileName) {
		if (message.isEmpty())
			return;
		String replaceMessage = "";
		for (AllianceMemberDto allianceMemberDto : allianceMemberDtoList) {
			String mentionName = "@" + allianceMemberDto.getDiscordName();
			Pattern pattern = Pattern.compile("(" + mentionName + ")");
			String[] arr = pattern.split(message);
			if (arr.length == 1)
				continue;
			replaceMessage = "";
			for (int i = 0; i < arr.length; i++) {
				String str = arr[i];
				if (message.startsWith(mentionName)) {
					replaceMessage += getMention(allianceMemberDto.getDiscordMemberId()) + str;
				} else if (i < arr.length - 1) {
					replaceMessage += str + getMention(allianceMemberDto.getDiscordMemberId());
				} else {
					replaceMessage += str;
				}
			}
			message = replaceMessage;
		}
		if (replaceMessage.isEmpty())
			replaceMessage = message;

		MessageCreateAction messageCreateAction = getWebTextChannel().sendMessage(replaceMessage);
		if (referencedMessageId != null && !referencedMessageId.isEmpty())
			messageCreateAction.setMessageReference(referencedMessageId.trim());
		if (inputStream != null)
			messageCreateAction.setFiles(FileUpload.fromData(inputStream, fileName));
		messageCreateAction.queue();
	}

	public List<AllianceMemberDto> getAllianceMemberDtoList() {
		return allianceMemberDtoList;
	}

	public AllianceMemberDto getAllianceMemberDto(String discordMemberId) {
		for (AllianceMemberDto allianceMemberDto : allianceMemberDtoList) {
			if (allianceMemberDto.getDiscordMemberId().equals(discordMemberId))
				return allianceMemberDto;
		}
		return null;
	}
}
