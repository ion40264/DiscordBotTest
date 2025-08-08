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
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bot.DiscordBotTestApplication;
import bot.dto.AllianceMemberDto;
import bot.dto.Color;
import bot.dto.FightingStrengthDto;
import bot.dto.FightingStrengthExcelDto;
import bot.dto.MemberAlliance;
import bot.entity.AllianceMember;
import bot.entity.FightingStrength;
import bot.entity.LevelMaster;
import bot.repository.AllianceMemberRepository;
import bot.repository.FightingStrengthRepository;
import bot.repository.LevelMasterRepository;
import jp.highwide.common.excel.ExcelUtil;

/**
 * 戦略情報全般を管理するサービスクラス<br>
 * web、discordより呼ばれる
 */
@Service
public class FightingStrengthService {
	Logger log = LoggerFactory.getLogger(FightingStrengthService.class);
	@Autowired
	private FightingStrengthRepository fightingStrengthRepository;
	@Autowired
	private LevelMasterRepository levelMasterRepository;
	@Autowired
	private AllianceMemberRepository allianceMemberRepository;
	@Autowired
	private MemberService memberService;
	private ModelMapper modelMapper;

	public FightingStrengthService() {
		modelMapper = new ModelMapper();
	}

	/**
	 * 戦力情報をすべて削除し、登録されているメンバーの戦力を0Pですべて登録する。
	 */
	public void init() {
		fightingStrengthRepository.deleteAll();
		List<AllianceMember> allianceMemberList = allianceMemberRepository.findAll();
		allianceMemberList.forEach(allianceMember -> {
			addDefault(allianceMember);
		});
	}
	/**
	 * 新しく入ってきたメンバーの戦力を0Pですべて登録する。
	 * @param allianceMember 登録するメンバー
	 */
	public void addDefault(AllianceMember allianceMember) {
		List<FightingStrengthDto> strengthDtoList = getDefault(allianceMember);
		strengthDtoList.forEach(fightingStrengthDto->{
			try {
				addFightingStrength(fightingStrengthDto);
			} catch (LevelException e) {
			}
		});
		
	}
	private List<FightingStrengthDto> getDefault(AllianceMember allianceMember) {
		List<FightingStrengthDto> result = new ArrayList<FightingStrengthDto>();
		FightingStrengthDto fightingStrengthDto = new FightingStrengthDto();
		List<LevelMaster> levelMasterList = levelMasterRepository.findAll();
		Arrays.stream(Color.values()).forEach(color -> {
			levelMasterList.forEach(level -> {
				fightingStrengthDto.setColor(color);
				fightingStrengthDto.setAllianceMemberDto(modelMapper.map(allianceMember, AllianceMemberDto.class));
				fightingStrengthDto.setEnemyLevel(level.getEnemyLevel());
				fightingStrengthDto.setMemberId(allianceMember.getId());
				fightingStrengthDto.setPoint(0);
				fightingStrengthDto.setUpdateDate(DiscordBotTestApplication.sdf.format(new Date()));
				result.add(fightingStrengthDto);
			});
		});
		return result;
	}
	/**
	 * 戦力の一覧をエクセルに出力する。
	 * @return 戦力一覧のエクセル
	 */
	public XSSFWorkbook downloadExcel() {
		List<FightingStrengthExcelDto> fightingStrengthExcelDtoList = modelMapper
				.map(fightingStrengthRepository.findAll(), new TypeToken<List<FightingStrengthExcelDto>>() {
				}.getType());
		fightingStrengthExcelDtoList.forEach(dto->{
			Optional<AllianceMember> optional = allianceMemberRepository.findById(dto.getMemberId());
			dto.setAyarabuName(optional.get().getAyarabuName());
			dto.setDiscordName(optional.get().getDiscordName());
		});
		XSSFWorkbook excel = ExcelUtil.writeBeanToExcel(fightingStrengthExcelDtoList, FightingStrengthExcelDto.class);
		return excel;
	}

