package bot.util.google;

import lombok.Data;

@Data
public class AyarabuHeader {
	@Column(index = 0)
	private Integer id;
	@Column(index = 1)
	private Integer name;
	@Column(index = 1)
	private Integer ayarabuId;
	@Column(index = 1)
	private String versus1;
	private String firstRecord;
	private Integer level;
}
