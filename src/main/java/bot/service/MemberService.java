package bot.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bot.dto.AllianceMemberDto;
import bot.dto.ChatMessageDto;
import bot.model.MemberModel;
import bot.model.discord.DIscordEventListener;

@Service
public class MemberService implements DIscordEventListener {
	@Autowired
	private ChatService chatService;
	@Autowired
	private MemberModel memberModel;

	public void addAllianceMemberDto(AllianceMemberDto AllianceMemberDto) {
		memberModel.addOrChangeAllianceMemberDto(AllianceMemberDto);
	}

	@Transactional
	public void updateAllianceMemberDto(AllianceMemberDto AllianceMemberDto) {
		memberModel.addOrChangeAllianceMemberDto(AllianceMemberDto);
	}

	public List<AllianceMemberDto> getAllianceMemberDtoList() {
		return memberModel.getAllianceMemberDtoList();
	}

	public void removeAllianceMemberDto(long id) {
		memberModel.removeAllianceMemberDto(id);
	}

	@Override
	public void onGuildMemberJoin(AllianceMemberDto allianceMemberDto) {
		if (allianceMemberDto == null)
			return;
		memberModel.addOrChangeAllianceMemberDto(allianceMemberDto);
	}

	@Override
	public void onGuildMemberRemove(AllianceMemberDto allianceMemberDto) {
		// TODO discord抜けたら脱退扱いでいいか？
		memberModel.removeAllianceMemberDtoByDiscordId(allianceMemberDto);
	}

	@Override
	public void onMessageReceived(ChatMessageDto chatMessageDto) {
	}

	@Override
	public void onMessageUpdate(ChatMessageDto chatMessageDto) {
	}

	@Override
	public void onMessageDelete(String messageId) {
	}

}
