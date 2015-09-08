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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
 
/**
 * Transform an XML file using a stylesheet (XSL)
 *
 * @author Todd King
 * @version 1.00 2006
 */
public class Transform 
{
	private String	mVersion = "1.0.2";
	private String mOverview = "Transform an XML file using a stylesheet (XSL)."
			 ;
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private static boolean mVerbose= false;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public Transform() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "s", "stylesheet", true, "Stylesheet. The XML style sheet." );
	}

	/** 
    * Command-line interface
	 * 
    * @param args    	the arguments passed on the command-line.
    *
    * @since           1.0
    **/
	static public void main(String[] args) 
	{
		String stylesheet = null;
		
		Transform me = new Transform();

		CommandLineParser parser = new PosixParser();
		try {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) mVerbose = true;
   			if(line.hasOption("s")) stylesheet = line.getOptionValue("s");
		
			if(stylesheet == null) {
	    		me.showHelp();
	    		return;			
			}

  			// Process arguments looking for variable context
        	if(line.getArgs().length != 1) {
        		me.showHelp();
        		return;
        	}
        	
        	String filename = line.getArgs()[0];
        	
			if(filename.startsWith("http:")) {
				Transform.perform(getURLSource(filename), stylesheet, System.out);
			} else {
				Transform.perform(filename, stylesheet, System.out);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Display help information.
	 **/
	public void showHelp()
	{
		System.out.println("");
		System.out.println(getClass().getName() + "; Version: " + mVersion);
		System.out.println(mOverview);
		System.out.println("");
		System.out.println("Usage: java " + getClass().getName() + " [options] file");
		System.out.println("");
		System.out.println("Options:");

		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(getClass().getName(), mAppOptions);

		System.out.println("");
		System.out.println("Acknowledgements:");
		System.out.println(mAcknowledge);
		System.out.println("");
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
    static public synchronized Transformer getTransformer(String xslFile, HashMap<String, Templates> cache) 
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