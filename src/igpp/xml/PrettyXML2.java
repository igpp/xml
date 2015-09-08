package igpp.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

public class PrettyXML2 
{
	private String mVersion = "1.0.0";
	private String mOverview = "Converts XML to pretty XML."
			 ;
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private boolean mVerbose= false;
	
	private static String mIndent = "3";	// Indent amount for XML output;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public PrettyXML2() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated document to {file}. Default: System.out." );
	}
	
    public static void main(String[] args) 
    {
    	String	outFile = null;
     	
    	PrettyXML2 me = new PrettyXML2();
		
		CommandLineParser parser = new PosixParser();
		try {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) me.mVerbose = true;
   			if(line.hasOption("o")) outFile = line.getOptionValue("o");
   			
        	PrintStream outStream = System.out;
        	if(outFile != null) {
        		outStream = new PrintStream(outFile);
        	}
        	
   			// Process arguments looking for variable context
        	if(line.getArgs().length != 1) {
        		System.out.println("Pass the file to transform as a plain command-line argument.");
        		return;
        	}
        	
        	if(me.mVerbose) System.out.println("Processing: " + line.getArgs()[0]); 
        	outStream.print(fileToXML(line.getArgs()[0]));
        } catch (Exception e) {
        	e.printStackTrace(System.out);
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

    public static String fileToXML(String fileName){
        try{
            Transformer serializer= SAXTransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", mIndent);
            Source xmlSource=new SAXSource(new InputSource(fileName));
            StreamResult res =  new StreamResult(new ByteArrayOutputStream());            
            serializer.transform(xmlSource, res);
            return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());
        } catch(Exception e) {
            e.printStackTrace(System.out);
        }
        return "";
    }
    
    public static String stringToXML(String xml){
        try{
            Transformer serializer= SAXTransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", mIndent);
            Source xmlSource=new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
            StreamResult res =  new StreamResult(new ByteArrayOutputStream());            
            serializer.transform(xmlSource, res);
            return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());
        } catch(Exception e) {
            e.printStackTrace(System.out);
	        return xml;
        }
    }
}
