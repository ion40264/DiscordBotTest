package bot.dto;

import jp.highwide.common.excel.ExcelColumn;
import jp.highwide.common.excel.ExcelEntity;
import jp.highwide.common.excel.ExcelEntity.Align;
import jp.highwide.common.excel.ExcelEntity.Border;
import lombok.Data;

@Data
@ExcelEntity(header = true, headerStyle = Align.ALIGN_CENTER, borderStyle = Border.STYLE2)
public class FightingStrengthExcelDto {
	@ExcelColumn(columnIndex = 0)
	private Integer id;
	@ExcelColumn(columnIndex = 1)
	private Integer memberId;
	@ExcelColumn(columnIndex = 2)
	private String ayarabuName;
	@ExcelColumn(columnIndex = 3)
	private String discordName;
	@ExcelColumn(columnIndex = 4)
	private String color;
	@ExcelColumn(columnIndex = 5)
	private Integer enemyLevel;
	@ExcelColumn(columnIndex = 6)
	private Integer point;
	@ExcelColumn(columnIndex = 7)
	private String updateDate;

}
