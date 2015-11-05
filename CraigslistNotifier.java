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
						sendEmail(ad.getTitle(), "$" + ad.getPrice() + " in " + ad.getLocation() + "\n" + ad.getLink());
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
		String html = "";
		boolean skip, add;
		newAds.clear();
		System.out.println(term.replace("%20", " ") + " " + city);
		try {
			html = Scraper.getHtml("http://" + city + ".craigslist.org/search/sss?sort=date&query=" + term);
		}
		catch (IOException e) {
			sendEmail("IP blocked", "rip");
			System.out.print("ip blocked");
			System.exit(0);
		}
		for (Ad temp : createAds(html)) {
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

	//given the html code for a Craigslist page, creates and returns an ArrayList containing Ads created from the html code
	public static ArrayList<Ad> createAds(String str) {
		ArrayList<Ad> result = new ArrayList<Ad>();
		if (str.contains("noresults")) {
			System.out.println("no results");
			return result;
		}
		Ad temp;
		String title, date, location = "n/a", link, city;
		String[] splitHtml;
		int price = 0;
		city = str.substring(str.indexOf("<option value=\"") + 15, str.indexOf("</option>"));
		city = city.substring(0, city.indexOf("\">"));
		if (str.contains("<h4"))
			splitHtml =  str.substring(str.indexOf("<p"), str.indexOf("<h4")).split("</p>");		
		else splitHtml = str.substring(str.indexOf("<p"), str.indexOf("<div id=\"mapcontainer")).split("</p>");
		for (String adHtml : splitHtml) {
			title = adHtml.substring(adHtml.indexOf("hdrlnk") + 8, adHtml.indexOf("</a> </span>"));
			date = adHtml.substring(adHtml.indexOf("title") + 7, adHtml.indexOf("title") + 29);
			if (adHtml.contains("<small>"))
				location = adHtml.substring(adHtml.indexOf("<small>") + 9, adHtml.indexOf("</small>") - 1);
			link = "https://" + city + ".craigslist.org" + adHtml.substring(adHtml.indexOf("href") + 6, adHtml.indexOf("html") + 4);
			if (adHtml.contains("price"))
				price = Integer.parseInt(adHtml.substring(adHtml.indexOf("price") + 8, adHtml.indexOf("</span")));		
			temp = new Ad(title, price, date, location, link);
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