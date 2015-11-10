import java.io.*;
import java.net.*;

public final class Scraper {

	public static String getHtml(String url) throws IOException {
		StringBuilder html = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
		String line = reader.readLine();
		while (line != null) {
			html.append(line);
			line = reader.readLine();
		}
		reader.close();
		return html.toString();
	}
}