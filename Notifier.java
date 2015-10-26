import java.io.IOException;
import javax.mail.*;
import javax.mail.internet.*;

public class Notifier {
	public static void main(String[] args) {
		try {
			Craigslist.loadSearchTerms();
			Craigslist.loadCities();
			Craigslist.loadAds();
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
				Thread.sleep(300000);
			}
			catch (InterruptedException e) {
				System.out.println("InterruptedException");
			}
		}
	}

	public static void sendEmail(String subject, String body) {
		try {
			GoogleMail.Send("NotifierProgram", "notifier123", "notifierprogram@gmail.com", subject, body);
		}
		catch (AddressException e) {
		}
		catch (MessagingException e) {
		}
	}
}