package cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import java.sql.*;
import java.util.Date;
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
	    if ((accept != null) && accept.matches(".*application/xml.*") && (! accept.matches(".*application/xhtml.*"))) {
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

	    if (uri.matches("/v1/whoami")) {
	    	Document xmlDoc = xml.newDocument();
	    	Element base = xmlDoc.createElement("url");
	    	base.setAttribute("value", "http://somerandomurl.org");
	    	xmlDoc.appendChild(base);

            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();
            pw.println("tiedemanb193schs@yahoo.com");
            
            return;
        }
	    else if (uri.matches("/v1/lookupurls")) {
	    	Document xmlDoc = xml.newDocument();
	    	Element base = xmlDoc.createElement("urls");

	    	String[] urls = DBHelper.getUrlsFromDB();
	    	for (int i = 0; i < urls.length; i++) {
	    		Element urlNode = xmlDoc.createElement("url");
	    		urlNode.setTextContent(urls[i]);
	    		base.appendChild(urlNode);
	    	}

	    	xmlDoc.appendChild(base);

            response.setContentType("text/xml");
            PrintWriter pw = response.getWriter();

	    	try {
	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		    	StreamResult result = new StreamResult(new StringWriter());
		    	DOMSource source = new DOMSource(xmlDoc);
		    	transformer.transform(source, result);
		    	String xmlString = result.getWriter().toString();

		    	pw.println(xmlString);
	    	}
	    	catch (TransformerException e) {
	            response.setContentType("text/plain");
	    		pw.println("lookupurls failed");
	    	}
	    	
	    	return;
        }
	    else if (uri.matches("/admin/setup")) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

            boolean result = DBHelper.setupDB();
            if (result) {
        		pw.println("setup successful");
            }
            else {
        		pw.println("setup failed");
            }
        	
        	return;
        }
	    else if (uri.matches("/admin/reset")) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

            boolean result = DBHelper.resetDB();
            if (result) {
        		pw.println("reset successful");
            }
            else {
        		pw.println("reset failed");
            }
        	
        	return;
        }
	    else if (uri.matches("/v2/users")) {
            PrintWriter pw = response.getWriter();
            String[][] users = DBHelper.getUsersFromDB();
            if (xmlOnly) {
            	response.setContentType("text/xml");
            	pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		    	pw.println("<users>");
            	for (int i = 0; i < users.length; i++) {
            		pw.println("<user>" +
            			"<name>"+users[i][1]+"</name>" +
            			"<link>"+users[i][2]+"/v2/users/"+users[i][1]+"</link>" +
            			"</user>");
            	}
            	pw.println("</users>");
            }
            else {
            	response.setContentType("application/xhtml+xml");
            	pw.println("<ul class=\"users\">");
            	for (int i = 0; i < users.length; i++) {
            		pw.println("<li class=\"user\">" +
            			"<a href=\""+users[i][2]+"/v2/users/"+users[i][1]+"\" rel=\"details\">"+users[i][1]+"</a>" +
            			"</li>");
            	}
            	pw.println("</ul>");
            }
            
            return;
	    }
	    else if (uri.matches("/v2/users/[^/]*")) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();
            
			String user = uri.substring(("/v2/users/").length());
			if (DBHelper.getUserExistenceFromDB(user)) {
				String[] userdata = DBHelper.getUserFromDB(user);
				//String userid = userdata[0];
				String usernode = userdata[2];
				
				//String[][] userurls = DBHelper.getUserUrlsFromDB(userid);
				//String[][] notifications = DBHelper.getUserNotificationsFromDB(userid);
				
	            if (xmlOnly) {
	            	response.setContentType("text/xml");
	            	pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	            	pw.println("<user>");
	            	pw.println("<email>"+user+"</email>");
	            	pw.println("<node>"+usernode+"</node>");
	            	pw.println("<urls>"+usernode+"/v2/users/"+user+"/urls</urls>");
	            	pw.println("<notifications>"+usernode+"/v2/users/"+user+"/notifications</notifications>");
	            	pw.println("</user>");
	            }
	            else {
	            	response.setContentType("application/xhtml+xml");
	            	pw.println("<div class=\"user\">");
	            	pw.println("<p><a href=\""+user+"\" rel=\"email\">"+user+"</a></p>");
	            	pw.println("<p><a href=\""+usernode+"\" rel=\"node\">Visit node</a></p>");
	            	pw.println("<p><a href=\""+usernode+"/v2/users/"+user+"/urls\" rel=\"urls\">Urls</a></p>");
	            	pw.println("<p><a href=\""+usernode+"/v2/users/"+user+"/notifications\" rel=\"notifications\">Notifications</a></p>");
	            	pw.println("</div>");
	            }
			}
			else {
				pw.println("user not found");
			}
			
			return;
		}
	    else if (uri.matches("/v2/users/[^/]*/urls")) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

			String user = uri.substring(("/v2/users/").length());
			user = user.substring(0, user.length()-5);
			if (DBHelper.getUserExistenceFromDB(user)) {            	
				String[] userdata = DBHelper.getUserFromDB(user);
				String userid = userdata[0];
				String host = userdata[2];
				
				String[][] userurls = DBHelper.getUserUrlsFromDB(userid);
				//String[][] notifications = DBHelper.getUserNotificationsFromDB(userid);

            	if (userurls.length == 0) {
            		pw.println("no urls found");
            	}
            	else if (xmlOnly) {
        	    	Document xmlDoc = xml.newDocument();
        	    	Element base = xmlDoc.createElement("urls");
        	    	
        	    	for (int i = 0; i < userurls.length; i++) {
        	    		Element urlNode = xmlDoc.createElement("url");
        	    		Element uriNode = xmlDoc.createElement("uri");
        	    		uriNode.setTextContent(host + "/users/" + user + "/urls/" + userurls[i][0]);
        	    		Element bookmark = xmlDoc.createElement("bookmark");
        	    		bookmark.setTextContent(userurls[i][2]);
        	    		urlNode.appendChild(uriNode);
        	    		urlNode.appendChild(bookmark);
        	    		base.appendChild(urlNode);
        	    	}

        	    	xmlDoc.appendChild(base);
        	    	
        	    	try {
        	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
        		    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        		    	StreamResult result = new StreamResult(new StringWriter());
        		    	DOMSource source = new DOMSource(xmlDoc);
        		    	transformer.transform(source, result);
        		    	String xmlString = result.getWriter().toString();

        		    	response.setContentType("application/xml");
        		    	pw.println(xmlString);
        	    	}
        	    	catch (TransformerException e) {
        	            response.setContentType("text/plain");
        	    		pw.println("lookupurls failed");
        	    	}
	            }
	            else {
	            	response.setContentType("application/xhtml+xml");
	            	pw.println("<div class=\"urls\">");
	            	for (int i = 0; i < userurls.length; i++) {
	                	pw.println("<a href=\""+host+"/v2/users/"+user+"/urls/"+userurls[i][0]+"\" rel=\"url\">Metadata on "+userurls[i][2]+"</a>");
	            	}
	            	pw.println("</div>");
	            }
			}
			else {
				pw.println("user not found");
			}
			
			return;
	    }
	    else if (uri.matches("/v2/users/[^/]*/urls/\\d*")) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

			String[] data = uri.split("/v2/users/");
			data = data[1].split("/urls/");
			String user = data[0];
			int urlid = Integer.parseInt(data[1]);

			if (DBHelper.getUserExistenceFromDB(user)) {            	
				String[] userdata = DBHelper.getUserFromDB(user);
				int userid = Integer.parseInt(userdata[0]);
				String host = userdata[2];
				
				if (DBHelper.getUrlIdFromDBUsingID(userid, urlid) > -1) {
					String[] urldata = DBHelper.getUrlDataFromDB(urlid);
					String[] categories = DBHelper.getCategoriesFromDB(urlid);
					
	            	if (xmlOnly) {
		            	//response.setContentType("application/xml");
		            	pw.println("stub");
		            }
		            else {
		            	response.setContentType("application/xhtml+xml");
		            	pw.println("<div class=\"url\">");
		            	try {
		            		pw.println("<abbr class=\"date-added\" title=\""+timestampFormatter(urldata[3])+"\">"+SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).parse(urldata[3])+"</abbr>");
		            	}
		            	catch (Exception e) {
		            		
		            	}
		            	pw.println("<a rel=\"source\" href=\""+urldata[2]+"\">"+urldata[2]+"</a>");
		            	if (categories.length > 0) {
		            		pw.println("<ul>Categories");
		            		for (int i = 0; i < categories.length; i++) {
		            			pw.println("<li><a href=\""+host+"/v2/categories/"+categories[i]+"\" rel=\"category\">"+categories[i]+"</a></li>");
		            		}
			            	pw.println("</ul>");
		            	}
		            	pw.println("</div>");
		            }
				}
				else {
					pw.println("url resource not found");
				}				
			}
			else {
				pw.println("user not found");
			}
			
			return;
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

	    boolean xmlOnly = false;
	    String accept = request.getHeader("Accept");
	    if ((accept != null) && accept.matches(".*application/xml.*") && (! accept.matches(".*application/xhtml.*"))) {
	    	xmlOnly = true;
	    }
	    
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
	    	PrintWriter pw = response.getWriter();
	    	
	    	String[] urls = new String[0];
	    	String[] temp;
	    	
	    	NodeList inBase = xmlDoc.getChildNodes();
	    	for (int i = 0; i < inBase.getLength(); i++) {
	    		if (inBase.item(i).getNodeName().equals("urls")) {
	    			NodeList inUrls = inBase.item(i).getChildNodes();
	    			for (int j = 0; j < inUrls.getLength(); j++) {
	    				if (inUrls.item(j).getNodeName().equals("url")) {
	    					temp = new String[urls.length + 1];
	    					System.arraycopy(urls, 0, temp, 0, urls.length);
	    					temp[urls.length] = inUrls.item(j).getChildNodes().item(0).getNodeValue();
	    					urls = temp;
	    				}
	    			}
	    		}
	    	}
	    	
	    	if (urls.length > 0) {
	            String[] registeredUrls = DBHelper.registerNonExistingUrls(urls);

	            response.setContentType("text/xml");
            	pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	            pw.println("<urls>");
	            for (int i = 0; i < registeredUrls.length; i++) {
	            	pw.println("<url>"+registeredUrls[i]+"</url>");
	            }
	            pw.println("</urls>");
	            
	            return;
	    	}
	    	else {
	            response.setContentType("text/plain");
	            
	    		pw.println("error with validation");
	    		
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
	    else if (uri.matches("/v2/users/[^/]*/urls")) {
            response.setContentType("text/plain");
            PrintWriter pw = response.getWriter();

			String user = uri.substring(("/v2/users/").length());
			user = user.substring(0, user.length()-5);
			String[] userdata = DBHelper.getUserFromDB(user);
			int userid = Integer.parseInt(userdata[0]);
			String host = userdata[2];
			
			if (DBHelper.getUserExistenceFromDB(user)) {
				String urlUri = "";
				String[] categories = new String[0];
				String[] comments = new String[0];
				String[] temp;
		    	
		    	NodeList inBase = xmlDoc.getChildNodes();
		    	for (int i = 0; i < inBase.getLength(); i++) {
		    		if (inBase.item(i).getNodeName().equals("url")) {
		    			NodeList inUsers = inBase.item(i).getChildNodes();
		    			for (int j = 0; j < inUsers.getLength(); j++) {
		    				if (inUsers.item(j).getNodeName().equals("uri")) {
		    					urlUri = inUsers.item(j).getChildNodes().item(0).getNodeValue();
		    				}
		    				else if (inUsers.item(j).getNodeName().equals("categories")) {
		    					NodeList inCategories = inUsers.item(j).getChildNodes();
		    					for (int k = 0; k < inCategories.getLength(); k++) {
		    						if (inCategories.item(k).getNodeName().equals("category")) {
		    							temp = new String[categories.length + 1];
		    							System.arraycopy(categories, 0, temp, 0, categories.length);
		    							temp[categories.length] = inCategories.item(k).getChildNodes().item(0).getNodeValue();
		    							categories = temp;
		    						}
		    					}
		    				}
		    				else if (inUsers.item(j).getNodeName().equals("comments")) {
		    					NodeList inComments = inUsers.item(j).getChildNodes();
		    					for (int k = 0; k < inComments.getLength(); k++) {
		    						if (inComments.item(k).getNodeName().equals("comment")) {
		    							temp = new String[comments.length + 1];
		    							System.arraycopy(comments, 0, temp, 0, comments.length);
		    							temp[comments.length] = inComments.item(k).getChildNodes().item(0).getNodeValue();
		    							comments = temp;
		    						}
		    					}
		    				}
		    			}
		    			break;
		    		}
		    	}
				
				if (DBHelper.addUrl(userid, urlUri, categories, comments)) {
					response.setHeader("Location", host+"/v2/users/"+user+"/urls/"+Integer.toString(DBHelper.getUrlIdFromDB(userid, urlUri)));
					response.setStatus(201);
				}
				else {
					pw.println("url resource not added");
				}
			}
			else {
				pw.println("user not found");
			}
			
			return;
	    }
	    else {
	    	PrintWriter pw = response.getWriter();
            response.setContentType("text/plain");
            
    		pw.println("page not found");
    		return;
	    }
	}
    
	public String timestampFormatter(String timestamp) {
		return timestamp = timestamp.substring(0, timestamp.length()-2) + ":" + timestamp.substring(timestamp.length()-2, timestamp.length());
	}
}