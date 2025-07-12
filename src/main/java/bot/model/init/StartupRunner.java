package bot.model.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import bot.repository.ChatAttachmentRepository;
import bot.repository.ChatMessageRepository;
import bot.service.ChatService;
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
	private ChatAttachmentRepository chatAttachmentRepository;
	@Autowired
	private ChatMessageRepository chatMessageRepository;
	@Autowired
	private Git git;

	@Override
	public void run(String... args) throws Exception {
		try {
			discordBot.init(chatService);
			chatService.init(discordBot, chatMessageRepository, chatAttachmentRepository);
			git.init();
		} catch (Exception e) {
			log.error("初期起動に失敗しました。終了します。",e);
			System.exit(-1);
		}
		log.info("初期起動完了");
	}

}
