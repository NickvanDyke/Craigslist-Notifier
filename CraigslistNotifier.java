import java.io.IOException;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import java.io.*;

public final class CraigslistNotifier {
	private static String email;
	private static String password;
	private static String recipient;

	public static void main(String[] args) {
		try {
			Craigslist.loadSettings();
		}
		catch (FileNotFoundException e) {
			try {
				String n = System.getProperty("line.separator");
				FileUtils.writeStringToFile(new File("settings.txt"), "Gmail account to send emails from:" + n + "email address: " + n + "password: " + n + "You must enable less secure apps to access your account; do so here:" + n + "https://www.google.com/settings/security/lesssecureapps" + n + n + "recipient's email address: " + n + n + "cities to search; ensure the city actually has its own craigslist (i.e. <city>.craigslist.org is a valid url)" + n + "put each entry on its own line:" + n + n + n + "search terms; put each entry on its own line:" + n + n + n + "negative keywords; if the ad's title contains any of the given keywords, you will not receive an email notification for that ad" + n + "put each entry on its own line:" + n + n + n + "how often to check listings, in minutes: ");
			}
			catch (IOException i) {
				System.out.println("IOException while loading settings.txt");
			}
			JOptionPane.showMessageDialog(null, "This is your first time running the app.\nPlease fill out the settings.txt file that has been\ncreated in the directory that this app resides in.\nThen launch the app again.");
			System.exit(0);
		}
		try {
			Craigslist.loadAds();
		}
		catch (IOException e) {
			System.out.println("IOException while loading ads");
		}
		while (true) {
			Craigslist.updateAds();
			try {
				Craigslist.saveAds();
			}
			catch (IOException e) {
				System.out.println("IOException while saving");
			}
			for (Ad ad : Craigslist.getNewAds())
				sendEmail(ad.getTitle(), "$" + ad.getPrice() + " - " + ad.getBody() + "\n" + ad.getLink());
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

	public static void createSettings() {

	}

	public static void setEmail(String str) {
		email = str;
	}

	public static void setPassword(String str) {
		password = str;
	}

	public static void setRecipient(String str) {
		recipient = str;
	}
}