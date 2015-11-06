# Notifier
Given search terms, cities, and a refresh frequency, this automatically checks Craigslist for new postings, and emails the given recipient a notification using the given Gmail address when new postings are found.
Use by downloading the zip, extracting NicksNotifier.jar and icon.png to the same directory, running the jar, and following the instructions. The settings file is not very foolproof yet, so be careful to enter your settings as it says.

Why use this over Craigslist's built-in email alerts or an RSS feed?
* Those often have very long delays between when a new ad is posted and when you are notified; I personally have received email notifications about ads that I had seen 6 hours earlier while manually searching. My program, on the other hand, will only experience a maximum delay of whatever refresh interval you set plus half a minute (the plus half a minute is because of the randomization of refreshing, and is an absolute worst-case scenario), with an average delay of the refresh interval you set divided by two. Note that Craigslist claims it can take up to 15 minutes for an ad to appear in search results after it has been posted. However, since every user will experience this delay, you're at no disadvantage.
* CL's email alerts email you multiple times for the same ad if you have multiple search terms that return that particular ad, often resulting in needless email spam (I have experienced a LOT of this). My program avoids this, and only emails you regarding the same ad if it is updated or has a price change.
* My program has a negative keyword feature to filter out ads you don't want; for example, a search for "cpu" will likely return a lot of laptops, which you might not care about; so, you would enter the word laptop as a negative keyword. This feature isn't 100% foolproof, as it uses the title of the ad to determine whether it should be filtered out or not, and the poster might not include the word laptop in the title, for example. Filtering based on the item description wouldn't work well, because the poster might write, "I'm selling this desktop cpu because I have decided to exclusively use a laptop," which would cause the ad to be filtered, even though its of something you wanted.
