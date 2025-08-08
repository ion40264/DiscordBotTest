package bot.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bot.dto.AllianceMemberDto;
import bot.form.ExcelUploadForm;
import bot.form.FightingStrengthForm;
import bot.service.FightingStrengthService;
import bot.service.MemberService;

@RestController
@RequestMapping(value = "/fightingStrength", produces = MediaType.APPLICATION_JSON_VALUE)
public class FightingStrengthRestController {
	Logger log = LoggerFactory.getLogger(FightingStrengthService.class);
	@Autowired
	private FightingStrengthService fightingStrengthService;
	@Autowired
	private MemberService memberService;

	@GetMapping("/init")
	public void initFightingStrength() {
		fightingStrengthService.init();
	}
	@PutMapping
	public void putFightingStrength(FightingStrengthForm fightingStrengthForm) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String ayarabuName = authentication.getName();
		AllianceMemberDto allianceMemberDto = memberService.getAllianceMemberDtoByAyarabuName(ayarabuName);
		fightingStrengthService.updateFightingStrength(allianceMemberDto.getId(), fightingStrengthForm.getText());
	}
	@PostMapping("/excel")
	public void uploadFile(ExcelUploadForm excelUploadForm) {
		try {
			log.info("excelUploadForm=" + excelUploadForm);
			if (excelUploadForm.getMultipartFiles().length == 0)
				return;
			InputStream inputStream = excelUploadForm.getMultipartFiles()[0].getInputStream();
			fightingStrengthService.uploadExcel(inputStream);
		} catch (Exception e) {
			log.error("アップロードで失敗しました。excelUploadForm=" + excelUploadForm, e);
		}
	}

	@GetMapping("/excel")
	public ResponseEntity<Resource> downloadFile() {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			fightingStrengthService.downloadExcel().write(byteArrayOutputStream);
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
