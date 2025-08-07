package bot.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bot.DiscordBotTestApplication;
import bot.dto.AllianceMemberDto;
import bot.form.AllianceMemberForm;
import bot.form.ExcelUploadForm;
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
		AllianceMemberDto allianceMemberDto = modelMapper.map(allianceMemberForm, AllianceMemberDto.class);
		allianceMemberDto.setId(null);
		allianceMemberDto.setCreateDate(DiscordBotTestApplication.sdf.format(new Date()));
		memberService.addAllianceMemberDto(allianceMemberDto);
	}
	
	@PutMapping
	public void putMember(@RequestBody AllianceMemberForm allianceMemberForm) {
//		log.info("メンバー更新="+allianceMemberForm);
		ModelMapper modelMapper = new ModelMapper();
		memberService.updateAllianceMemberDto(modelMapper.map(allianceMemberForm, AllianceMemberDto.class));
	}

	@DeleteMapping("/{id}")
	public void deleteMember(@PathVariable Integer id) {
		log.info("メンバー削除="+id);
		memberService.removeAllianceMemberDto(id);
	}
	@PostMapping("/excel")
	public void uploadFile(ExcelUploadForm excelUploadForm) {
		try {
			log.info("excelUploadForm=" + excelUploadForm);
			if (excelUploadForm.getMultipartFiles().length == 0)
				return;
			InputStream inputStream = excelUploadForm.getMultipartFiles()[0].getInputStream();
			memberService.uploadExcel(inputStream);
		} catch (Exception e) {
			log.error("アップロードで失敗しました。excelUploadForm=" + excelUploadForm, e);
		}
	}

	@GetMapping("/excel")
	public ResponseEntity<Resource> downloadFile() {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			memberService.downloadExcel().write(byteArrayOutputStream);
		} catch (IOException e) {
			log.error("エクセルファイルの出力に失敗しました。", e);
			return ResponseEntity.internalServerError().build();
		}
		byte[] bytes = byteArrayOutputStream.toByteArray();
		Resource resource = new ByteArrayResource(bytes);
		String contentType = "application/octet-stream";
		HttpHeaders headers = new HttpHeaders();
        // Content-Disposition でダウンロード時のファイル名を指定
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ightingStrength.xlsx\"");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(bytes.length)
                .headers(headers)
                .body(resource);
	}

}