	/**
	 * エクセルに書かれた戦力を登録する
	 * @param is エクセルファイルのインプットストリーム
	 */
	public void uploadExcel(InputStream is) {
		try {
			List<FightingStrengthExcelDto> fightingStrengthExcelDtoList = ExcelUtil.readExcelToBean(0, 1, is,
					FightingStrengthExcelDto.class);
			fightingStrengthExcelDtoList.forEach(dto->{
				AllianceMemberDto allianceMemberDto = memberService.getAllianceMemberDtoByMemberId(dto.getMemberId());
				dto.setMemberId(allianceMemberDto.getId());
			});
			List<FightingStrength> fightingStrengthList = modelMapper.map(fightingStrengthExcelDtoList,
					new TypeToken<List<FightingStrength>>() {
					}.getType());
			fightingStrengthList.forEach(fightingStrength -> {
				fightingStrengthRepository.save(fightingStrength);
			});
		} catch (Exception e) {
			log.error("エクセルアップロードエラー", e);
		}
	}

	/**
	 * 指定された書式の戦力を登録する。<br>
	 * 書式は以下の形式で書かれる<br>
	 * 
	 * @param memberId 戦力を登録するメンバーID
	 * @param text 戦力が記載された文字列
	 */
	public void updateFightingStrength(long memberId, String text) {
		// TODO ここはFineさんが実装
	}

	/**
	 * 戦力を追加、もしくは更新する。
	 * @param fightingStrengthDto 追加、更新する戦力
	 * @throws LevelException 不正なレベルが指定された場合にthrowされる
	 */
	public void addOrUpdateFightingStrength(FightingStrengthDto fightingStrengthDto) throws LevelException {
		try {
			updateFightingStrength(fightingStrengthDto);
		} catch (NotfoundException e) {
			addFightingStrength(fightingStrengthDto);
		}
	}

	/**
	 * 戦力を追加する。
	 * @param fightingStrengthDto 追加戦力
	 * @throws LevelException 不正なレベルが指定された場合にthrowされる
	 */
	private void addFightingStrength(FightingStrengthDto fightingStrengthDto) throws LevelException {
		List<LevelMaster> levelMasterList = levelMasterRepository.findAll();
		boolean okFlag = false;
		for (LevelMaster levelMaster : levelMasterList) {
			if (levelMaster.getEnemyLevel().equals(fightingStrengthDto.getEnemyLevel())) {
				okFlag = true;
				break;
			}
		}
		if (okFlag == false)
			throw new LevelException("レベルが不正です。level=" + fightingStrengthDto.getEnemyLevel());

		FightingStrength fightingStrength = modelMapper.map(fightingStrengthDto, FightingStrength.class);
		fightingStrength.setMemberId(fightingStrengthDto.getAllianceMemberDto().getId());
		fightingStrength.setUpdateDate(DiscordBotTestApplication.sdf.format(new Date()));

		fightingStrengthRepository.save(fightingStrength);
		log.info("戦力追加 fightingStrength=" + fightingStrength);
	}

	/**
	 * 戦力を更新する。
	 * @param fightingStrengthDto 更新する戦力
	 * @throws NotfoundException 更新対象の戦力が見つからなかった場合throwされる
	 */
	private void updateFightingStrength(FightingStrengthDto fightingStrengthDto) throws NotfoundException {
		Optional<FightingStrength> optional = fightingStrengthRepository.findById(fightingStrengthDto.getId());
		if (optional.isEmpty())
			throw new NotfoundException("更新対象の戦力が見つかりません。id=" + fightingStrengthDto.getId());

		FightingStrength fightingStrength = optional.get();

		fightingStrength.setPoint(fightingStrengthDto.getPoint());
		fightingStrength.setUpdateDate(DiscordBotTestApplication.sdf.format(new Date()));

		fightingStrengthRepository.save(fightingStrength);
	}

	/**
	 * 戦力を削除する。
	 * @param id 削除する戦力のID
	 */
	public void deleteFightingStrength(Integer id) {
		Optional<FightingStrength> optional = fightingStrengthRepository.findById(id);
		if (optional.isEmpty())
			return;

		FightingStrength fightingStrength = optional.get();

		fightingStrengthRepository.delete(fightingStrength);
	}

	/**
	 * 指定された同盟の戦力一覧を返却する。
	 * @param memberAlliance 同盟
	 * @return 同盟の戦力のリスト
	 */
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
				.thenComparing(FightingStrengthDto::getEnemyLevel, Comparator.reverseOrder())
				.thenComparing(FightingStrengthDto::getColor));

		return result;
	}
}
