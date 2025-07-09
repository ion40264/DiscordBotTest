package bot.util.prop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class AppriCationProperties {
	@Value("${discord.guild-id}")
	private String guildId;
	@Value("${discord.channel-id}")
	private String channelId;
	@Value("${discord.web-channel-id}")
	private String webChannelId;
	@Value("${github.repository-name}")
	private String githubRepositoryName;
}
