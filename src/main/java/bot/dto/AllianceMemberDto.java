package bot.dto;

import lombok.Data;

@Data
public class AllianceMemberDto {
	private Long id;
	private String discordMemberId;
	private String discordName;
	private String ayarabuId;
	private String ayarabuName;
	private String allianceName;
	private Integer statementCount;
	private String createDate;
	private boolean isBot;
}
