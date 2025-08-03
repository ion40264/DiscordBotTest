package bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jp.highwide.common.excel.ExcelColumn;
import jp.highwide.common.excel.ExcelEntity;
import jp.highwide.common.excel.ExcelEntity.Align;
import jp.highwide.common.excel.ExcelEntity.Border;
import lombok.Data;

@Data
@Entity
@ExcelEntity(header = true, headerStyle = Align.ALIGN_CENTER, borderStyle = Border.STYLE2)
public class FightingStrength {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ExcelColumn(columnIndex = 0)
	private Long id;
	@ExcelColumn(columnIndex = 1)
	private String color;
	@ExcelColumn(columnIndex = 2)
	private Integer level;
	@ExcelColumn(columnIndex = 3)
	private Integer point;
	@ExcelColumn(columnIndex = 4)
	private Long memberId;
	@ExcelColumn(columnIndex = 5)
	private String updateDate;
}
