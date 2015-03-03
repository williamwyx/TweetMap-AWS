package awstm;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * <p>
 * This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TweetGrasper {
	/**
	 * Main entry of this application.
	 * 
	 * @param args
	 */
	private static SQLManager sqlmng = null;

	public static void main(String[] args) throws TwitterException {
		sqlmng = new SQLManager();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey("Db2Ieq9ApygcLhipSPU0qrM54")
				.setOAuthConsumerSecret(
						"W0IB2nSu2qyyVS28OV70k0Yjjblb7kBi4gQW0pdke2tEojRNDA")
				.setOAuthAccessToken(
						"2196936672-IptBHpc4Wc7Jng6IirdJ2QZdAE1JODLnV3tLhqI")
				.setOAuthAccessTokenSecret(
						"1yCDO84WLeGzF5CK1mhmRK5xrMhf7Wucs3bkD6I13fySf");

		TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
				.getInstance();
		StatusListener listener = new StatusListener() {
			int count = 0;

			@Override
			public void onStatus(Status status) {
				if (status.getGeoLocation() != null) {
					count++;
					System.out.println(count + " @"
							+ status.getUser().getScreenName() + " - "
							+ status.getText());
					System.out.println(status.getGeoLocation().getLatitude()
							+ ", " + status.getGeoLocation().getLongitude());
					sqlmng.storeTweet(status);
				}
			}

			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
				// System.out.println("Got a status deletion notice id:" +
				// statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				// System.out.println("Got track limitation notice:" +
				// numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// System.out.println("Got scrub_geo event userId:" + userId+
				// " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				// System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
		twitterStream.addListener(listener);
		FilterQuery fq = new FilterQuery();
        fq.track(sqlmng.getKeywordList());
		
		sqlmng.getConnection();
		twitterStream.filter(fq);
		/*try {
			Thread.currentThread().sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		twitterStream.cleanUp();
		System.out.println("Stopped.");*/
	}
}