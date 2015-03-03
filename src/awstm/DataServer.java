package awstm;

import java.io.IOException;
import java.util.ArrayList;

import javax.json.JsonObject;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

@ServerEndpoint("/dataserver")
public class DataServer {
	@OnOpen
	public void onOpen(Session session) {
		SQLManager sqlmng = new SQLManager();
    	sqlmng.getConnection();
        try {
            if (session.isOpen()) {
                ArrayList<JSONObject> tweets = sqlmng.getTweets(null, 0);
				for (int i = 0; i < tweets.size(); i++) {
					session.getBasicRemote().sendText(tweets.get(i).toString());
				}
            }
        } catch (IOException e) {
            try {
                session.close();
            } catch (IOException e1) {
                // Ignore
            }
        } finally {
        	sqlmng.closeConnection();
        }
	}

    @OnMessage
    public void onMessage(String msg, Session session) {
    	// The msg contains millisecond, we need second
    	int updateInt = Integer.parseInt(msg) / 1000;
    	SQLManager sqlmng = new SQLManager();
    	sqlmng.getConnection();
        try {
            if (session.isOpen()) {
                ArrayList<JSONObject> tweets = sqlmng.getTweets(null, updateInt);
				for (int i = 0; i < tweets.size(); i++) {
					session.getBasicRemote().sendText(tweets.get(i).toString());
				}	
            }
        } catch (IOException e) {
            try {
                session.close();
            } catch (IOException e1) {
                // Ignore
            }
        } finally {
        	sqlmng.closeConnection();
        }
    }
}