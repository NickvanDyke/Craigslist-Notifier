import java.text.ParseException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ad implements Serializable {
	private String title, location, link;
	private int price, oldPrice;
	private Date date;
	private boolean priceChange;
	private static DateFormat df = new SimpleDateFormat("EEE dd MMM hh:mm:ss a");

	public Ad(String title, int price, String date, String location, String link, String city) {
		this.title = title;
		this.price = price;
		priceChange = false;
		try {
			this.date = df.parse(date);
		}
		catch (ParseException e) {
			System.out.println("ParseException");
		}
		this.location = location;
		this.link = "https://" + city + ".craigslist.org" + link;
	}

	public String toString() {
		if (priceChange)
			return title + " price change from " + oldPrice + " to " + price + ", updated at " + date.toString() + " " + link;
		return title + " for $" + price + " in " + location + " posted at " + date.toString() + " " + link;
	}

	public boolean equals(Ad o) {
		return title.equals(o.getTitle()) && price == o.getPrice() && location.equals(o.getLocation()) && link.equals(o.getLink()) && date.equals(o.getDate());
	}

	public int compareTo(Ad o) {
		//returns 3 if all fields are the same; they are the exact same ad
		if (this.equals(o))
			return 3;
		//returns 2 if they are duplicate postings; everything but link and date is the same
		if (title.equals(o.getTitle()) && price == o.getPrice() && location.equals(o.getLocation()) && !link.equals(o.getLink()))
			return 2;
		//returns 1 if everything but price and date is the same; price change
		if (title.equals(o.getTitle()) && price != o.getPrice() && location.equals(o.getLocation()) && link.equals(o.getLink()))
			return 1;
		//returns 0 if title, price, and link are different; they are completely different ads
		if (!title.equals(o.getTitle()) && price != o.getPrice() && !link.equals(o.getLink()))
			return 0;
		return -1;
	}

	public String getTitle() {
		return title;
	}

	public int getPrice() {
		return price;
	}
	
	public int getOldPrice() {
		return oldPrice;
	}
	
	public void setOldPrice(int n) {
		priceChange = true;
		oldPrice = n;
	}

	public Date getDate() {
		return date;
	}

	public String getLocation() {
		return location;
	}

	public String getLink() {
		return link;
	}
}
