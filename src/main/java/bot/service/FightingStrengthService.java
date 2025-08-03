package bot.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bot.DiscordBotTestApplication;
import bot.dto.AllianceMemberDto;
import bot.dto.Color;
import bot.dto.FightingStrengthDto;
import bot.dto.MemberAlliance;
import bot.entity.AllianceMember;
import bot.entity.FightingStrength;
import bot.entity.LevelMaster;
import bot.repository.AllianceMemberRepository;
import bot.repository.FightingStrengthRepository;
import bot.repository.LevelMasterRepository;
import jp.highwide.common.excel.ExcelUtil;

@Service
public class FightingStrengthService {
	Logger log = LoggerFactory.getLogger(FightingStrengthService.class);
	@Autowired
	private FightingStrengthRepository fightingStrengthRepository;
	@Autowired
	private LevelMasterRepository levelMasterRepository;
	@Autowired
	private AllianceMemberRepository allianceMemberRepository;
	private ModelMapper modelMapper;

	public FightingStrengthService() {
		modelMapper = new ModelMapper();
	}

	public void init() {
		List<AllianceMember> allianceMemberList = allianceMemberRepository.findAll();
		List<LevelMaster> levelMasterList = levelMasterRepository.findAll();
		allianceMemberList.forEach(member -> {
			Arrays.stream(Color.values()).forEach(color -> {
				levelMasterList.forEach(level -> {
					FightingStrengthDto fightingStrengthDto = new FightingStrengthDto();
					fightingStrengthDto.setColor(color);
					fightingStrengthDto.setAllianceMemberDto(modelMapper.map(member, AllianceMemberDto.class));
					fightingStrengthDto.setLevel(level.getLevel());
					fightingStrengthDto.setMemberId(member.getId());
					fightingStrengthDto.setPoint(0);
					fightingStrengthDto.setUpdateDate(DiscordBotTestApplication.sdf.format(new Date()));
					try {
						addFightingStrength(fightingStrengthDto);
					} catch (LevelException e) {
					}
				});
			});
		});
	}

	public XSSFWorkbook downloadExcel() {
		XSSFWorkbook excel = ExcelUtil.writeBeanToExcel(fightingStrengthRepository.findAll(), FightingStrength.class);
		return excel;
	}

	public void uploadExcel(InputStream is) {
		try {
			List<FightingStrength> fightingStrengthList = ExcelUtil.readExcelToBean(0, 1, is, FightingStrength.class);
			fightingStrengthList.forEach(fightingStrength -> fightingStrengthRepository.save(fightingStrength));
		} catch (Exception e) {
			log.error("エクセルアップロードエラー", e);
		}
	}

	public void updateFightingStrength(long memberId, String text) {
		// TODO ここはFineさんが実装
	}

	public void addOrUpdateFightingStrength(FightingStrengthDto fightingStrengthDto) throws LevelException {
		try {
			updateFightingStrength(fightingStrengthDto);
		} catch (NotfoundException e) {
			addFightingStrength(fightingStrengthDto);
		}
	}

	private void addFightingStrength(FightingStrengthDto fightingStrengthDto) throws LevelException {
		List<LevelMaster> levelMasterList = levelMasterRepository.findAll();
		boolean okFlag = false;
		for (LevelMaster levelMaster : levelMasterList) {
			if (levelMaster.getLevel().equals(fightingStrengthDto.getLevel())) {
				okFlag = true;
				break;
			}
		}
		if (okFlag == false)
			throw new LevelException("レベルが不正です。level=" + fightingStrengthDto.getLevel());

		FightingStrength fightingStrength = modelMapper.map(fightingStrengthDto, FightingStrength.class);
		fightingStrength.setMemberId(fightingStrengthDto.getAllianceMemberDto().getId());
		fightingStrength.setUpdateDate(DiscordBotTestApplication.sdf.format(new Date()));

		fightingStrengthRepository.save(fightingStrength);
		log.info("戦力追加 fightingStrength=" + fightingStrength);
	}

	private void updateFightingStrength(FightingStrengthDto fightingStrengthDto) throws NotfoundException {
		Optional<FightingStrength> optional = fightingStrengthRepository.findById(fightingStrengthDto.getId());
		if (optional.isEmpty())
			throw new NotfoundException("更新対象の戦力が見つかりません。id=" + fightingStrengthDto.getId());

		FightingStrength fightingStrength = optional.get();

		fightingStrength.setPoint(fightingStrengthDto.getPoint());
		fightingStrength.setUpdateDate(DiscordBotTestApplication.sdf.format(new Date()));

		fightingStrengthRepository.save(fightingStrength);
	}

	public void deleteFightingStrength(long id) {
		Optional<FightingStrength> optional = fightingStrengthRepository.findById(id);
		if (optional.isEmpty())
			return;

		FightingStrength fightingStrength = optional.get();

		fightingStrengthRepository.delete(fightingStrength);
	}

	public List<FightingStrengthDto> getFightingStrengthDtoListByAlliance(MemberAlliance memberAlliance) {
		List<FightingStrengthDto> result = new ArrayList<FightingStrengthDto>();
		List<AllianceMember> allianceMemberList = allianceMemberRepository.findAllByAlliance(memberAlliance.toString());
		allianceMemberList.forEach(member -> {
			List<FightingStrength> all = fightingStrengthRepository.findAll();
			all.forEach(fightingStrength -> {
				if (fightingStrength.getMemberId() == member.getId()) {
					FightingStrengthDto dto = modelMapper.map(fightingStrength, FightingStrengthDto.class);
					dto.setAllianceMemberDto(modelMapper.map(member, AllianceMemberDto.class));
					result.add(dto);
				}
			});
		});
		result.sort(Comparator.comparing(FightingStrengthDto::getMemberId)
				.thenComparing(FightingStrengthDto::getLevel, Comparator.reverseOrder())
				.thenComparing(FightingStrengthDto::getColor));

		return result;
	}
}
