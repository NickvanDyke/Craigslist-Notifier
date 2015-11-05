import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;

import java.util.Date;

public final class CraigslistNotifier {
	private static ArrayList<Ad> ads = new ArrayList<Ad>(), newAds = new ArrayList<Ad>();
	private static ArrayList<String> searchTerms = new ArrayList<String>(), cities = new ArrayList<String>(), negativeKeywords = new ArrayList<String>();
	private static String email, password, recipient;
	private static double frequency;

	public static void main(String[] args) {
		loadSettings();
		loadAds();
		while (true) {
			for (String city : cities)
				for (String term : searchTerms) {
					updateAds(city, term);
					for (Ad ad : newAds)
						CraigslistNotifier.sendEmail(ad.getTitle(), "$" + ad.getPrice() + " - " + ad.getBody() + "\n" + ad.getLink());
					saveAds();
					try {
						Thread.sleep((long)(((frequency + Math.random()) * 60000) / (searchTerms.size() * cities.size())));
					}
					catch (InterruptedException e) {
						System.out.println("InterruptedException");
					}
				}
		}
	}

	public static void updateAds(String city, String term) {
		String htm = "";
		boolean skip, add;
		newAds.clear();
		System.out.println(term.replace("%20", " ") + " " + city);
		try {
			htm = Scraper.getHtml("https://" + city + ".craigslist.org/search/sss?format=rss&query=" + term + "&sort=rel");
		}
		catch (IOException e) {
			sendEmail("IP blocked", "rip");
			System.out.print("ip blocked");
			System.exit(0);
		}
		for (Ad temp : createAds(htm)) {
			skip = false;
			add = true;
			if (ads.isEmpty()) {
				ads.add(temp);
				newAds.add(temp);
			}
			for (String word : negativeKeywords)
				if (skip == false && temp.getTitle().toLowerCase().contains(word.toLowerCase())) {
					skip = true;
					add = false;
				}
			if (!skip)
				for (Ad ad : ads)
					if (temp.equals(ad))
						add = false;
			if (add) {
				ads.add(temp);
				newAds.add(temp);
			}
		}
	}

	//given the html code for a Craigslist RSS page, creates and returns an ArrayList containing Ads created from the html code
	public static ArrayList<Ad> createAds(String str) {
		ArrayList<Ad> result = new ArrayList<Ad>();
		Ad temp;
		if (str.indexOf("<rdf:Seq>") + 10 == str.indexOf("</rdf:Seq>")) {
			System.out.println("no results");
			return result;
		}
		String title, date, location, link, body;
		int price = 0;
		String[] htm =  str.substring(str.indexOf("<item rdf"), str.lastIndexOf("</item>")).split("</item>");
		for (String html : htm) {
			if (html.contains("(") && html.indexOf("(") - 1 > html.indexOf("<title><![CDATA[") + 16 && html.indexOf("(") < html.indexOf("]]></title>"))
				title = html.substring(html.indexOf("<title><![CDATA[") + 16, html.indexOf("(") - 1);
			else if (html.contains("&#x0024;"))
				title = html.substring(html.indexOf("<title><![CDATA[") + 16, html.indexOf("&#x0024;") - 1);
			else title = html.substring(html.indexOf("<title><![CDATA[") + 16, html.indexOf("]]></title>"));
			body = html.substring(html.indexOf("<description><![CDATA[") + 22, html.indexOf("]]></description>"));
			date = html.substring(html.indexOf("<dc:date>") + 9, html.indexOf("</dc:date>"));
			if (html.contains("(") && html.contains(")") && html.lastIndexOf("(") < html.lastIndexOf(")"))
				location = html.substring(html.lastIndexOf("(") + 1, html.lastIndexOf(")"));
			else location = "N/A";
			link = html.substring(html.indexOf("<link>") + 6, html.indexOf("</link>"));
			if (html.contains("&#x0024;"))
				price = Integer.parseInt(html.substring(html.indexOf("&#x0024;") + 8, html.indexOf("]]></title>")));
			else price = 0;
			temp = new Ad(title, price, date, location, link, body);
			System.out.println(temp);
			result.add(temp);
		}
		return result;
	}

