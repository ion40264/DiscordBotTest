package bot.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import bot.DiscordBotTestApplication;
import bot.dto.AllianceMemberDto;
import bot.dto.MemberAlliance;
import bot.dto.MemberRole;
import bot.entity.AllianceMember;
import bot.repository.AllianceMemberRepository;

@Component
public class MemberModel {
	private static final Logger log = LoggerFactory.getLogger(MemberModel.class);
	private List<AllianceMemberDto> allianceMemberDtoList = new ArrayList<>();
	@Autowired
	private AllianceMemberRepository allianceMemberRepository;

	@Transactional
	public void removeAllianceMemberDto(long id) {
		AllianceMemberDto removeDto = getAllianceMemberDto(id);
		allianceMemberDtoList.remove(removeDto);
		allianceMemberRepository.deleteById(id);
		log.info("メンバー削除:" + removeDto);
	}

	public void init(String name, String discordId, boolean isBot) {
		AllianceMemberDto allianceMemberDto = new AllianceMemberDto();
		AllianceMember allianceMember = allianceMemberRepository.findByDiscordMemberId(discordId);
		if (allianceMember == null) {
			allianceMemberDto = getDefault(name, discordId, isBot);
			addOrChangeAllianceMemberDto(allianceMemberDto);
			log.info("メンバー追加:" + allianceMemberDto);
		} else {
			allianceMemberDto = toDtoFromEntity(allianceMember);
			addOrChangeAllianceMemberDto(allianceMemberDto);
			log.info("メンバー:" + allianceMemberDto);
		}
	}
	private AllianceMemberDto getDefault(String name, String discordId, boolean isBot) {
		AllianceMemberDto allianceMemberDto = new AllianceMemberDto();
		allianceMemberDto.setMemberRole(MemberRole.MEMBER);
		allianceMemberDto.setAyarabuId("");
		allianceMemberDto.setAyarabuName(name);
		allianceMemberDto.setBot(false);
		allianceMemberDto.setCreateDate(DiscordBotTestApplication.sdf.format(new Date()));
		allianceMemberDto.setStatementCount(0);
		allianceMemberDto.setAlliance(MemberAlliance.NONE);
		allianceMemberDto.setDiscordMemberId(discordId);
		allianceMemberDto.setDiscordName(name);
		allianceMemberDto.setBot(isBot);
		return allianceMemberDto;
	}

	public List<AllianceMemberDto> getAllianceMemberDtoList() {
		return allianceMemberDtoList;
	}

	private AllianceMember toEntityFromDto(AllianceMemberDto allianceMemberDto) {
		ModelMapper modelMapper = new ModelMapper();
		//		modelMapper.addConverter(new StringToMemberRoleConverter());
		return modelMapper.map(allianceMemberDto, AllianceMember.class);
	}

	private AllianceMemberDto toDtoFromEntity(AllianceMember allianceMember) {
		ModelMapper modelMapper = new ModelMapper();
		return modelMapper.map(allianceMember, AllianceMemberDto.class);
	}

	public void addOrChangeAllianceMemberDto(AllianceMemberDto allianceMemberDto) {
		// TODO 起動時DBにいてdiscoにいない場合は消す？

		AllianceMember allianceMember;
		allianceMember = toEntityFromDto(allianceMemberDto);
		allianceMember = allianceMemberRepository.save(allianceMember);
		// TODO 性能はま、いっか。マスタだし数十人だろうし
		updateAllianceMemberDtoList();
		log.info("メンバー追加or更新:" + allianceMemberDto);
	}

	private void updateAllianceMemberDtoList() {
		allianceMemberDtoList = new ArrayList<AllianceMemberDto>();
		allianceMemberRepository.findAll().forEach(member -> {
			allianceMemberDtoList.add(toDtoFromEntity(member));
		});
	}
	public void removeAllianceMemberDtoByDiscordId(AllianceMemberDto allianceMemberDto) {
		AllianceMemberDto removeDto = getAllianceMemberDto(allianceMemberDto.getDiscordMemberId());
		allianceMemberDtoList.remove(removeDto);
		AllianceMember allianceMember = allianceMemberRepository.findByDiscordMemberId(
				allianceMemberDto.getDiscordMemberId());

		allianceMemberRepository.deleteById(allianceMember.getId());
		log.info("メンバー削除:" + allianceMemberDto);
	}

	public AllianceMemberDto getAllianceMemberDto(String discordMemberId) {
		for (AllianceMemberDto allianceMemberDto : allianceMemberDtoList) {
			if (allianceMemberDto.getDiscordMemberId().equals(discordMemberId))
				return allianceMemberDto;
		}
		return null;
	}

	public AllianceMemberDto getAllianceMemberDto(long id) {
		for (AllianceMemberDto allianceMemberDto : allianceMemberDtoList) {
			if (allianceMemberDto.getId() == id)
				return allianceMemberDto;
		}
		return null;
	}

}
