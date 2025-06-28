package bot.model.discord;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bot.dto.MemberDto;
import bot.entity.DiscoMember;
import bot.repository.DiscoMemberRepository;
import bot.util.discord.DiscordBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class DiscordBotModel extends ListenerAdapter {
	private static final Logger log = LoggerFactory.getLogger(DiscordBotModel.class);

	@Autowired
	private DiscoMemberRepository discoMemberRepository;
	@Autowired
	private DiscordBot discordBot;



	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		super.onGuildMemberJoin(event);

	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return;

		Member member = event.getMember();
		String nickname = member.getNickname();
		Message discoMessage = event.getMessage();
		List<Attachment> attachments = discoMessage.getAttachments();
		if (attachments != null && !attachments.isEmpty()) {
			Attachment attachment = attachments.getFirst();
			String url = attachment.getUrl();
			log.info(url);
		}
		log.info("messageid="+ event.getMessage().getId());
		String message = event.getMessage().getContentRaw();
		String effectiveName = member.getEffectiveName();
		if (nickname == null) {
			nickname = effectiveName;
		}
		log.info("メッセージ:" + message);

		if (message.contains("botさん")) {
			for (MemberDto memberDto : discordBot.getMemberDtoList()) {
				if (nickname.contains(memberDto.getNickname())) {
					if (message.contains("質問")) {
						//gemini
					}
					if (memberDto.getCount() == 0) {
						event.getChannel().sendMessage(nickname + "さん、よろしくお願いします").queue();
						memberDto.setCount(memberDto.getCount() + 1);
					} else if (memberDto.getCount() >= 3) {
						event.getChannel().sendMessage(nickname + "さん、しつこいですよ。").queue();
						memberDto.setCount(memberDto.getCount() + 1);
					} else if (memberDto.getCount() >= 0) {
						event.getChannel().sendMessage(nickname + "さん、まだ生まれたばっかなので複雑な会話はできません。").queue();
						memberDto.setCount(memberDto.getCount() + 1);
					}
				}
				DiscoMember discoMember;
				Optional<DiscoMember> optional = discoMemberRepository.findById(memberDto.getId());
				discoMember = optional.get();
				discoMember.setMcount(memberDto.getCount());
				discoMemberRepository.save(discoMember);
			}
		}
	}

	//コマンドの反応メソッド
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

	}
}
