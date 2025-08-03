package bot.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bot.dto.AllianceMemberDto;
import bot.dto.ChatMessageDto;
import bot.entity.AllianceMember;
import bot.model.MemberModel;
import bot.model.discord.DIscordEventListener;
import bot.repository.AllianceMemberRepository;

@Service
public class MemberService implements DIscordEventListener {
	@Autowired
	private MemberModel memberModel;
	@Autowired
	private AllianceMemberRepository allianceMemberRepository;
	private ModelMapper modelMapper;

	public MemberService() {
		modelMapper = new ModelMapper();
	}

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
	
	public AllianceMemberDto getAllianceMemberDtoByAyarabuName(String ayarabuName) {
		AllianceMember allianceMember = allianceMemberRepository.findByAyarabuName(ayarabuName);
		return modelMapper.map(allianceMember, AllianceMemberDto.class);
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
