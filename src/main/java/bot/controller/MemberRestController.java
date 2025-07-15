package bot.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bot.dto.AllianceMemberDto;
import bot.form.AllianceMemberForm;
import bot.service.MemberService;

@RestController
@RequestMapping(value = "/member", produces = MediaType.APPLICATION_JSON_VALUE)
public class MemberRestController {
	private static final Logger log = LoggerFactory.getLogger(MemberRestController.class);
	@Autowired
	private MemberService memberService;

	@GetMapping
	public List<AllianceMemberDto> getAllMember() {
		return memberService.getAllianceMemberDtoList();
	}

	@PostMapping
	public void postMember(@RequestBody AllianceMemberForm allianceMemberForm) {
		log.info("メンバー追加="+allianceMemberForm);
		ModelMapper modelMapper = new ModelMapper();
		memberService.addAllianceMemberDto(modelMapper.map(allianceMemberForm, AllianceMemberDto.class));
	}
	
	@PutMapping
	public void putMember(@RequestBody AllianceMemberForm allianceMemberForm) {
		log.info("メンバー更新="+allianceMemberForm);
		ModelMapper modelMapper = new ModelMapper();
		memberService.updateAllianceMemberDto(modelMapper.map(allianceMemberForm, AllianceMemberDto.class));
	}

	@DeleteMapping("/{id}")
	public void deleteMember(@PathVariable Long id) {
		log.info("メンバー削除="+id);
		memberService.removeAllianceMemberDto(id);
	}
}
