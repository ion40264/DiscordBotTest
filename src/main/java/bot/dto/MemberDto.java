package bot.dto;

import lombok.Data;
import net.dv8tion.jda.api.entities.Member;

@Data
public class MemberDto {
	private Long id;
	private Member memeber;
	private String nickname;
	private int count;
}
