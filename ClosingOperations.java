import java.io.IOException;

public final class ClosingOperations extends Thread {
	public void run() {
		try {
			Craigslist.saveAds();
		}
		catch (IOException e) {
			System.out.println("IOException while saving");
		}
	}
}
