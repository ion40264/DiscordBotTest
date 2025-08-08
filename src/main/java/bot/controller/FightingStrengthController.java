package bot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FightingStrengthController {
	@GetMapping("/fightingStrengthHtml")
	public String getIndex() {
		return "fightingStrength";
	}
}
