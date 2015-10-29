import java.io.*;
import java.net.*;

public final class Scraper {
	private static String html;

	public static String getHtml(String url) throws IOException {
		html = "";
		//Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("14.152.49.193", 80));
		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
		String line = reader.readLine();
		while (line != null) {
			html += line;
			line = reader.readLine();
		}
		return html;
	}
}