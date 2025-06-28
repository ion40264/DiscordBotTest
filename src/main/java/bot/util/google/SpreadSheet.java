package bot.util.google;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

@Component
public class SpreadSheet {
	public <T> void writeSpreadSheat(List<T> recordList, AyarabuHeader ayarabuHeader) {
	}
	public Sheets getSpreadsheets() throws RuntimeException, IOException {

		List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
				.createScoped(scopes);

		HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
				credentials);

		Sheets sheets = new Sheets.Builder(new NetHttpTransport(),
				GsonFactory.getDefaultInstance(),
				requestInitializer)
						.setApplicationName("ayarabu")
						.build();


		return sheets;
	}
}
