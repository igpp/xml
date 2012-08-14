package igpp.xml;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;

public class ToXHTML 
{
	private String mVersion = "1.0.0";
	private String mOverview = "Converts an HTML file into a compliant XHTML file."
			 ;
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private boolean mVerbose= false;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public ToXHTML() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated document to {file}. Default: System.out." );
		mAppOptions.addOption( "c", "css", true, "CSS. A CSS file to insert into the generated document." );
	}
	
    public static void main(String[] args) 
    {
    	String	outFile = null;
    	String	cssFile = null;
    	
    	ToXHTML me = new ToXHTML();
		
		CommandLineParser parser = new PosixParser();
		try {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) me.mVerbose = true;
   			if(line.hasOption("o")) outFile = line.getOptionValue("o");
   			if(line.hasOption("c")) cssFile = line.getOptionValue("c");
   			
        	PrintStream outStream = System.out;
        	if(outFile != null) {
        		outStream = new PrintStream(outFile);
        	}
        	
   			// Process arguments looking for variable context
        	if(line.getArgs().length != 1) {
        		System.out.println("Pass the file to transform as a plain command-line argument.");
        		return;
        	}
        	
        	outStream.print(fileToXHTML(line.getArgs()[0], cssFile));
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


    public static String fileToXHTML(String fileName)
    		throws Exception
    {
    	return fileToXHTML(fileName, null);
    }
    
    public static String fileToXHTML(String fileName, String cssFile)
    		throws Exception
    {
        File file = new File(fileName);
        FileReader reader = new FileReader(file);

        CleanerProperties props = new CleanerProperties();

        // HTMLCleaner part
        // set some properties to non-default values
        props.setTranslateSpecialEntities(true);
        props.setTransResCharsToNCR(true);
        props.setOmitComments(true);

        // do parsing
        TagNode tagNode = new HtmlCleaner(props).clean(reader);
        
        // Add CSS - if passed
        if(cssFile != null) insertCSS(tagNode, cssFile);
        
        // serialize to xml file
        PrettyXmlSerializer xml = new PrettyXmlSerializer(props);
        
        return xml.getAsString(tagNode, "utf-8");
        // xml.writeToFile(tagNode, "page.xhtml", "utf-8");    	
    }
    
    public static String stringToXHTML(String content)
    		throws Exception
    {
        CleanerProperties props = new CleanerProperties();

        // HTMLCleaner part
        // set some properties to non-default values
        props.setTranslateSpecialEntities(true);
        props.setTransResCharsToNCR(true);
        props.setOmitComments(true);

        // do parsing
        TagNode tagNode = new HtmlCleaner(props).clean(content);
        
        // serialize to xml file
        PrettyXmlSerializer xml = new PrettyXmlSerializer(props);
        
        return xml.getAsString(tagNode, "utf-8");
    }

    public static void insertCSS(TagNode tagNode, String cssFile)
    {
    	tagNode.traverse(new TagNodeVisitor() {
    		 
            public boolean visit(TagNode tagNode, HtmlNode htmlNode) {
                if (htmlNode instanceof TagNode) {
                    TagNode tag = (TagNode) htmlNode;
                    String tagName = tag.getName();
                    if ("head".equals(tagName)) {
                    	TagNode style = new TagNode("style");
                    	tag.addChild(style);
                    }
                }
                // tells visitor to continue traversing the DOM tree
                return true;
            }
        });
    }
}