	//deletes the ad if it's date is 90 days older than the current date
	public static void deleteOldAds() {
		ArrayList<Ad> adsToBeDeleted = new ArrayList<Ad>();
		for (Ad ad : ads)
			if (ad.getDate().getTime() < new Date().getTime() - 7776000000L)
				adsToBeDeleted.add(ad);
		for (Ad ad : adsToBeDeleted)
			ads.remove(ad);
	}

	public static void loadSettings() {
		Scanner sc = null;
		try {
			sc = new Scanner(new File("settings.txt"));
		}
		catch (FileNotFoundException e) {
			try {
				String n = System.getProperty("line.separator");
				FileUtils.writeStringToFile(new File("settings.txt"), "Gmail account to send emails from" + n + "address: " + n + "password: " + n + "You must enable less secure apps to access your account; do so here:" + n + "https://www.google.com/settings/security/lesssecureapps" + n + n + "recipient's email address: " + n + n + "cities to search; ensure the city actually has its own craigslist (i.e. <city>.craigslist.org is a valid url)" + n + "put each entry on its own line:" + n + n + n + "search terms; put each entry on its own line:" + n + n + n + "negative keywords; if the ad's title contains any of the given keywords, you will not receive an email notification for that ad" + n + "put each entry on its own line:" + n + n + n + "how often to check listings, in minutes: ");
			}
			catch (IOException i) {
				System.out.println("IOException while loading settings.txt");
			}
			JOptionPane.showMessageDialog(null, "This is your first time running the app.\nPlease fill out the settings.txt file that has been\ncreated in the directory that this app resides in.\nThen launch the app again.");
			System.exit(0);
		}
		String text = sc.next();
		while (!text.equals("address:"))
			text = sc.next();
		text = sc.next();
		email = text.substring(0, text.indexOf("@"));
		while (!text.equals("password:"))
			text = sc.next();
		password = sc.next();
		while (!text.equals("address:"))
			text = sc.next();
		recipient = sc.next();
		while (sc.hasNextLine() && !text.contains("cities to search"))
			text = sc.nextLine();
		sc.nextLine();
		text = sc.nextLine();
		while (sc.hasNextLine() && !text.contains("search terms; put each entry on its own line:")) {
			if (text.length() > 1)
				cities.add(text);
			text = sc.nextLine();
		}
		text = sc.nextLine();
		while (sc.hasNextLine() && !text.contains("negative keywords")) {
			if (text.length() > 1)
				searchTerms.add(text.replace(" ", "%20"));
			text = sc.nextLine();
		}
		text = sc.nextLine();
		while (sc.hasNextLine() && !text.contains("how often to check listings, in minutes: ")) {
			if (text.length() > 1)
				negativeKeywords.add(text);
			text = sc.nextLine();
		}
		frequency = Integer.parseInt(text.substring(41)) - 0.5;
		sc.close();
	}

	public static void loadAds() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("savedAds.txt"));
			boolean end = false;
			while (!end) {
				try {
					ads.add((Ad)ois.readObject());
				}
				catch (IOException e) {
					end = true;
				}
				catch (ClassNotFoundException e) {
					System.out.println("ClassNotFoundException");
				}
			}
			ois.close();
		}
		catch (IOException e) {
			System.out.println("IOException while loading ads");
		}
	}

	public static void saveAds() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("savedAds.txt"));
			for (Ad ad : ads)
				oos.writeObject(ad);
			oos.close();
		}
		catch (IOException e) {
			System.out.println("IOException while saving");
		}
	}

	public static void sendEmail(String subject, String body) {
		try {
			GoogleMail.Send(email, password, recipient, subject, body);
		}
		catch (AddressException e) {
		}
		catch (MessagingException e) {
		}
	}
}