import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;

public final class Craigslist {
	private static ArrayList<Ad> ads = new ArrayList<Ad>();
	private static ArrayList<Ad> newAds = new ArrayList<Ad>();
	private static ArrayList<String> searchTerms = new ArrayList<String>();
	private static ArrayList<String> cities = new ArrayList<String>();
	private static ArrayList<String> negativeKeywords = new ArrayList<String>();
	private static String cityy;
	private static double frequency;

	public static String[] splitLocalListingsHtml(String html) {
		if (html.contains("<h4"))
			return html.substring(html.indexOf("<p"), html.indexOf("<h4")).split("</p>");
		return html.substring(html.indexOf("<p"), html.lastIndexOf("<div id=\"mapcontainer")).split("</p>");
	}

	public static void updateAds() {
		Ad match = null;
		String htm = "";
		int result = -2;
		boolean firstPageDone;
		newAds.clear();
		for (String city : cities) {
			cityy = city;
			for (String term : searchTerms) {
				firstPageDone = false;
				System.out.println(term.replace("%20", " ") + " " + city);
				do {
					try {
						if (firstPageDone)
							htm = Scraper.getHtml(htm.substring(htm.indexOf("next\" href=\"") + 12, htm.indexOf("<meta name=\"viewport") - 2));
						else htm = Scraper.getHtml("https://" + city + ".craigslist.org/search/sss?sort=rel&query=" + term);
					}
					catch (IOException e) {
						CraigslistNotifier.sendEmail("IP blocked", "rip");
						System.out.print("ip banned");
						System.exit(0);
					}
					//System.out.println(htm);
					for (Ad temp : createAds(htm)) {
						System.out.println(temp);
						if (ads.isEmpty()) {
							ads.add(temp);
							newAds.add(temp);
						}
						result = -2;
						for (Ad ad : ads) {
							//System.out.println(temp.getTitle() + " compared to " + ad.getTitle());
							if (result < temp.compareTo(ad)) {
								result = temp.compareTo(ad);
								if (result == 1)
									match = ad;
							}
							//System.out.println(result);
						}
						switch (result) {
						case 0:
							ads.add(temp);
							newAds.add(temp);
							break;
						case 1:
							ads.add(temp);
							temp.setOldPrice(match.getPrice());
							ads.remove(match);
							newAds.add(temp);
							break;
						default: break;
						}
					}
					firstPageDone = true;
					try {
						System.out.println("sleeping");
						Thread.sleep((long)(((frequency + Math.random()) * 60000) / (searchTerms.size() * cities.size())));
						System.out.println("resuming");
					}
					catch (InterruptedException e) {
						System.out.println("InterruptedException");
					}
				} while (htm.contains("next\" href=\""));
			}
		}
		for (Ad ad : ads)
			for (String neg : negativeKeywords)
				if (ad.getTitle().toLowerCase().contains(neg.toLowerCase()))
					ads.remove(ad);
		for (Ad ad : newAds)
			for (String neg : negativeKeywords)
				if (ad.getTitle().toLowerCase().contains(neg.toLowerCase()))
					newAds.remove(ad);
	}

	//given the html code for a Craigslist page, creates and returns an ArrayList containing Ads created from the html code
	public static ArrayList<Ad> createAds(String str) {
		ArrayList<Ad> result = new ArrayList<Ad>();
		Ad temp;
		if (str.contains("noresults")) {
			System.out.println("noresults");
			return result;
		}
		String title, date, location, link;
		String[] htm;
		int price = 0;
		if (str.contains("<h4"))
			htm =  str.substring(str.indexOf("<p"), str.indexOf("<h4")).split("</p>");
		else htm = str.substring(str.indexOf("<p"), str.indexOf("<div id=\"mapcontainer")).split("</p>");
		for (String html : htm) {
			title = html.substring(html.indexOf("hdrlnk") + 8, html.indexOf("</a> </span>"));
			date = html.substring(html.indexOf("title") + 7, html.indexOf("title") + 29);
			if (html.contains("<small>"))
				location = html.substring(html.indexOf("<small>") + 9, html.indexOf("</small>") - 1);
			else location = "N/A";
			link = html.substring(html.indexOf("href") + 6, html.indexOf("html") + 4);
			if (html.contains("price"))
				price = Integer.parseInt(html.substring(html.indexOf("price") + 8, html.indexOf("</span")));
			else price = 0;
			temp = new Ad(title, price, date, location, link, cityy);
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

	public static void loadSettings() throws FileNotFoundException {
		Scanner sc = new Scanner(new File("settings.txt"));
		String text = sc.next();
		while (!text.equals("address:"))
			text = sc.next();
		text = sc.next();
		CraigslistNotifier.setEmail(text.substring(0, text.indexOf("@")));
		while (!text.equals("password:"))
			text = sc.next();
		CraigslistNotifier.setPassword(sc.next());
		while (!text.equals("address:"))
			text = sc.next();
		CraigslistNotifier.setRecipient(sc.next());
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

	public static void loadAds() throws IOException {
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

	public static void saveAds() throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("savedAds.txt"));
		for (Ad ad : ads)
			oos.writeObject(ad);
		oos.close();
	}

	public static ArrayList<Ad> getAds() {
		return ads;
	}

	public static ArrayList<Ad> getNewAds() {
		return newAds;
	}

	public static ArrayList<String> getTerms() {
		return searchTerms;
	}

	public static ArrayList<String> getCities() {
		return cities;
	}
}