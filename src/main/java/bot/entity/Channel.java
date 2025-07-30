package bot.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import lombok.Data;

@Data
@Entity
public class Channel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String channelId;
	private String channelName;
	@OneToMany(mappedBy = "channel", fetch = FetchType.EAGER)
	private List<ChatMessage> chatMessageList = new ArrayList<>();
}
