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
import org.kohsuke.github.HttpException;
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
			GitHub github = new GitHubBuilder().withOAuthToken(System.getenv("FINE_GRAINED_PERSONAL_ACCESS_TOKENS"))
					.build();
			ghRepository = github.getRepository(appriCationProperties.getGithubRepositoryName());
		} catch (IOException e) {
			throw new RuntimeException("githubの取得に失敗しました。", e);
		}
	}

	/**
	 * Issueを作ってIssueのDTOを返す。
	 * @param title タイトル
	 * @param body 本文
	 * @param targetBranch どのブランチに対するIssueか？デフォルトはmain
	 * @return IssueDto
	 * @throws Exception 
	 */
	public IssueDto createIssue(String title, String body, String targetBranch) throws Exception {
		GHBranch ghBranch;
		try {
			ghBranch = ghRepository.getBranch(targetBranch);
		} catch (IOException e) {
			throw new Exception("targetBranchがありません。" + getBranches(), e);
		}
		try {
			title += " 対象ブランチ:" + ghBranch.getName();
			GHIssueBuilder issue = ghRepository.createIssue(title);
			issue.body(body);
			GHIssue ghIssue = issue.create();
			int number = ghIssue.getNumber();

			String baseBranchSha = ghRepository.getRef("heads/" + targetBranch).getObject().getSha();
			GHRef ghRef = ghRepository.createRef("refs/heads/" + "feature#" + number, baseBranchSha);

			IssueDto issueDto = new IssueDto();
			issueDto.setBody(body);
			issueDto.setBranchName("feature#" + number);
			issueDto.setTitle(title);
			issueDto.setIssueUrl(ghRef.getUrl().toString());

			return issueDto;
		} catch (IOException e) {
			log.error("issueの作成に失敗しました", e);
			throw new Exception("issueの作成に失敗しました", e);
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

	public IssueDto createIssue(String title) throws Exception {
		return createIssue(title, title, "main");
	}

	public IssueDto createIssue(String title, String targetBranch) throws Exception {
		return createIssue(title, title, targetBranch);
	}

	/**
	 * 
	 * @param issueDto IssueDtoでなくてもいいかもしんない
	 * @param headBranch 自分が編集していたブランチ名 feature#1等
	 * @param baseBranch 適用したいブランチ main等
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public String createPullRequest(IssueDto issueDto, String headBranch, String baseBranch, String body)
			throws Exception {
		if (!existBranch(headBranch)) {
			throw new Exception("ブランチが存在しません headBranch=" + headBranch);
		}
		if (!existBranch(headBranch)) {
			throw new Exception("ブランチが存在しません baseBranch=" + baseBranch);
		}
		GHPullRequest pullRequest;
		try {
			pullRequest = ghRepository.createPullRequest("Re:" + issueDto.getTitle(), headBranch, baseBranch, body);
		} catch (IOException e) {
			log.error("プルリクエストの作成に失敗しました。", e);
			if (e instanceof HttpException) {
				throw new Exception("プルリクエストの作成に失敗しました。" + ((HttpException) e).getMessage(), e);
			}
			throw new Exception("プルリクエストの作成に失敗しました。", e);
		}
		return pullRequest.getHtmlUrl().toString();
	}

	public String createPullRequest(IssueDto issueDto, String headBranch, String baseBranch) throws Exception {
		return createPullRequest(issueDto, headBranch, baseBranch, "「"+issueDto.getTitle() + "」のレビュー、マージをお願いします。");
	}

	public String createPullRequest(IssueDto issueDto, String headBranch) throws Exception {
		return createPullRequest(issueDto, headBranch, "main");
	}

	private boolean existBranch(String branchName) {
		try {
			ghRepository.getBranch(branchName);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
