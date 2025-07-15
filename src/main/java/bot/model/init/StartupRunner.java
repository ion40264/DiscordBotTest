package bot.model.init;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import bot.model.discord.DiscordModel;
import bot.repository.ChatAttachmentRepository;
import bot.repository.ChatMessageRepository;
import bot.service.ChatService;
import bot.service.MemberService;
import bot.util.discord.DiscordBot;
import bot.util.github.Git;

@Component
public class StartupRunner implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);
	@Autowired
	private ChatService chatService;
	@Autowired
	private DiscordBot discordBot;
	@Autowired
	public DiscordModel discordModel;
	@Autowired
	private Git git;
	@Autowired
	private MemberService memberService;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	@Autowired
	private ChatAttachmentRepository chatAttachmentRepository;

	@Override
	public void run(String... args) throws Exception {
		try {
			discordModel.adddIscordEventListener(chatService);
			discordModel.adddIscordEventListener(memberService);
			discordBot.init(discordModel);
			discordModel.initDiscordMember();
			chatService.init();
			discordModel.getHistory(100);

			git.init();
		} catch (Exception e) {
			log.error("初期起動に失敗しました。終了します。", e);
			System.exit(-1);
		}
		log.info("初期起動完了");
	}
	@PreDestroy
    public void cleanup() {
		discordModel.removeIscordEventListener(chatService);
		discordBot.shutDown();
		log.info("終了完了");
    }
}
