package bot.util.github;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GitTest {
	private static final Logger log = LoggerFactory.getLogger(GitTest.class);
	@Autowired
	private Git git;

	@Test
	void testCreateIssue() throws Exception {
		log.info(git.createIssue("MemberDtoのメンバー情報が分かりずらい。コメントをいれてほしい"));
		log.info(git.getBranches());
	}

	@Test
	void testCreatePullRequest() {
	}

}
