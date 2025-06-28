package bot.util.google;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpreadSheetTest {
	@Autowired
	private SpreadSheet spreadSheet;

	@Test
	void testWriteSpreadSheat() {
		spreadSheet.writeSpreadSheat(null, null);
	}

}
