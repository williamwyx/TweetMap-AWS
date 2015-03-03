package awstm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.json.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Status;

public class SQLManager {
	private ArrayList<String[]> keywordList = new ArrayList<String[]>();
	private String[] sportList = { "soccer", "FIFA", "World Cup", "basketball",
			"NBA", "F1", "football", "NEL", "volleyball" };
	private String[] musicList = { "music", "song", "singer", "album",
			"pop star", "rock star" };
	private String[] magicList = { "Magic", "Magician", "card trick",
			"coin trick", "mentalism", "mentalist", "Derren Brown" };
	private String[] techList = { "Apple", "Google", "Android", "IOS", "ipad",
			"iphone", "mac", "amazon", "samsung", "vmware", "html", "java",
			"c++", "javascript", "code", "technology" };
	private static Connection conn;

	public SQLManager() {
		keywordList.add(sportList);
		keywordList.add(musicList);
		keywordList.add(magicList);
		keywordList.add(techList);
	}

	public String[] getKeywordList() {
		return concatAll(keywordList);
	}

	private String[] concatAll(ArrayList<String[]> keywordList) {
		int totalLength = 0;
		for (String[] array : keywordList) {
			totalLength += array.length;
		}
		String[] result = new String[totalLength];
		int offset = 0;
		for (String[] array : keywordList) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	/*
	 * Get tweets from database according to keywords and time range(with in how
	 * many minutes)
	 */
	public ArrayList<JSONObject> getTweets(String[] keywords, int recent) {
		PreparedStatement ps = null;
		String sql = "SELECT * FROM tweets WHERE created_time > ?";
		// String sql = "SELECT latitude, longitude FROM tweets";
		ResultSet rs = null;
		ArrayList<JSONObject> tweets = new ArrayList<JSONObject>();
		try {
			Calendar currentTime = Calendar.getInstance();
			currentTime.add(currentTime.SECOND, -recent);
			ps = conn.prepareStatement(sql);
			java.sql.Timestamp queryTime = new java.sql.Timestamp(
					currentTime.getTimeInMillis());
			ps.setTimestamp(1, queryTime);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.getString("status_kw") == null)
					continue;
				JSONObject tweet = new JSONObject();
				try {
					tweet.put("created_time", rs.getTimestamp("created_time").getTime());
					tweet.put("keyword", rs.getString("status_kw"));
					tweet.put("latitude", rs.getDouble("latitude"));
					tweet.put("longitude", rs.getDouble("longitude"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tweets.add(tweet);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return tweets;
	}

	/*
	 * Store a new tweet in the database. Arguments: status_id, user_id,
	 * user_screen_name, status_text, status_kw, created_time, latitude,
	 * longitude
	 */
	public int storeTweet(Status status) {
		int ret = 0;
		PreparedStatement ps = null;
		String sql = "INSERT INTO tweets VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, status.getId());
			ps.setLong(2, status.getUser().getId());
			ps.setString(3, status.getUser().getScreenName());
			ps.setString(4, status.getText());
			String keyword = getKeyword(status);
			ps.setString(5, keyword);
			java.sql.Timestamp createTime = new java.sql.Timestamp(status
					.getCreatedAt().getTime());
			ps.setTimestamp(6, createTime);
			ps.setDouble(7, status.getGeoLocation().getLatitude());
			ps.setDouble(8, status.getGeoLocation().getLongitude());
			ret = ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return ret;
	}

	private String getKeyword(Status status) {
		for (String keyword : magicList) {
			if (status.getText().toLowerCase().contains(keyword.toLowerCase())) {
				return "magic";
			}
		}
		for (String keyword : sportList) {
			if (status.getText().toLowerCase().contains(keyword.toLowerCase())) {
				return "sport";
			}
		}
		for (String keyword : techList) {
			if (status.getText().toLowerCase().contains(keyword.toLowerCase())) {
				return "tech";
			}
		}
		for (String keyword : musicList) {
			if (status.getText().toLowerCase().contains(keyword.toLowerCase())) {
				return "music";
			}
		}
		return null;
	}

	public void getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			conn = DriverManager.getConnection(
					"jdbc:mysql://127.0.0.1/tweetmap", "root", "6998");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
