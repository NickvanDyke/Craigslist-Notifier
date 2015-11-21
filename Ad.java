import java.text.ParseException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ad implements Serializable {
	private String title, location, link;
	private int price;
	private Date date;
	private static DateFormat df = new SimpleDateFormat("EEE dd MMM hh:mm:ss a");

	public Ad(String title, int price, String date, String location, String link) {
		this.title = title;
		this.price = price;
		try {
			this.date = df.parse(date);
			this.date.setYear(new Date().getYear());
		} catch (ParseException e) {
			System.out.println("ParseException");
		}
		this.location = location;
		this.link = link;
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
