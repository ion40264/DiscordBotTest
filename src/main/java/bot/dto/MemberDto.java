package bot.dto;

import lombok.Data;
import net.dv8tion.jda.api.entities.Member;

@Data
public class MemberDto {
	private Long id;
	// Discord APIのDiscordメンバー
	private Member memeber;
	// Discordのメンバーのニックネーム
	private String nickname;
	private int count;
}
