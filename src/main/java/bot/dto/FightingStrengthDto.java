package bot.dto;

import lombok.Data;

@Data
public class FightingStrengthDto {
	private Integer id;
	private Color color;
	private Integer enemyLevel;
	private Integer point;
	private Integer memberId;
	private String updateDate;
	private AllianceMemberDto allianceMemberDto;
}
