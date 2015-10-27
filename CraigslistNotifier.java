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
	private static int frequency;

	public static void main(String[] args) {
		try {
			Craigslist.loadSettings();
			Craigslist.loadAds();
		}
		catch (FileNotFoundException e) {
			try {
				String n = System.getProperty("line.separator");
				FileUtils.writeStringToFile(new File("settings.txt"), "Gmail account to send emails from:" + n + "address: " + n + "password: " + n + "You must enable less secure apps to access your account; do so here:" + n + "https://www.google.com/settings/security/lesssecureapps" + n + n + "recipient's email: " + n + n + "cities to search; ensure the city actually has it's own craigslist (i.e. <city>.craigslist.org is a valid url)" + n + "put each entry on its own line:" + n + n + n + "search terms (put each entry on its own line):" + n + n + n + "how often to check listings, in minutes: ");
			}
			catch (IOException i) {
				System.out.println("IOException while creating settings.txt");
			}
			JOptionPane.showMessageDialog(null, "This is your first time running the app.\nPlease fill out the settings.txt file that has been\ncreated in the directory that this app resides in.\nThen launch the app again.");
			System.exit(0);
		}
		catch (IOException e) {
			System.out.println("IOException while loading");
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
				sendEmail(ad.getTitle(), "$" + ad.getPrice() + " in " + ad.getLocation() + "\n" + ad.getLink());
			try {
				Thread.sleep(frequency);
			}
			catch (InterruptedException e) {
				System.out.println("InterruptedException");
			}
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

	public static void setFrequency(int n) {
		frequency = n * 60000;
	}
}