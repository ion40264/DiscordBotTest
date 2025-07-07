package bot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import bot.form.DiscordForm;
import bot.model.discord.DiscordBotModel;

@Controller
public class DiscordController {
	@Autowired
	private DiscordBotModel discordBotModel;
	
	@GetMapping("/discord")
	public String getDisco(Model model) {
		return "discord";
	}

	@PostMapping("/discord")
	public String postDisco(@ModelAttribute DiscordForm discordForm) {
		discordBotModel.sendMessage(discordForm.getMessage());
        return "redirect:/discord";
	}

}
