package bot.util.github;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bot.util.prop.AppriCationProperties;

@Component
public class Git {
	private static final Logger log = LoggerFactory.getLogger(Git.class);
	@Autowired
	private AppriCationProperties appriCationProperties;
	private GHRepository ghRepository;

	public void init() {
		try {
			// 環境変数FINE-GRAINED PERSONAL ACCESS TOKENSにFine-grained personal access tokensが必要
			GitHub github = new GitHubBuilder().withOAuthToken(System.getenv("FINE-GRAINED PERSONAL ACCESS TOKENS"))
					.build();
			ghRepository = github.getRepository(appriCationProperties.getGithubRepositoryName());
		} catch (IOException e) {
			throw new RuntimeException("githubの取得に失敗しました。", e);
		}
	}

	/**
	 * Issueを作ってIssueのURLを返す。
	 * @param title タイトル
	 * @param body 本文
	 * @param targetBranch どのブランチに対するIssueか？デフォルトはmain
	 * @return url もしくはエラー文字
	 * @throws IOException 
	 */
	public String createIssue(String title, String body, String targetBranch) throws IOException {
		GHBranch ghBranch;
		try {
			ghBranch = ghRepository.getBranch(targetBranch);
		} catch (IOException e) {
			return "targetBranchがありません。" + getBranches();
		}
		try {
			title += " 対象ブランチ:" + ghBranch.getName();
			GHIssueBuilder issue = ghRepository.createIssue(title);
			issue.body(body);
			GHIssue ghIssue = issue.create();
			int number = ghIssue.getNumber();

			String baseBranchSha = ghRepository.getRef("heads/" + targetBranch).getObject().getSha();
			GHRef newBranchRef = ghRepository.createRef("refs/heads/" + "feature#" + number, baseBranchSha);

			Map<String, GHBranch> branches = ghRepository.getBranches();
			String branchNames = "存在するブランチ名:";
			for (Entry<String, GHBranch> entry : branches.entrySet()) {
				branchNames += entry.getKey() + ", ";
			}
			log.info("number=" + number);

			
			return ghIssue.getTitle() + "#" + number;
		} catch (IOException e) {
			log.error("issueの作成に失敗しました", e);
			return "issueの作成に失敗しました";
		}
	}
	
	public String getBranches() throws IOException {
		Map<String, GHBranch> branches = ghRepository.getBranches();
		String branchNames = "存在するブランチ名:";
		for (Entry<String, GHBranch> entry : branches.entrySet()) {
			branchNames += entry.getKey() + ", ";
		}
		return branchNames;
	}

	public String createIssue(String title) throws IOException {
		return createIssue(title, title, "main");
	}

	public String createIssue(String title, String targetBranch) throws IOException {
		return createIssue(title, title, targetBranch);
	}

	public GHPullRequest createPullRequest(String headBranch, String baseBranch, String title, String body)
			throws Exception {
		// head: 変更が含まれるブランチ (例: feature-branch)
		// base: 変更をマージしたいブランチ (例: main, develop)
		GHPullRequest pullRequest = ghRepository.createPullRequest(title, headBranch, baseBranch, body);
		System.out.println("プルリクエストが作成されました: " + pullRequest.getHtmlUrl());
		return pullRequest;
	}
}
