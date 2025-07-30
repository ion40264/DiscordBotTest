package bot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import bot.entity.Channel;
import bot.repository.ChannelRepository;

@Controller
public class ChatController {
	@Autowired
	private ChannelRepository channelRepository;
	@GetMapping("/chatHtml/{channelId}")
    public String index(@PathVariable String channelId, Model model) {
		Channel channel = channelRepository.findByChannelId(channelId);
		model.addAttribute("channelName", channel.getChannelName());
		model.addAttribute("channelId", channelId);
        return "chat";
    }
}
