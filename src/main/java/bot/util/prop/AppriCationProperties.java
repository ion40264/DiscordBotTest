package bot.util.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "discord")
public class AppriCationProperties {
	private String guildId;
	private String channelId;
}
