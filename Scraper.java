import java.io.*;
import java.net.*;

public final class Scraper {
	private static String html;

	public static String getHtml(String url) throws IOException {
		html = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			String line = reader.readLine();
			while (line != null) {
				html += line;
				line = reader.readLine();
			}
		return html;
	}
}