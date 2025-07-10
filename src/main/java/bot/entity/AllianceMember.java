package bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;

@Data
@Entity
public class AllianceMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String discordMemberId;
	private String discordName;
	private String ayarabuId;
	private String ayarabuName;
	private Integer statementCount;
	private String createDate;
}
