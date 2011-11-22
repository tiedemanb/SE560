package cp;

import java.io.PrintWriter;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;

public class DBHelper
{
	public static Connection getConnection() {
		Connection connection = null;
		try {
		    String driverName = "com.mysql.jdbc.Driver";
		    Class.forName(driverName);

		    String serverName = "se560.cvdmxatvytbj.us-east-1.rds.amazonaws.com:3306";
		    String mydatabase = "se560";
		    String url = "jdbc:mysql://" + serverName +  "/" + mydatabase;
		    String username = "btiedema";
		    String password = "18273645"; // <- throwaway password, of course
		    connection = DriverManager.getConnection(url, username, password);
		}
		catch (ClassNotFoundException e) {
			return null;
		}
		catch (SQLException e) {
			return null;
		}
		return connection;
	}
	
	// Used in: /v1/lookupurls [GET]
	public static String[] getUrlsFromDB() {
		return getStringsFromDB("urls", "url", "");
	}
	
	// Used in: /v1/registerurls [POST]
	public static String[] registerNonExistingUrls(String[] urls) {
		String[] output = new String[0];
		String[] temp;
		
		Connection con = getConnection();
		if (con != null) {
			try {
				for (int i = 0; i < urls.length; i++) {
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT * FROM urls WHERE url = '"+urls[i]+"'");
					if (rs.next()) {
						
					}
					else {
						stmt.executeUpdate("INSERT INTO urls (url) VALUES ('"+urls[i]+"')");
						temp = new String[output.length + 1];
						System.arraycopy(output, 0, temp, 0, output.length);
						temp[output.length] = urls[i];
						output = temp;
					}
					rs.close();
					stmt.close();
				}
				con.close();
			}
			catch (SQLException e) {
				
			}
		}
		
		return output;
	}

