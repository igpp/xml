package igpp.xml;

// import javax.xml.transform.*;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

//import java.io.*;
import java.io.Reader;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;

// import java.net.*;
import java.net.URL;
import java.net.URLConnection;

// import java.util.*
import java.util.HashMap;

//import java.servlet.jsp.*;
import javax.servlet.jsp.JspWriter;
    
/**
 * Transform an XML file using a stylsheet (XSL)
 *
 * @author Todd King
 * @version 1.00 2006
 */
public class Transform 
{
	String	mVersion = "1.0.2";
	/** 
    * Command-line interface
	 * 
    * @param args    	the arguments passe don the command-line.
    *
    * @since           1.0
    **/
	static public void main(String[] args) 
	{
		String	message;
		Transform me = new Transform();
		
		if(args.length < 2) {
			System.out.println("Version: " + me.mVersion);
			System.out.println("Usage: " + me.getClass().getName() + " {xmlFile} {xslFile}");
			System.exit(1);
		}
		
		try {
			if(args[0].startsWith("http:")) {
				me.perform(getURLSource(args[0]), args[1], System.out);
			} else {
				me.perform(args[0], args[1], System.out);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Get a new transformer based on a stylesheet
	 *
	 * @param xslFile		the pathname to the XML stylesheet file.
	 *
	 * @return the {@link Transformer} prepared to use the passed stylesheet.
	 **/
    static public Transformer getTransformer(String xslFile) 
    	throws TransformerException 
    {
      System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

		TransformerFactory tFactory = TransformerFactory.newInstance();
		
		Transformer transformer = tFactory.newTransformer(new StreamSource(xslFile));
		
		return transformer;
    }
	 
    /**
    * Load a stylesheet and maintain it in memory for reuse.
    *
	 * @param xslFile		the pathname to the XML stylesheet file.
	 * @param cache	the {@link HashMap} to maintain the prepared stylesheets.
	 *
	 * @return the {@link Transformer} prepared to use the passed stylesheet.
    **/
    static public synchronized Transformer getTransformer(String xslFile, HashMap cache) 
    	throws TransformerException 
    {
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        
        Templates template = (Templates)cache.get(xslFile);
        if (template == null) {
            TransformerFactory factory = TransformerFactory.newInstance();
            template = factory.newTemplates(new StreamSource(new File(xslFile)));
            cache.put(xslFile, template);
        }
        
        Transformer transformer = template.newTransformer();

        return transformer;
    }

	/** 
    * Get a {@link StreamSource} to the response of invoking a URL.
    *
	 * @param url			the URL of the resource to transform.
	 *
	 * @return a {@link StreamSource} for the URL.
    **/
	static public StreamSource getURLSource(String url)
		throws Exception
	{
		URL urlSource = new URL(url);
		URLConnection con = urlSource.openConnection();
		InputStream stream = con.getInputStream();

		StreamSource source = new StreamSource(stream);
		
		return source;
	}

	/** 
    * Use an XML style sheet to transform an XML document and write the results
    * to an {@link PrintWriter}.
    *
	 * @param xmlStream    	the {@link StreamSource} to the XML text.
	 * @param xslFile		the pathname to the XML stylesheet file.
	 * @param outStream		the {@link PrintStream} to write the results.
    *
    * @since           1.0
    **/
	static public void perform(StreamSource xmlStream, String xslFile, PrintStream outStream)
		throws Exception
	{
		Transformer transformer = getTransformer(xslFile);
		
		transformer.transform(xmlStream, new StreamResult(outStream));
	}
	
	/** 
    * Use an XML style sheet to transform an XML document and write the results
    * to an {@link PrintWriter}.
    *
	 * @param xmlFile    	the pathname to the XML file.
	 * @param xslFile		the pathname to the XML stylesheet file.
	 * @param outWriter		the {@link PrintWriter} to write the results.
    *
    * @since           1.0
    **/
	static public void perform(String xmlFile, String xslFile, PrintWriter outWriter)
		throws Exception
	{
		perform(new FileInputStream(xmlFile), xslFile, outWriter);
		
		// Transformer transformer = getTransformer(xslFile);
		// 
		// transformer.transform(new StreamSource(xmlFile), new StreamResult(outWriter));
	}

	/** 
    * Use an XML style sheet to transform an XML document and write the results
    * to an {@link PrintWriter}.
    *
	 * @param xmlStream    	the {@link InputStream} for the XML text.
	 * @param xslFile		the pathname to the XML stylesheet file.
	 * @param outWriter		the {@link PrintWriter} to write the results.
    *
    * @since           1.0
    **/
	static public void perform(InputStream xmlStream, String xslFile, PrintWriter outWriter)
		throws Exception
	{
		Transformer transformer = getTransformer(xslFile);
		
		Reader reader = new InputStreamReader(xmlStream, "UTF-8");

		// transformer.transform(new StreamSource(xmlStream), new StreamResult(outWriter));
		transformer.transform(new StreamSource(reader), new StreamResult(outWriter));
	}
	
	/** 
    * Use an XML style sheet to transform an XML document and write the results
    * to an {@link PrintStream}.
    *
	 * @param xmlFile    	the pathname to the XML file.
	 * @param xslFile		the pathname to the XML stylesheet file.
	 * @param outStream		the {@link PrintStream} to write the results.
    *
    * @since           1.0
    **/
	static public void perform(String xmlFile, String xslFile, PrintStream outStream)
		throws Exception
	{
		Transformer transformer = getTransformer(xslFile);
		
		FileInputStream stream = new FileInputStream(xmlFile);
		Reader reader = new InputStreamReader(stream, "UTF-8");
		
		transformer.transform(new StreamSource(reader), new StreamResult(outStream));
	}
	
	/** 
    * Use an XML style sheet to transform an XML document and write the results
    * to an {@link JspWriter}. A {@link JspWriter} is the default output stream
    * for JSP pages.
    *
	 * @param xmlFile    	the pathname to the XML file.
	 * @param xslFile		the pathname to the XML stylesheet file.
	 * @param outStream		the {@link JspWriter} to write the results.
    *
    * @since           1.0
    **/
	static public void perform(String xmlFile, String xslFile, JspWriter outStream)
		throws Exception
	{
		Transformer transformer = getTransformer(xslFile);

		FileInputStream stream = new FileInputStream(xmlFile);
		Reader reader = new InputStreamReader(stream, "UTF-8");

		transformer.transform(new StreamSource(reader), new StreamResult(outStream));
	}
	
	/** 
    * Use an XML style sheet to transform an XML document and write the results
    * to an {@link JspWriter}. A {@link JspWriter} is the default output stream
    * for JSP pages.
    *
	 * @param xmlFile    	the {@link StringReader} for the XML text.
	 * @param xslFile		the pathname to the XML stylesheet file.
	 * @param outStream		the {@link JspWriter} to write the results.
    *
    * @since           1.0
    **/
	static public void perform(StringReader xmlFile, String xslFile, JspWriter outStream)
		throws Exception
	{
		Transformer transformer = getTransformer(xslFile);
		
		transformer.transform(new StreamSource(xmlFile),  new StreamResult(outStream)
		   );
	}
	
	/** 
    * Use an XML style sheet to transform an XML document and write the results
    * to an {@link JspWriter}. A {@link JspWriter} is the default output stream
    * for JSP pages.
    *
	 * @param xmlStream    	the {@link InputStream} for the XML text.
	 * @param xslFile		the pathname to the XML stylesheet file.
	 * @param outStream		the {@link JspWriter} to write the results.
    *
    * @since           1.0
    **/
	static public void perform(InputStream xmlStream, String xslFile, JspWriter outStream)
		throws Exception
	{
		Transformer transformer = getTransformer(xslFile);
		
		transformer.transform(new StreamSource(xmlStream), new StreamResult(outStream)
		   );
	}

	/** 
    * Use an XML style sheet to transform an XML document returned by a URL and write the results
    * to an {@link JspWriter}. A {@link JspWriter} is the default output stream
    * for JSP pages.
    *
	 * @param url			the URL of the resource to transform.
	 * @param xslFile		the pathname to the XML stylesheet file.
    * @param outStream		the {@link JspWriter} to write the results.
    *
    * @since           1.0
    **/
	static public void performOnURL(String url, String xslFile, JspWriter outStream)
		throws Exception
	{
		URL urlSource = new URL(url);
		URLConnection con = urlSource.openConnection();
		InputStream stream = con.getInputStream();

		perform(stream, xslFile, outStream);
		
		stream.close();
	}
	
}