package bot.model.google;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Component
public class SpreadSheetModel {
	private static final String SPREADS_SHEET_ID = "1t5SUA-CTqIzfiw4oNRP5oymZR81ykgt2JPCBNiX8s2I";

	public static void main(String[] args) throws Exception {
		SpreadSheetModel s = new SpreadSheetModel();
		Sheets sheets = s.getSpreadsheets();
		Request request = new Request();
		CellFormat format = new CellFormat();
		Color color = new Color();
		color.setRed(1f); //R(レッド)
		format.setBackgroundColor(color);

		GridRange gridRange = new GridRange()
				.setSheetId(0) // シートのID (最初のシートは通常0)
				.setStartRowIndex(0) // 0-indexed, A1の行は0
				.setEndRowIndex(1) // 0-indexed, A1の行までなので1
				.setStartColumnIndex(0) // 0-indexed, A1の列は0
				.setEndColumnIndex(1); // 0-indexed, A1の列までなので1

		// 設定したい色 (RGB値, 各0.0-1.0) - 例: 赤
		Color redColor = new Color()
				.setRed(1.0f) // 赤の最大値
				.setGreen(0.0f) // 緑なし
				.setBlue(0.0f) // 青なし
				.setAlpha(1.0f); // 不透明度 (完全に不透明)

		// セルに適用するフォーマット
		CellFormat cellFormat = new CellFormat()
				.setBackgroundColor(redColor);

		// 変更を適用するCellData (ここでは値は変更せずフォーマットのみ)
		CellData cellData = new CellData()
				.setUserEnteredFormat(cellFormat);

		// RowData (1つの行に1つのセルデータを設定)
		RowData rowData = new RowData()
				.setValues(Collections.singletonList(cellData));

		// UpdateCellsRequest の作成
		UpdateCellsRequest updateCellsRequest = new UpdateCellsRequest()
				.setRange(gridRange)
				.setRows(Collections.singletonList(rowData)) // 適用する行データ
				.setFields("userEnteredFormat.backgroundColor"); // 変更するフィールドを指定

		// BatchUpdateSpreadsheetRequest の作成
		BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
				.setRequests(Collections.singletonList(new Request().setUpdateCells(updateCellsRequest)));

		// リクエストの実行
		sheets.spreadsheets().batchUpdate(SPREADS_SHEET_ID, batchUpdateRequest).execute();

		System.out.println("セルの色が設定されました。");

	}

	public Sheets getSpreadsheets() throws RuntimeException, IOException {

		List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
				.createScoped(scopes);

		HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
				credentials);

		// Create the sheets API client
		Sheets sheets = new Sheets.Builder(new NetHttpTransport(),
				GsonFactory.getDefaultInstance(),
				requestInitializer)
						.setApplicationName("ayarabu")
						.build();

		//		String range = "シート1!A1";
		//		List<List<Object>> values = Arrays.asList(
		//				Arrays.asList("Header 1", "Header 2", "Header 3"),
		//				Arrays.asList("Data A1", "Data B1", "Data C1"));
		//
		//		ValueRange body = new ValueRange()
		//				.setValues(values);
		//
		//		Sheets.Spreadsheets.Values.Update request = sheets.spreadsheets().values().update(SPREADS_SHEET_ID, range, body);
		//		request.setValueInputOption("RAW");
		//		request.execute();

		return sheets;
	}
}
