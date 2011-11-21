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
	
	public static String[] getUrlsFromDB() {
		return getStringsFromDB("urls", "url", "");
	}
	
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

	public static String[][] getUsersFromDB() {
		String[][] output = new String[0][2];
		String[][] temp;

		Connection con = getConnection();
		if (con != null) {
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM userlist");
				while (rs.next()) {
					String email = rs.getString("username");
					String node = rs.getString("node");
					temp = new String[output.length + 1][2];
					System.arraycopy(output, 0, temp, 0, output.length);
					temp[output.length][0] = email;
					temp[output.length][1] = node;
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

	public static String[] getUserUrlsFromDB(String username) {
		return getStringsFromDB("userurls", "url", "WHERE '"+username+"' = userlist.username AND userlist.id = userurls.userid");
	}
	
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