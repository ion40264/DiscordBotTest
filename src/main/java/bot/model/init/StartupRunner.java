package bot.model.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import bot.util.discord.DiscordBot;
import bot.util.prop.AppriCationProperties;

@Component
public class StartupRunner implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);
	@Autowired
	AppriCationProperties appriCationProperties;

	@Autowired
	private DiscordBot discordBot;

	@Override
	public void run(String... args) throws Exception {
//		memberDtoList = discordBot.getMemberDtoList();
		log.info("初期起動完了");
	}

}
