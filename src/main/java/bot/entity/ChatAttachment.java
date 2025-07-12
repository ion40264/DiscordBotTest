package bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Data;

@Data
@Entity
public class ChatAttachment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne // ChatMessage への多対一関係を定義
	@JoinColumn(name = "chat_message_id") // 外部キーカラムを指定
	private ChatMessage chatMessage;
	private String attachmentUrl;
	private String attachmentFileName;
}
