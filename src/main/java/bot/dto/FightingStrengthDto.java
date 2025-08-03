package bot.dto;

import lombok.Data;

@Data
public class FightingStrengthDto {
	private Long id;
	private Color color;
	private Integer level;
	private Integer point;
	private Long memberId;
	private String updateDate;
	private AllianceMemberDto allianceMemberDto;
}