	// Used in: /v2/users [GET], /v2/users/{user} [GET]
	public static String[][] getUsersFromDB() {
		String[][] output = new String[0][3];
		String[][] temp;

		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM userlist");
				while (rs.next()) {
					String id = Integer.toString(rs.getInt("id"));
					String email = rs.getString("username");
					String node = rs.getString("node");
					temp = new String[output.length + 1][3];
					System.arraycopy(output, 0, temp, 0, output.length);
					temp[output.length][0] = id;
					temp[output.length][1] = email;
					temp[output.length][2] = node;
					output = temp;
				}
				rs.close();
				stmt.close();
				con.close();
			}
			catch (SQLException e) {
				return output;
			}
		}
		return output;
	}
	
	// Used in: /v2/users [POST], /v2/users/{user} [GET], /v2/users/{user}/urls [GET]
	public static boolean getUserExistenceFromDB(String username) {
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM userlist WHERE username = '"+username+"'");
				while (rs.next()) {
					return true;
				}
			}
			catch (SQLException e) {
				
			}
		}
		return false;
	}

	// Used in: /v2/users [POST]
	public static boolean createUser(String email, String node) {
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				stmt.executeUpdate("INSERT INTO userlist (username, node) VALUES ('"+email+"', '"+node+"')");
				stmt.close();
				con.close();
				return true;
			}
			catch (Exception e) {
				
			}
		}
		return false;
	}
	
	// Used in: /v2/users/{user} [GET], /v2/users/{user}/urls [GET], /v2/users/{user}/urls [POST], /v2/users/{user}/urls/{urlid} [GET]
	public static String[] getUserFromDB(String username) {
		String[] output = new String[3];
		
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM userlist WHERE username = '"+username+"'");
				while (rs.next()) {
					String id = Integer.toString(rs.getInt("id"));
					String email = rs.getString("username");
					String node = rs.getString("node");
					output[0] = id;
					output[1] = email;
					output[2] = node;
				}
			}
			catch (SQLException e) {
				
			}
		}
		return output;
	}
	
	// Used in: /v2/users/{user}/urls [GET]
	public static String[][] getUserUrlsFromDB(String id) {
		String[][] output = new String[0][3];
		String[][] temp;

		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM userurls WHERE "+Integer.parseInt(id)+" = userurls.userid");
				while (rs.next()) {
					String urlid = Integer.toString(rs.getInt("id"));
					String userid = Integer.toString(rs.getInt("userid"));
					String url = rs.getString("url");
					temp = new String[output.length + 1][3];
					System.arraycopy(output, 0, temp, 0, output.length);
					temp[output.length][0] = urlid;
					temp[output.length][1] = userid;
					temp[output.length][2] = url;
					output = temp;
				}
				rs.close();
				stmt.close();
				con.close();
			}
			catch (SQLException e) {
				return output;
			}
		}
		return output;
	}
	
	// Used in: /v2/users/{user} [GET]
	/*public static String[] getUserNotificationsFromDB(String id) {
		String[][] output = new String[0][3];
		String[][] temp;

		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM notifications WHERE "+Integer.parseInt(id)+" = notifications.userid");
				while (rs.next()) {
					String noteid = Integer.toString(rs.getInt("id"));
					String userid = Integer.toString(rs.getInt("userid"));
					String notification = rs.getString("notification");
					temp = new String[output.length + 1][3];
					System.arraycopy(output, 0, temp, 0, output.length);
					temp[output.length][0] = noteid;
					temp[output.length][1] = userid;
					temp[output.length][2] = notification;
					output = temp;
				}
				rs.close();
				stmt.close();
				con.close();
			}
			catch (SQLException e) {
				return output;
			}
		}
		return output;
	}*/
	
	// Used in: /v2/users/{user}/urls [POST]
	/*
	 * This method is a little complex. It returns a list of categories' ids.
	 * If a category is not already in the database, it first adds it.
	 */
	public static int[] getCategoryIDs(String[] categories) {
		int[] output = new int[categories.length];
		
		for (int i = 0; i < categories.length; i++) {
			int id = getCategoryID(categories[i]);
			output[i] = id;
		}
		
		return output;
	}
	
	// Used in: /v2/users/{user}/urls [POST]
	/*
	 * Gets the id of a category.
	 * Adds the category to the system first if it doesn't exist yet.
	 * Returns -1 if it couldn't add the category for some reason.
	 */
	public static int getCategoryID(String category) {
		int output = -1;
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM categorylist WHERE category = '"+category+"'");
				if (rs.next()) {
					output = rs.getInt("id");
				}
				else {
					int count = addCategory(category);
					if (count == 1) {
						output = getCategoryID(category);
					}
				}
				rs.close();
				stmt.close();
				con.close();
			}
			catch (Exception e) {

			}
		}
		return output;
	}
	
	// Used in: /v2/users/{user}/urls [POST]
	/*
	 * Adds a category to the category table.
	 */
	private static int addCategory(String category) {
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				int count = stmt.executeUpdate("INSERT INTO categorylist (category) VALUES ('"+category+"')");
				stmt.close();
				con.close();
				return count;
			}
			catch (Exception e) {

			}
		}
		
		return -1;
	}
	
	// Used in: /v2/users/{user}/urls [POST]
	private static void addCategoryReference(int urlid, int catid) {
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				stmt.executeUpdate("INSERT INTO categories (urlid, catid) VALUES ("+urlid+", "+catid+")");
				stmt.close();
				con.close();
			}
			catch (Exception e) {

			}
		}
	}
	
	// Used in: /v2/users/{user}/urls [POST]
	private static void addComment(int urlid, String comment) {
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				stmt.executeUpdate("INSERT INTO comments (urlid, comment) VALUES ("+urlid+", '"+comment+"')");
				stmt.close();
				con.close();
			}
			catch (Exception e) {

			}
		}
	}
	
	// Used in: /v2/users/{user}/urls [POST]
	/*
	 * Controller for adding a user url resource.
	 */
	public static boolean addUrl(int userid, String url, String[] categories, String[] comments) {
		String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'h:m:ssZ").format(new Date());
		int urlid = getNewUrlIdFromDB(userid, url, timestamp);
		if (urlid != -1) {
			int[] categoryIds = getCategoryIDs(categories);
			for (int i = 0; i < categories.length; i++) {
				if (categoryIds[i] != -1) {
					addCategoryReference(urlid, categoryIds[i]);
				}
			}
			for (int i = 0; i < comments.length; i++) {
				addComment(urlid, comments[i]);
			}
			return true;
		}

		return false;
	}
	
	// Used in: /v2/users/{user}/urls [POST]
	/*
	 * If the url is not yet created, create it and return the ID.
	 * Otherwise, return -1.
	 */
	private static int getNewUrlIdFromDB(int userid, String url, String timestamp) {
		if (getUrlIdFromDB(userid, url) == -1) {
			if (createUrl(userid, url, timestamp) == 1) {
				return getUrlIdFromDB(userid, url);
			}
		}

		return -1;
	}
	
	// Used in: /v2/users/{user}/urls [POST]
	public static int getUrlIdFromDB(int userid, String url) {
		int output = -1;
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM userurls WHERE userid = "+userid+" AND url = '"+url+"'");
				if (rs.next()) {
					output = rs.getInt("id");
				}
				rs.close();
				stmt.close();
				con.close();				
			}
			catch (SQLException e) {
				
			}
		}
		return output;
	}
	
	// Used in: /v2/users/{user}/urls/{urlid} [GET]
	public static int getUrlIdFromDBUsingID(int userid, int urlid) {
		int output = -1;
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				// Still checking by userid here to, so you can't see one bookmark, even though it doesn't belong to that user.
				ResultSet rs = stmt.executeQuery("SELECT * FROM userurls WHERE userid = "+userid+" AND id = '"+urlid+"'");
				if (rs.next()) {
					output = rs.getInt("id");
				}
				rs.close();
				stmt.close();
				con.close();				
			}
			catch (SQLException e) {
				
			}
		}
		return output;
	}
	
	// Used in: /v2/users/{user}/urls [POST]
	private static int createUrl(int userid, String url, String timestamp) {
		int count = 0;
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				count = stmt.executeUpdate("INSERT INTO userurls (userid, url, timestamp) VALUES ("+userid+", '"+url+"', '"+timestamp+"')");
				stmt.close();
				con.close();
			}
			catch (SQLException e) {
				
			}
		}
		
		return count;
	}
	
	
	// Used in: /v2/users/{user}/urls/{urlid} [GET]
	public static String[] getUrlDataFromDB(int urlid) {
		String[] output = new String[4];
		
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM userurls WHERE id = "+urlid);
				while (rs.next()) {
					String id = Integer.toString(rs.getInt("id"));
					String userid = Integer.toString(rs.getInt("userid"));
					String url = rs.getString("url");
					String timestamp = rs.getString("timestamp");
					output[0] = id;
					output[1] = userid;
					output[2] = url;
					output[3] = timestamp;
				}
			}
			catch (SQLException e) {
				
			}
		}
		return output;
	}
	
	// Used in: ???
	public static String[] getCommentsFromDB(int urlid) {
		String[] output = new String[0];
		String[] temp;
		
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM comments WHERE urlid = "+urlid);
				while (rs.next()) {
					temp = new String[output.length+1];
					System.arraycopy(output, 0, temp, 0, output.length);
					temp[output.length] = rs.getString("comment");
					output = temp;
				}
			}
			catch (SQLException e) {
				
			}
		}
		return output;
	}

	// Used in: /v2/users/{user}/urls/{urlid} [GET]
	public static String[] getCategoriesFromDB(int urlid) {
		String[] output = new String[0];
		String[] temp;
		
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT category FROM categories, categorylist WHERE categorylist.urlid = "+urlid+" AND category.id = categorylist.catid");
				while (rs.next()) {
					temp = new String[output.length+1];
					System.arraycopy(output, 0, temp, 0, output.length);
					temp[output.length] = rs.getString("category");
					output = temp;
				}
			}
			catch (SQLException e) {
				
			}
		}
		return output;
	}

	public static String[] getStringsFromDB(String table, String column, String condition) {
		String[] output = new String[0];
		String[] temp;

		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM "+table+" "+condition);
				while (rs.next()) {
					String url = rs.getString(column);
					temp = new String[output.length + 1];
					System.arraycopy(output, 0, temp, 0, output.length);
					temp[output.length] = url;
					output = temp;
				}
				rs.close();
				con.close();
			}
			catch (Exception e) {
				return new String[0];
			}
		}
		return output;
	}
	
	public static boolean setupDB() {
		Connection con = DBHelper.getConnection();
    	if (con != null) {
        	try {
        		Statement stmt = con.createStatement();

        		// Remove existing tables.
        		String sql = "DROP TABLE IF EXISTS categories";
        		stmt.execute(sql);
        		sql = "DROP TABLE IF EXISTS categorylist";
	    		stmt.execute(sql);
	    		sql = "DROP TABLE IF EXISTS comments";
				stmt.execute(sql);
				sql = "DROP TABLE IF EXISTS userurls";
				stmt.execute(sql);
				sql = "DROP TABLE IF EXISTS userlist";
				stmt.execute(sql);
        		sql = "DROP TABLE IF EXISTS urls";
        		stmt.execute(sql);
        		
        		// Create new tableset.
        		sql = "CREATE TABLE urls " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, url VARCHAR(500))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE userlist " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100), node VARCHAR(500))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE userurls " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, userid INT, url VARCHAR(500), timestamp VARCHAR(100), " + 
        			"FOREIGN KEY(userid) REFERENCES userlist(id))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE comments " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, urlid INT, comment VARCHAR(500), " + 
        			"FOREIGN KEY(urlid) REFERENCES userurls(id))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE categorylist " +
    				"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, category VARCHAR(100))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE categories " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, urlid INT, catid INT, " + 
        			"FOREIGN KEY(urlid) REFERENCES userurls(id), FOREIGN KEY(catid) REFERENCES categorylist(id))";
        		stmt.executeUpdate(sql);
        		stmt.close();
        		con.close();
        		return true;
        	}
        	catch (Exception e) {
        		return false;
        	}
    	}
    	
    	return false;
	}
	
	public static boolean resetDB() {
		Connection con = DBHelper.getConnection();
    	if (con != null) {
        	try {
        		Statement stmt = con.createStatement();

        		// Create new tableset.
        		String sql = "CREATE TABLE IF NOT EXISTS urls " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, url VARCHAR(500))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE IF NOT EXISTS userlist " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100), node VARCHAR(500))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE IF NOT EXISTS userurls " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, userid INT, url VARCHAR(500), timestamp VARCHAR(100), " + 
        			"FOREIGN KEY(userid) REFERENCES userlist(id))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE IF NOT EXISTS comments " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, urlid INT, comment VARCHAR(500), " + 
        			"FOREIGN KEY(urlid) REFERENCES userurls(id))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE IF NOT EXISTS categorylist " +
    				"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, category VARCHAR(100))";
        		stmt.executeUpdate(sql);
        		sql = "CREATE TABLE IF NOT EXISTS categories " +
        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, urlid INT, catid INT, " + 
        			"FOREIGN KEY(urlid) REFERENCES userurls(id), FOREIGN KEY(catid) REFERENCES categorylist(id))";
        		stmt.executeUpdate(sql);
        		
        		// Remove existing tables.
        		sql = "TRUNCATE TABLE categories";
        		stmt.executeUpdate(sql);
        		sql = "TRUNCATE TABLE categorylist";
	    		stmt.executeUpdate(sql);
	    		sql = "TRUNCATE TABLE comments";
				stmt.executeUpdate(sql);
				sql = "TRUNCATE TABLE userurls";
				stmt.executeUpdate(sql);
				sql = "TRUNCATE TABLE userlist";
				stmt.executeUpdate(sql);
        		sql = "TRUNCATE TABLE urls";
        		stmt.executeUpdate(sql);
        		
        		stmt.close();
        		con.close();
        		return true;
        	}
        	catch (Exception e) {
        		return false;
        	}
    	}
    	
    	return false;
	}
}