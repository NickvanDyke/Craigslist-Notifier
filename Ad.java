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

	public Ad(String title, int price, String date, String location, String link, String body) {
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
