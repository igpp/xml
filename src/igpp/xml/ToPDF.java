package igpp.xml;

import java.io.File;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.xhtmlrenderer.pdf.ITextRenderer;

public class ToPDF 
{
	private String mVersion = "1.0.0";
	private String mOverview = "Convert an XHTML file into a PDF file."
			 ;
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private boolean mVerbose= false;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public ToPDF() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated document to {file}. Default: System.out." );
	}
	
    public static void main(String[] args) 
    {
    	String	outfile = null;
    	
    	ToPDF me = new ToPDF();
		
		CommandLineParser parser = new PosixParser();
		try {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) me.mVerbose = true;
   			if(line.hasOption("o")) outfile = line.getOptionValue("o");
   			
        	PrintStream outStream = System.out;
        	if(outfile != null) {
        		outStream = new PrintStream(outfile);
        	}
        	
   			// Process arguments looking for variable context
        	if(line.getArgs().length != 1) {
        		System.out.println("Pass the file to transform as a plain command-line argument.");
        		return;
        	}
        	
        	if(me.mVerbose) System.out.println("File: " + line.getArgs()[0]);
        	
        	fileToPDF(line.getArgs()[0], outStream);
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
		System.out.println("Usage: java " + getClass().getName() + " [options] [file...]");
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


    public static void fileToPDF(String fileName, PrintStream outStream)
    		throws Exception
    {
    	// FlyingSaucer and iText part
    	// File in = new File(fileName);
    	String url = new File(fileName).toURI().toURL().toString();
 
        ITextRenderer renderer = new ITextRenderer();
        // renderer.setDocument(in);
        renderer.setDocument(url);
        renderer.layout();
        renderer.createPDF(outStream);

        outStream.close();
    }

   
    public static void stringToPDF(String content, PrintStream outStream)
    		throws Exception
    {
    	// FlyingSaucer and iText part
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(content);
        renderer.layout();
        renderer.createPDF(outStream);

        outStream.close();
    }
}
