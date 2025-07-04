package bot.model.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import bot.model.discord.DiscordBotModel;
import bot.util.discord.DiscordBot;
import bot.util.github.Git;

@Component
public class StartupRunner implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

	@Autowired
	private DiscordBotModel discordBotModel;
	@Autowired
	private DiscordBot discordBot;
	@Autowired
	private Git git;

	@Override
	public void run(String... args) throws Exception {
		discordBot.init(discordBotModel);
		git.init();
		log.info("初期起動完了");
	}

}
