package cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

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
import org.xml.sax.SAXException;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class Servlet extends HttpServlet
{
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException
	{
	    String uri = request.getRequestURI ();
	    StringBuffer url = request.getRequestURL ();
	    String servletPath = request.getServletPath ();
	    String queryString = request.getQueryString ();
	    
	    DocumentBuilderFactory xmlFactory;
	    DocumentBuilder xml;
	    
	    try {
	    	xmlFactory = DocumentBuilderFactory.newInstance();
		    xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    }
	    catch (ParserConfigurationException e) {
            response.setContentType ("text/plain");
            PrintWriter pw = response.getWriter ();
            
            pw.println ("error with parser");
            return;
	    }
	    
	    if (uri.matches ("/whoami")) {
	    	Document xmlDoc = xml.newDocument();
	    	Element base = xmlDoc.createElement("url");
	    	base.setAttribute("value", "http://somerandomurl.org");
	    	xmlDoc.appendChild(base);
	    	
            response.setContentType ("text/plain");
            PrintWriter pw = response.getWriter ();
	    	
	    	try {
	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	
		    	StreamResult result = new StreamResult(new StringWriter());
		    	DOMSource source = new DOMSource(xmlDoc);
		    	transformer.transform(source, result);
		    	String xmlString = result.getWriter().toString();
		    	
		    	pw.println (xmlString);
		    	return;
	    	}
	    	catch (TransformerException e) {
	    		pw.println ("whoami failed");
	    		return;
	    	}
        }
        if (uri.matches ("/lookupurls")) {
	    	Document xmlDoc = xml.newDocument();
	    	Element base = xmlDoc.createElement("urls");
	    	
	    	String[] urls = {"http://webplaces.net/i_c_weiner", "https://www.wheresURLdo?.com"};
	    	for (int i = 0; i < urls.length; i++) {
	    		Element urlNode = xmlDoc.createElement("url");
	    		urlNode.setAttribute("value", urls[i]);
	    		base.appendChild(urlNode);
	    	}
	    	
	    	xmlDoc.appendChild(base);
	    	
            response.setContentType ("text/plain");
            PrintWriter pw = response.getWriter ();
	    	
	    	try {
	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	
		    	StreamResult result = new StreamResult(new StringWriter());
		    	DOMSource source = new DOMSource(xmlDoc);
		    	transformer.transform(source, result);
		    	String xmlString = result.getWriter().toString();
		    	
		    	pw.println (xmlString);
		    	return;
	    	}
	    	catch (TransformerException e) {
	    		pw.println ("lookupurls failed");
	    		return;
	    	}
        }
	}
	
	public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException {
	    String uri = request.getRequestURI ();
	    StringBuffer url = request.getRequestURL ();
	    String servletPath = request.getServletPath ();
	    String queryString = request.getQueryString ();
	    
	    DocumentBuilderFactory xmlFactory;
	    DocumentBuilder xml;
	    
	    try {
	    	xmlFactory = DocumentBuilderFactory.newInstance();
		    xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    }
	    catch (ParserConfigurationException e) {
            response.setContentType ("text/plain");
            PrintWriter pw = response.getWriter ();
            
            pw.println ("error with parser");
            return;
	    }
	    
	    if (uri.matches ("/registerurls")) {
	    	String input = "";
	    	BufferedReader reader = request.getReader();
	    	while (reader.ready()) input += reader.readLine();
	    	
	    	DOMParser parser = new DOMParser();
	    	try {
	    		parser.parse(input);
	    	}
	    	catch (SAXException e) {
	            response.setContentType ("text/plain");
	            PrintWriter pw = response.getWriter ();
	            
	            pw.println ("error with parsing");
	            return;
	    	}
	    	
	    	Document xmlDoc = parser.getDocument();
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
	    	
            response.setContentType ("text/plain");
            PrintWriter pw = response.getWriter ();
	    	
	    	try {
	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	
		    	StreamResult result = new StreamResult(new StringWriter());
		    	DOMSource source = new DOMSource(xmlOut);
		    	transformer.transform(source, result);
		    	String xmlString = result.getWriter().toString();
		    	
		    	pw.println (xmlString);
		    	return;
	    	}
	    	catch (TransformerException e) {
	    		pw.println ("registerurls failed");
	    		return;
	    	}
	    }
	}
}