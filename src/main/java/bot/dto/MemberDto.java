package bot.dto;

import lombok.Data;
import net.dv8tion.jda.api.entities.Member;

@Data
public class MemberDto {
	private Long id;
	private String discordMemberId;
	// Discord APIのDiscordメンバー
	private Member memeber;
	// Discordのメンバーのニックネーム
	private String discordName;
	private String ayarabuName;
	private int count;
	private boolean isBot;
}
