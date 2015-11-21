import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.Date;

public final class CraigslistNotifier {
	private static ArrayList<Ad> ads = new ArrayList<Ad>(), newAds = new ArrayList<Ad>();
	private static ArrayList<String> searchTerms = new ArrayList<String>(), cities = new ArrayList<String>(), negativeKeywords = new ArrayList<String>();
	private static String email, password = "", recipient;
	private static double frequency;
	private static JFrame f = new JFrame("Settings");
	private static boolean firstTime, settingsChanged;

	public static void main(String[] args) {
		createSystemTrayIcon();
		loadSettings();
		constructSettingsGUI();
		loadAds();
		if (firstTime) {
			f.setVisible(true);
			JOptionPane.showMessageDialog(null, "No saved settings found. Please enter your settings.\nYou can view more info by hovering over labels.\nChanges to settings will take effect when the window is closed.\nNote that making more than 1 request every\n~3 minutes may result in an automatic IP ban by Craigslist.\n\nAccess various options by right-clicking the icon in the system tray.");
			Object lock = new Object();
			Thread t = new Thread() {
				public void run() {
					synchronized(lock) {
						while (f.isVisible()) {
							try {
								lock.wait();
							} catch (InterruptedException e) {
								System.out.println("InterruptedException");
							}
						}
						loadSettings();
					}
				}
			};
			t.start();
			f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					synchronized (lock) {
						f.setVisible(false);
						lock.notify();
					}
				}

			});
			try {
				t.join();
			} catch (InterruptedException e) {
				System.out.println("InterruptedException");
			}
		}
		while (true) {
			settingsChanged = false;
			start:
				for (int c = 0; c < cities.size(); c++) {
					if (settingsChanged)
						break start;
					for (int t = 0; t < searchTerms.size(); t++) {
						if (settingsChanged)
							break start;
						System.out.println(cities.get(c) + " " + searchTerms.get(t));
						updateAds(cities.get(c), searchTerms.get(t));
						for (Ad ad : newAds)
							sendEmail(ad.getTitle(), "$" + ad.getPrice() + " in " + ad.getLocation() + "\n" + ad.getLink());
						saveAds();
						try {
							Thread.sleep((long)(((frequency + Math.random()) * 60000) / (searchTerms.size() * cities.size())));
						} catch (InterruptedException e) {
							System.out.println("InterruptedException");
						}
						if (settingsChanged)
							break start;
					}
					if (settingsChanged)
						break start;
				}
		}
	}

	public static void constructSettingsGUI() {
		String sCities = "", sTerms = "", sNegs = "";
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword("Nick");
		Container p = f.getContentPane();
		f.setLayout(null);
		JLabel lEmailInstructions = new JLabel("Gmail account to send emails from");
		JLabel lEmail = new JLabel("address:");
		JLabel lPassword = new JLabel("password:");
		JLabel lLink = new JLabel("<html>You must <a href=\"\">allow<br>less secure apps</a></html>");
		lLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lLink.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("https://www.google.com/settings/security/lesssecureapps"));
				} catch (IOException i) {
					System.out.println("IOException while opening webpage");
				} catch (URISyntaxException u) {
					System.out.println("URISyntaxException");
				}
			}
		});
		JLabel lRecipient = new JLabel("recipient:");
		JLabel lCities = new JLabel("cities to search:");
		JLabel lTerms = new JLabel("search terms:");
		JLabel lNeg = new JLabel("negative keywords:");
		JLabel lRefresh = new JLabel("refresh search results every               minutes");
		JLabel lRequests = new JLabel();
		if (searchTerms.size() * cities.size() == 0)
			lRequests.setText("<html>You are currently making 1<br>request every ∞ minutes</html>");
		else lRequests.setText("<html>You are currently making 1<br>request every " + frequency/(searchTerms.size() * cities.size()) + " minutes</html>");
		//load text fields
		for (int i = 0; i < cities.size(); i++) {
			sCities += cities.get(i);
			if (i < cities.size() - 1)
				sCities += ", ";
		}
		for (int i = 0; i < searchTerms.size(); i++) {
			sTerms += searchTerms.get(i);
			if (i < searchTerms.size() - 1)
				sTerms += ", ";
		}
		for (int i = 0; i < negativeKeywords.size(); i++) {
			sNegs += negativeKeywords.get(i);
			if (i < negativeKeywords.size() - 1)
				sNegs += ", ";
		}
		JTextField tEmail = new JTextField(CraigslistNotifier.email, 13);
		JTextField tRecipient = new JTextField(recipient, 13);
		JTextField tCities = new JTextField(sCities, 9);
		JTextField tTerms = new JTextField(sTerms, 25);
		JTextField tNeg = new JTextField(sNegs, 22);
		JTextField tRefresh = new JTextField(Double.toString(frequency), 3);
		JPasswordField tPassword = new JPasswordField(password, 12);
		JCheckBox cSavePass = new JCheckBox("save password locally");
		if (password.length() > 0)
			cSavePass.setSelected(true);
		JButton bDonate = new JButton(new ImageIcon(CraigslistNotifier.class.getResource("/donateButton.png")));
		lEmail.setToolTipText("Gmail address to send emails from");
		lLink.setToolTipText("https://www.google.com/settings/security/lesssecureapps");
		lRecipient.setToolTipText("Email address to send emails to; doesn't have to be a Gmail address");
		lCities.setToolTipText("Ensure <city>.craigslist.org is a valid url; separate multiple entries with a comma and space");
		lTerms.setToolTipText("Separate multiple entries with a comma and space");
		lNeg.setToolTipText("If an ad's title contains any negative keyword, you will not be notified of it; separate multiple entries with a comma and space");
		lRequests.setToolTipText("Making more than 1 request every ~3 minutes may result in an automatic IP ban from Craigslist");
		lPassword.setToolTipText("Password for the Gmail account that emails are sent from; used for nothing else than sending emails, and only encrypted and stored locally if you choose to");
		cSavePass.setToolTipText("Your password will be encrypted before saving");
		bDonate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI("https://www.paypal.me/NicholasVanDyke"));
					} catch (IOException i) {
						System.out.println("IOException while opening webpage");
					} catch (URISyntaxException u) {
						System.out.println("URISyntaxException while opening webpage");
					}
				}
			}
		});
		//add elements to container
		p.add(lEmailInstructions);
		p.add(lEmail);
		p.add(lPassword);
		p.add(lLink);
		p.add(lRecipient);
		p.add(lCities);
		p.add(lTerms);
		p.add(lNeg);
		p.add(lRefresh);
		p.add(lRequests);
		p.add(tEmail);
		p.add(tPassword);
		p.add(tRecipient);
		p.add(tCities);
		p.add(tTerms);
		p.add(tNeg);
		p.add(tRefresh);
		p.add(cSavePass);
		p.add(bDonate);
		Insets insets = f.getInsets();
		//position labels
		Dimension size = lEmailInstructions.getPreferredSize();
		lEmailInstructions.setBounds(5 + insets.left, 1 + insets.top, 300, size.height);
		size = lEmail.getPreferredSize();
		lEmail.setBounds(5 + insets.left, 22 + insets.top, size.width, size.height);
		size = lPassword.getPreferredSize();
		lPassword.setBounds(5 + insets.left, 43 + insets.top, size.width, size.height);
		size = lLink.getPreferredSize();
		lLink.setBounds(235 + insets.left, 6 + insets.top, size.width, size.height);
		size = lRecipient.getPreferredSize();
		lRecipient.setBounds(5 + insets.left, 64 + insets.top, size.width, size.height);
		size = lCities.getPreferredSize();
		lCities.setBounds(5 + insets.left, 85 + insets.top, size.width, size.height);
		size = lTerms.getPreferredSize();
		lTerms.setBounds(5 + insets.left, 106 + insets.top, size.width, size.height);
		size = lNeg.getPreferredSize();
		lNeg.setBounds(5 + insets.left, 127 + insets.top, size.width, size.height);
		size = lRefresh.getPreferredSize();
		lRefresh.setBounds(5 + insets.left, 148 + insets.top, size.width, size.height);
		size = lRequests.getPreferredSize();
		lRequests.setBounds(210 + insets.left, 69 + insets.top, size.width, size.height);
		//position text boxes
		size = tEmail.getPreferredSize();
		tEmail.setBounds(59 + insets.left, 20 + insets.top, size.width, size.height);
		size = tPassword.getPreferredSize();
		tPassword.setBounds(70 + insets.left, 41 + insets.top, size.width, size.height);
		size = tRecipient.getPreferredSize();
		tRecipient.setBounds(59 + insets.left, 62 + insets.top, size.width, size.height);
		size = tCities.getPreferredSize();
		tCities.setBounds(103 + insets.left, 83 + insets.top, size.width, size.height);
		size = tTerms.getPreferredSize();
		tTerms.setBounds(89 + insets.left, 104 + insets.top, size.width, size.height);
		size = tNeg.getPreferredSize();
		tNeg.setBounds(122 + insets.left, 125 + insets.top, size.width, size.height);
		size = tRefresh.getPreferredSize();
		tRefresh.setBounds(172 + insets.left, 146 + insets.top, size.width, size.height);
		size = cSavePass.getPreferredSize();
		cSavePass.setBounds(205 + insets.left, 38 + insets.top, size.width, size.height);
		bDonate.setBounds(271 + insets.left, 146 + insets.top, 96, 21);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				String n = System.getProperty("line.separator");
				try {
					BufferedWriter w = new BufferedWriter(new FileWriter(new File("settings.txt")));
					w.write(tEmail.getText() + n);
					if (cSavePass.isSelected())
						w.write(textEncryptor.encrypt(tPassword.getText()));
					else
						password = tPassword.getText();
					w.write(n);
					w.write(tRecipient.getText() + n);
					w.write(tCities.getText() + n);
					w.write(tTerms.getText() + n);
					w.write(tNeg.getText() + n);
					w.write(tRefresh.getText());
					w.close();
				} catch (IOException e) {
					System.out.println("Error writing settings to file");
				}
				loadSettings();
				if (searchTerms.size() * cities.size() == 0)
					lRequests.setText("<html>You are currently making 1<br>request every ∞ minutes</html>");
				else lRequests.setText("<html>You are currently making 1<br>request every " + frequency/(searchTerms.size() * cities.size()) + " minutes</html>");
			}
		});
		f.setSize(379, 202);
		f.setResizable(false);
		f.setLocationRelativeTo(null);
	}

	public static void loadSettings() {
		ArrayList<String> st = new ArrayList<String>(searchTerms), c = new ArrayList<String>(cities), nk = new ArrayList<String>(negativeKeywords);
		String text;
		Scanner lines = null, tokens = null;
		searchTerms.clear();
		cities.clear();
		negativeKeywords.clear();
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword("Nick");
		try {
			lines = new Scanner(new File("settings.txt"));
		} catch (FileNotFoundException e) {
			try {
				BufferedWriter w = new BufferedWriter(new FileWriter(new File("settings.txt")));
				String n = System.getProperty("line.separator");
				w.write("example@gmail.com" + n + n);
				w.write("example@gmail.com" + n + n + n + n);
				w.write("20");
				w.close();
				lines = new Scanner(new File("settings.txt"));
			} catch (IOException i) {
				System.out.println("IOException creating settings.txt");
			}
			firstTime = true;
		}
		email = lines.nextLine();
		text = lines.nextLine();
		if (text.length() > 0)
			password = textEncryptor.decrypt(text);
		recipient = lines.nextLine();
		tokens = new Scanner(lines.nextLine());
		tokens.useDelimiter(", ");
		while (tokens.hasNext()) {
			text = tokens.next();
			if (!cities.contains(text))
				cities.add(text);
		}
		tokens.close();
		tokens = new Scanner(lines.nextLine());
		tokens.useDelimiter(", ");
		while (tokens.hasNext()) {
			text = tokens.next();
			if (!searchTerms.contains(text))
				searchTerms.add(text);
		}
		tokens.close();
		tokens = new Scanner(lines.nextLine());
		tokens.useDelimiter(", ");
		while (tokens.hasNext()) {
			text = tokens.next();
			if (!negativeKeywords.contains(text))
				negativeKeywords.add(text);
		}
		tokens.close();
		frequency = Double.parseDouble(lines.nextLine()) - 0.5;
		lines.close();
		if (!st.equals(searchTerms) || !c.equals(cities) || !nk.equals(negativeKeywords))
			settingsChanged = true;
	}

	public static void createSystemTrayIcon() {
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		TrayIcon trayIcon = null;
		final PopupMenu popup = new PopupMenu();
		final SystemTray tray = SystemTray.getSystemTray();
		trayIcon = new TrayIcon(f.getToolkit().getImage(CraigslistNotifier.class.getResource("/trayIcon.png")), "Nick's Notifier");
		MenuItem settingsItem = new MenuItem("Settings");
		MenuItem exitItem = new MenuItem("Exit");
		popup.add(settingsItem);
		popup.add(exitItem);
		trayIcon.setImageAutoSize(true);
		trayIcon.setPopupMenu(popup);
		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added");
		}
		settingsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				f.setVisible(true);
			}
		});
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteOldAds();
				new File("savedAds.txt").delete();
				saveAds();
				System.exit(0);
			}
		});
	}

	public static void updateAds(String city, String term) {
		String html = "";
		newAds.clear();
		try {
			html = Scraper.getHtml("http://" + city + ".craigslist.org/search/sss?sort=date&query=" + term.replace(" ", "%20"));
		} catch (IOException e) {
			sendEmail("IP blocked", "Either your internet connection is offline, or your computer's ip address has been automatically blocked by Craigslist. It should be unblocked within ~24 hours. This application has exited.");
			System.out.print("ip blocked");
			System.exit(0);
		}
		loop:
			for (Ad temp : createAds(html)) {
				if (ads.isEmpty()) {
					ads.add(temp);
					newAds.add(temp);
					System.out.println("add: " + temp);
					continue loop;
				}
				for (String word : negativeKeywords)
					if (temp.getTitle().toLowerCase().contains(word.toLowerCase())) {
						System.out.println("neg: " + temp);
						continue loop;
					}
				for (Ad ad : ads)
					if (temp.equals(ad)) {
						System.out.println("dup: " + temp);
						continue loop;
					}
				ads.add(temp);
				newAds.add(temp);
				System.out.println("add: " + temp);
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
		String title, date, location, link, city;
		String[] splitHtml;
		int price;
		city = str.substring(str.indexOf("<option value=\"") + 15, str.indexOf("</option>"));
		city = city.substring(0, city.indexOf("\">"));
		if (str.contains("<h4"))
			splitHtml =  str.substring(str.indexOf("<p"), str.indexOf("<h4")).split("</p>");
		else splitHtml = str.substring(str.indexOf("<p"), str.indexOf("<div id=\"mapcontainer")).split("</p>");
		for (String adHtml : splitHtml) {
			title = adHtml.substring(adHtml.indexOf("hdrlnk\">") + 8, adHtml.indexOf("</a> </span>")).replace("&amp;", "&");
			date = adHtml.substring(adHtml.indexOf("title=\"") + 7, adHtml.indexOf("title=\"") + 29);
			if (adHtml.contains("<small>"))
				location = adHtml.substring(adHtml.indexOf("<small>") + 9, adHtml.indexOf("</small>") - 1);
			else location = "n/a";
			link = "https://" + city + ".craigslist.org" + adHtml.substring(adHtml.indexOf("href=\"") + 6, adHtml.indexOf("html\"") + 4);
			if (adHtml.contains("price"))
				price = Integer.parseInt(adHtml.substring(adHtml.indexOf("price\">") + 8, adHtml.indexOf("</span")));
			else price = 0;
			temp = new Ad(title, price, date, location, link);
			result.add(temp);
		}
		return result;
	}

	//deletes the ad if it's date is 46 days older than the current date
	public static void deleteOldAds() {
		long cutoff = new Date().getTime() - 3974400000L;
		for (int i = 0; i < ads.size(); i++)
			if (ads.get(i).getDate().getTime() <  cutoff) {
				ads.remove(i);
				i--;
			}
	}

	public static void loadAds() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("savedAds.txt"));
			boolean end = false;
			while (!end) {
				try {
					ads.add((Ad)ois.readObject());
				} catch (IOException e) {
					end = true;
				} catch (ClassNotFoundException e) {
					System.out.println("ClassNotFoundException");
				}
			}
			ois.close();
		} catch (IOException e) {
			System.out.println("IOException while loading ads");
		}
	}

	public static void saveAds() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("savedAds.txt"));
			for (Ad ad : ads)
				oos.writeObject(ad);
			oos.close();
		} catch (IOException e) {
			System.out.println("IOException while saving");
		}
	}

	public static void sendEmail(String subject, String body) {
		try {
			GoogleMail.Send(email, password, recipient, subject, body);
		} catch (AddressException e) {
		} catch (MessagingException e) {
		}
	}
}