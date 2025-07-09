package bot.model.init;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import bot.service.ChatService;
import bot.service.DiscordBotService;
import bot.util.discord.DiscordBot;
import bot.util.github.Git;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class StartupRunner implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

	@Autowired
	private ChatService chatService;
	@Autowired
	private DiscordBotService discordBotService;
	@Autowired
	private DiscordBot discordBot;
	@Autowired
	private Git git;

	@Override
	public void run(String... args) throws Exception {
		List<ListenerAdapter> listenerAdapterList = new ArrayList<ListenerAdapter>();
		listenerAdapterList.add(discordBotService);
		listenerAdapterList.add(chatService);
		discordBot.init(listenerAdapterList);
		chatService.init();
		git.init();
		log.info("初期起動完了");
	}

}
