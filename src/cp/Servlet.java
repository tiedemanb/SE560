package cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import java.sql.*;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Servlet extends HttpServlet
{
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException
	{
	    String uri = request.getRequestURI();
	    StringBuffer url = request.getRequestURL();
	    String servletPath = request.getServletPath();
	    String queryString = request.getQueryString();

	    DocumentBuilderFactory xmlFactory;
	    DocumentBuilder xml;

	    boolean xmlOnly = false;
	    String accept = request.getHeader("Accept");
	    if (accept.matches(".*application/xml.*") && (! accept.matches(".*application/xhtml.*"))) {
	    	xmlOnly = true;
	    }

	    try {
	    	xmlFactory = DocumentBuilderFactory.newInstance();
		    xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    }
	    catch (ParserConfigurationException e) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

            pw.println("error with parser");
            return;
	    }

	    if ((uri.matches("/v1/whoami")) || (uri.matches("/v2/whoami"))) {
	    	Document xmlDoc = xml.newDocument();
	    	Element base = xmlDoc.createElement("url");
	    	base.setAttribute("value", "http://somerandomurl.org");
	    	xmlDoc.appendChild(base);

            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

	    	try {
	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		    	StreamResult result = new StreamResult(new StringWriter());
		    	DOMSource source = new DOMSource(xmlDoc);
		    	transformer.transform(source, result);
		    	String xmlString = result.getWriter().toString();

		    	pw.println(xmlString);
		    	return;
	    	}
	    	catch (TransformerException e) {
	            response.setContentType("text/plain");
	    		pw.println("whoami failed");
	    		return;
	    	}
        }
	    else if (uri.matches("/v1/lookupurls")) {
	    	Document xmlDoc = xml.newDocument();
	    	Element base = xmlDoc.createElement("urls");

	    	String[] urls = {"http://webplaces.net/i_c_weiner", "https://www.wheresURLdo?.com"};
	    	for (int i = 0; i < urls.length; i++) {
	    		Element urlNode = xmlDoc.createElement("url");
	    		urlNode.setAttribute("value", urls[i]);
	    		base.appendChild(urlNode);
	    	}

	    	xmlDoc.appendChild(base);

            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

	    	try {
	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		    	StreamResult result = new StreamResult(new StringWriter());
		    	DOMSource source = new DOMSource(xmlDoc);
		    	transformer.transform(source, result);
		    	String xmlString = result.getWriter().toString();

		    	pw.println(xmlString);
		    	return;
	    	}
	    	catch (TransformerException e) {
	            response.setContentType("text/plain");
	    		pw.println("lookupurls failed");
	    		return;
	    	}
        }
	    else if (uri.matches("/v2/setup")) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

        	Connection con = DBHelper.getConnection();
        	if (con != null) {
	        	try {
	        		Statement stmt = con.createStatement();
	        		String sql = "CREATE TABLE IF NOT EXISTS urls " +
	        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, url VARCHAR(500))";
	        		stmt.executeUpdate(sql);
	        		sql = "CREATE TABLE IF NOT EXISTS userlist " +
	        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100), node VARCHAR(500))";
	        		stmt.executeUpdate(sql);
	        		sql = "CREATE TABLE IF NOT EXISTS userurls " +
	        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, userid INT, url VARCHAR(500), " + 
	        			"FOREIGN KEY(userid) REFERENCES userlist(id))";
	        		stmt.executeUpdate(sql);
	        		sql = "CREATE TABLE IF NOT EXISTS comments " +
	        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, urlid INT, comment VARCHAR(500), " + 
	        			"FOREIGN KEY(urlid) REFERENCES userurls(id))";
	        		stmt.executeUpdate(sql);
	        		sql = "CREATE TABLE IF NOT EXISTS categories " +
	        			"(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, urlid INT, category VARCHAR(500), " + 
	        			"FOREIGN KEY(urlid) REFERENCES userurls(id))";
	        		stmt.executeUpdate(sql);
	        		stmt.close();
	        		con.close();
	        		pw.println("setup successful");
	        		return;
	        	}
	        	catch (Exception e) {
	        		pw.println("setup failed");
	        		pw.println(e.getMessage());
	        		return;
	        	}
        	}
        	pw.println("DB could not be reached.");
        }
	    else if (uri.matches("/v2/users")) {
            PrintWriter pw = response.getWriter();
            String[][] users = DBHelper.getUsersFromDB();
            if (xmlOnly) {
            	//response.setContentType("application/xml");
            	pw.println("stub");
            }
            else {
            	response.setContentType("application/xhtml+xml");
            	pw.println("<ul class=\"users\">");
            	for (int i = 0; i < users.length; i++) {
            		pw.println("<li class=\"user\">" +
            			"<a href=\"/v2/users/"+users[i][1]+"\" rel=\"details\">"+users[i][0]+"</a>" +
            			"</li>");
            	}
            	pw.println("</ul>");
            }
	    }
	    else if (uri.matches("/v2/users/[^/]*")) {
			String user = uri.substring(("/v2/users/").length());
			String node = DBHelper.getUserNodeFromDB(user);
            PrintWriter pw = response.getWriter();
            if (xmlOnly) {
            	//response.setContentType("application/xml");
            	pw.println("stub");
            }
            else {
            	response.setContentType("application/xhtml+xml");
            	pw.println("<div class=\"user\">");
            	pw.println("<p><a href=\""+user+"\" rel=\"email\">"+user+"</p>");
            	pw.println("<p><a href=\""+node+"\" rel=\"node\">Visit node</a></p>");
            	pw.println("<p><a href=\"/v2/users/bob@whitequail.org/urls\" rel=\"urls\">Urls</a></p>");
            	//pw.println("<p><a href=\"/v2/users/bob@whitequail.org/notifications\" rel=\"notifications\">Notifications</a></p>");
            	pw.println("</div>");
            }
		}
	    else if (uri.matches("/v2/users/[^/]*/urls")) {
			String user = uri.substring(("/v2/users/").length());
			user = user.substring(0, user.length()-5);
			String[] urls = DBHelper.getUserUrlsFromDB(user);
            PrintWriter pw = response.getWriter();
            if (xmlOnly) {
            	//response.setContentType("application/xml");
            	pw.println("stub");
            }
            else {
            	response.setContentType("application/xhtml+xml");
            	for (int i = 0; i < urls.length; i++) {
                	pw.println("<a href=\"http://yournode.com/users/bob@whitequail.org/urls/54\" rel=\"url\">Metadata on http://somewhere.com</a>");
            	}
            }
	    }
	}

	public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException {
	    String uri = request.getRequestURI();
	    StringBuffer url = request.getRequestURL();
	    String servletPath = request.getServletPath();
	    String queryString = request.getQueryString();

	    DocumentBuilderFactory xmlFactory;
	    DocumentBuilder xml;
	    DocumentBuilder bodyXml;

	    try {
	    	xmlFactory = DocumentBuilderFactory.newInstance();
		    xml = xmlFactory.newDocumentBuilder();
		    bodyXml = xmlFactory.newDocumentBuilder();
	    }
	    catch (ParserConfigurationException e) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

            pw.println("error with parser");
            return;
	    }
	    
	    BufferedReader br = request.getReader(); 
	    char[] buf = new char[4 * 1024]; new String(buf, 0, 5 );
	    int len;
	    String body = "";
	    while ((len = br.read(buf, 0, buf.length)) != -1) {
	    	body += new String(buf, 0, len);
	    }
	    InputSource is = new InputSource();
	    is.setCharacterStream(new StringReader(body));
    	
	    Document xmlDoc;
    	try {
    		xmlDoc = bodyXml.parse(is);
    	}
    	catch (SAXException e) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

            pw.println("error with parsing");
            pw.println(body);
            pw.println(e.getMessage());
            return;
    	}
        
	    if (uri.matches("/v1/registerurls")) {
	    	Node inBase = xmlDoc.getElementsByTagName("urls").item(0);
	    	NodeList inUrls = inBase.getChildNodes();
	    	String[] urls = new String[inUrls.getLength()];
	    	for (int i = 0; i < inUrls.getLength(); i++) {
	    		urls[i] = inUrls.item(i).getAttributes().getNamedItem("value").getNodeValue();
	    	}

	    	Document xmlOut = xml.newDocument();
	    	Element base = xmlOut.createElement("urls");

	    	for (int i = 0; i < urls.length; i++) {
	    		Element urlNode = xmlOut.createElement("url");
	    		urlNode.setAttribute("value", urls[i]);
	    		base.appendChild(urlNode);
	    	}

	    	xmlOut.appendChild(base);

            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

	    	try {
	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		    	StreamResult result = new StreamResult(new StringWriter());
		    	DOMSource source = new DOMSource(xmlOut);
		    	transformer.transform(source, result);
		    	String xmlString = result.getWriter().toString();

		    	pw.println(xmlString);
		    	return;
	    	}
	    	catch (TransformerException e) {
	    		pw.println("registerurls failed");
	    		return;
	    	}
	    }
	    else if (uri.matches("/v2/users")) {
	    	PrintWriter pw = response.getWriter();
	    	
	    	String email = "";
	    	String node = "";
	    	
	    	NodeList inBase = xmlDoc.getChildNodes();
	    	for (int i = 0; i < inBase.getLength(); i++) {
	    		if (inBase.item(i).getNodeName().equals("user")) {
	    			NodeList inUsers = inBase.item(i).getChildNodes();
	    			for (int j = 0; j < inUsers.getLength(); j++) {
	    				if (inUsers.item(j).getNodeName().equals("email")) {
	    					email = inUsers.item(j).getChildNodes().item(0).getNodeValue();
	    				}
	    				if (inUsers.item(j).getNodeName().equals("node")) {
	    					node = inUsers.item(j).getChildNodes().item(0).getNodeValue();
	    				}
	    			}
	    			break;
	    		}
	    	}
	    	
	    	if ((email.length()) > 0 && (node.length() > 0)) {
	            response.setContentType("text/plain");
	            if (DBHelper.getUserExistenceFromDB(email)) {
		            response.setContentType("text/plain");
		            
		    		pw.println("user already in system");
		    		return;
	            }
	            else if (DBHelper.createUser(email, node)) {
		            response.setContentType("text/plain");
		            
		    		pw.println("user created successfully");
		    		return;
	    		}
	    		else {
		            response.setContentType("text/plain");
		            
		    		pw.println("error with database");
		    		pw.println(email);
		    		pw.println(node);
		    		return;
	    		}
	    	}
	    	else {
	            response.setContentType("text/plain");
	            
	    		pw.println("error with validation");
	    		return;
	    	}
	    }
	    else {
	    	PrintWriter pw = response.getWriter();
            response.setContentType("text/plain");
            
    		pw.println("page not found");
    		return;
	    }
	}
}