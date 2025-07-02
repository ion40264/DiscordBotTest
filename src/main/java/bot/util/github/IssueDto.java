package bot.util.github;

import lombok.Data;

@Data
public class IssueDto {
	private String title;
	private String body;
	private String branchName;
	private String issueUrl;
}
