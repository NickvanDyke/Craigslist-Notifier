import java.text.ParseException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ad implements Serializable {
	private String title, location, link, body;
	private int price;
	private Date date;
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

	public Ad(String title, int price, String date, String location, String link, String city, String body) {
		this.title = title;
		this.price = price;
		try {
			this.date = df.parse(date);
		}
		catch (ParseException e) {
			System.out.println("ParseException");
		}
		this.location = location;
		this.link = link;
		this.body = body;
	}

	public String toString() {
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
	
	public String getBody() {
		return body;
	}

	public int getPrice() {
		return price;
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
