package cp;

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
	
	// Used in: /v2/users [POST], /v2/users/{user} [GET]
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
	
	// Used in: /v2/users/{user} [GET]
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
	
	// Used in: /v2/users/{user} [GET]
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
	
	public static String getUrlFromDB(int urlid) {
		return getStringFromDB("userurls", "url", "WHERE '"+urlid+"' = userurls.id");
	}

	public static String[] getCategoriesFromDB(int urlid) {
		return getStringsFromDB("categories", "category", "WHERE '"+urlid+"' = userurls.id");
	}

	public static String[] getCommentsFromDB(int urlid) {
		return getStringsFromDB("comments", "comment", "WHERE '"+urlid+"' = userurls.id");
	}
	
	public static String getUserNodeFromDB(String username) {
		return getStringFromDB("userlist", "node", "");
	}
	
	public static String getStringFromDB(String table, String column, String condition) {
		String output = "";
		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM "+table+" "+condition);
				if (rs.next()) {
					output = rs.getString(column);
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
}