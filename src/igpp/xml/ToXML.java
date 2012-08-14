package igpp.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.dom4j.Document;  
import org.dom4j.DocumentHelper;  
import org.dom4j.io.OutputFormat;  
import org.dom4j.io.XMLWriter;

public class ToXML 
{
	private String mVersion = "1.0.0";
	private String mOverview = "Converts XML to pretty XML."
			 ;
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private static boolean mVerbose= false;
	
	private static String mIndent = "   ";	// Indent amount for XML output;
	private static boolean mCompact = false;	// Whether to produce compact output.
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public ToXML() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated document to {file}. Default: System.out." );
		mAppOptions.addOption( "c", "compact", false, "Compact. Generate compact output. Extra whitespace is removed." );
		mAppOptions.addOption( "i", "indent", true, "Indent. The string to use as the indent. Default: 3 spaces." );
	}
	
    public static void main(String[] args) 
    {
    	String	outFile = null;
     	
    	ToXML me = new ToXML();
		
		CommandLineParser parser = new PosixParser();
		try {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) setVerbose(true);
   			if(line.hasOption("o")) outFile = line.getOptionValue("o");
   			if(line.hasOption("c")) setCompact(true);
   			if(line.hasOption("i")) setIndent(line.getOptionValue("i"));
   			
        	PrintStream outStream = System.out;
        	if(outFile != null) {
        		outStream = new PrintStream(outFile);
        	}
        	
   			// Process arguments looking for variable context
        	if(line.getArgs().length != 1) {
        		System.out.println("Pass the file to transform as a plain command-line argument.");
        		return;
        	}
        	
        	if(getVerbose()) System.out.println("Processing: " + line.getArgs()[0]); 
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
            byte[] buffer = new byte[(int) new File(fileName).length()];
            BufferedInputStream f = null;
            try {
                f = new BufferedInputStream(new FileInputStream(fileName));
                f.read(buffer);
            } finally {
                if (f != null) try { f.close(); } catch (IOException ignored) { }
            }
            return stringToXML(new String(buffer));
        } catch(Exception e) {
            e.printStackTrace(System.out);
        }
        return "";
    }
    
    public static String stringToXML(String xml){
        try{
        	Document doc = DocumentHelper.parseText(xml);  
        	StringWriter sw = new StringWriter();  
        	OutputFormat format = OutputFormat.createPrettyPrint(); 
    	    format.setTrimText(mCompact);
    	    format.setNewlines(mCompact);
        	format.setIndent(mIndent);
        	XMLWriter xw = new XMLWriter(sw, format);  
        	xw.write(doc); 
        	return sw.toString();
        } catch(Exception e) {
            e.printStackTrace(System.out);
	        return xml;
        }
    }
    
    public static String stringToPlainXML(String xml)
    {
    	XMLParser parsed = new XMLParser();
    	
    	try {
    		parsed.parseXMLString(xml);
    		parsed.trim();
    	} catch(Exception e) {
    		return null;
    	}
    	
    	return parsed.toString();
    }
    
    public static boolean setCompact(boolean compact)  { mCompact = compact; return mCompact; }
    public static String setIndent(String indent) { mIndent = indent; return mIndent; }
    public static boolean setVerbose(boolean verbose) { mVerbose = verbose; return mVerbose; }
    public static boolean getVerbose() { return mVerbose; }
}
