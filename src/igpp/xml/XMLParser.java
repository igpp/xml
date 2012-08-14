package igpp.xml;

import java.io.PrintStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.ByteArrayInputStream;

import java.util.ArrayList;
import java.util.Iterator;

import java.lang.StringBuffer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * An base class for XML parsing.
 *
 * Supports parsing XML and loading the content into internal variables.
 * For each tag in the XML a corresponding set() method is called if it exists.
 * Allows for nesting of classes to support structured XML documents.
 * Supports navigation of the nested attributes and retrieval of values.
 * Items of multi-valued elements can be retrieved individually.
 *
 * @author Todd King
 * @author UCLA/IGPP
 * @version     1.0.0
 * @since     1.0.0
 **/
public class XMLParser {
	private String    mVersion = "1.0.0";
	private Document  mDocument;
	private String    mPathName = "";
	private String    mClassName = null;
	private Integer	  mIndent = new Integer(4);
	
	private ArrayList<String>	mRequired = new ArrayList<String>();
	
	/** 
	* Creates an instance of a XML 
	*
	* @since           1.0
	**/
	public XMLParser() 
	{
	}

	/**
	* Entry point for testing
	*
	* @since           1.0
	**/
	public static void main(String args[]) 
	{
		int      output = 1; // Default is XML
		XMLParser parser = new XMLParser();
		
		if(args.length == 0) {
			System.out.println("Version: " + parser.mVersion);
			System.out.println("Proper usage: " + parser.getClass().getName() + " pathname [dump|xml]");
			return;
		}
		
		try {
			parser.parseXML(args[0]);
			
			for(int i = 1; i < args.length; i++) {
				if(args[i].compareToIgnoreCase("dump") == 0) output = 0;  // Dump
				if(args[i].compareToIgnoreCase("xml") == 0) output = 1;   // XML
			}
			
			switch(output) {
			case 1:  // XML
				System.out.println("--- XML ---");
				parser.printXML(System.out);
				// doc.printXML(System.out);
				break;
			case 0:  // Dump as label
			default:
				// doc.print();
				break;
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			// e.printStackTrace(System.out);
			return;
		}
	}
    
    /** 
     * Parses a file containing XML into its constitute elments and
     * sets internal variables with the contents of the file.
     * The path and name of the file are passed to the method which is
     * opened and parsed. 
     * The processing of nodes starts at the
     * first node matching the class name {@link #getClassName()}.
     *
     * @param pathName  the fully qualified path and name of the file to parse.
     *
     * @since           1.0
     **/
    public void load(String pathName) 
       throws Exception 
    {
    	 load(pathName, getClassName());
    }
    
    /** 
     * Parses a file containing XML into its constitute elments and
     * sets internal variables with the contents of the file.
     * The path and name of the file are passed to the method which is
     * opened and parsed.
     * The processing of nodes starts at the
     * first node matching the passed root node name.
     *
     * @param pathName  the fully qualified path and name of the file to parse.
     * @param root	the name of the tag containg elements to process.
     *
     * @since           1.0
     **/
    public void load(String pathName, String root) 
       throws Exception 
    {
       parseXML(pathName);
       load(getDocument(), root);
    }
    
    /** 
     * Parses an InputStream of XML into its constitute elments and
     * sets internal variables with the contents of the file.
     * The processing of nodes starts at the
     * first node matching the class name {@link #getClassName()}.
     *
     * @param stream		the opened InputStream for the XML.
     *
     * @since           1.0
     **/
    public void load(InputStream stream) 
       throws Exception 
    {
       parseXML(stream);
       load(getDocument(), getClassName());
    }
    
    /** 
     * Parses an InputStream of XML into its constitute elments and
     * sets internal variables with the contents of the file.
     * The path and name of the file are passed to the method which is
     * opened and parsed.
     * The processing of nodes starts at the
     * first node matching the passed root node name.
     *
     * @param stream		the opened InputStream for the XML.
     * @param root	the name of the tag containg elements to process.
     *
     * @since           1.0
     **/
    public void load(InputStream stream, String root) 
       throws Exception 
    {
       parseXML(stream);
       load(getDocument(), root);
    }
    
    /** 
     * Parses a {@link Document} which contains a parsed XML file
     * and set internal variables with the contents of the file.
     *
     * @param document  the {@link Document} representation of a parsed XML file.
     * @param root	the name of the tag containg elements to process.
     *
     * @since           1.0
     **/
    public void load(Document document, String root) 
       throws Exception 
    {
       DOMSource source = new DOMSource(getDocument());
       source.getNode().normalize();  // Clean-up DOM structure
       
       Node node = source.getNode();
       if(root != null) node = findNode(node, root);
       
       processNode(node);
    }
    
    /** 
     * Parses a file containing XML into its constitute elments.
     * The path and name of the file are passed to the method which is
     * opened and parsed.
     *
     * @param pathName  the fully qualified path and name of the file to parse.
     *
     * @return          <code>true</code> if the file could be opened;
     *                  <code>false</code> otherwise.
     *
     * @since           1.0
     **/
    public boolean parseXML(String pathName) 
       throws Exception 
    {
       FileInputStream file;
       boolean        status;

       mPathName = pathName;

       file = new FileInputStream(mPathName);
       status = parseXML(file);
       file.close();

       return status;
    }

    /** 
     * Parses a string containing XML into its constitute elements.
     *
     * @param text	the String containing the XML text.
     *
     * @return          <code>true</code> if the file could be opened;
     *                  <code>false</code> otherwise.
     *
     * @since           1.0
     **/
    public boolean parseXMLString(String text) 
       throws Exception 
    {
    	ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes());
    	return parseXML(stream);
    }

    /** 
     * Parses a file containing XML into its constitute elments.
     * The file to parse must be previously opened and a InputStream
     * pointing to the file is passed.
     *
     * @param stream     a connection to a pre-opened file.
     *
     * @return          <code>true</code> if the file could be read;
     *                  <code>false</code> otherwise.
     *
     * @since           1.0
     **/
    public boolean parseXML(InputStream stream)  
       throws Exception
    {
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();

        // Configure parser
        factory.setValidating(false);   
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        factory.setCoalescing(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        mDocument = builder.parse(stream);

        return true;
    }

    /** 
     * Generates an XML representation of the content and return it as a string. 
     * 
     * @since           1.0.3
     **/
    public String toString()  
    {
        StringWriter out = new StringWriter();
        
    	try {
	        //set up a transformer
	        TransformerFactory transfac = TransformerFactory.newInstance();
	        try { // Need for Java 1.5 to work properly
	           transfac.setAttribute("indent-number", mIndent);
	        } catch(Exception ie) {
	        }
	        Transformer trans = transfac.newTransformer(getDefaultStyleSheet());
	
	        // Set up desired output format
	        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        trans.setOutputProperty(OutputKeys.INDENT, "yes");
	
	        // create string from xml tree
	        StreamResult result = new StreamResult(out);
	        DOMSource source = new DOMSource(mDocument);
	
	        trans.transform(source, result);
    	} catch(Exception e) {
    		// Do nothing
    	}
        return out.toString();
    }
    
    /** 
     * Generates an XML representation of the label and stream it to the 
     * print stream.
     * 
     * @param out     the stream to print the element to.
     *
     * @since           1.0
     **/
    public void printXML(PrintStream out)  
       throws Exception
    {
        //set up a transformer
        TransformerFactory transfac = TransformerFactory.newInstance();
        try { // Need for Java 1.5 to work properly
           transfac.setAttribute("indent-number", new Integer(4));
        } catch(Exception ie) {
        }
        Transformer trans = transfac.newTransformer(getDefaultStyleSheet());

        // Set up desired output format
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        // create string from xml tree
        StreamResult result = new StreamResult(out);
        DOMSource source = new DOMSource(mDocument);

        trans.transform(source, result);
   }

    /** 
     * Trim all text nodes by removing leading and trailing spaces.
     * @since           1.0
     **/
    public void trim()
       throws Exception
    {
        Node node;
        DOMSource  source = new DOMSource(mDocument);
        
        node = source.getNode();
        trimNode(node);
    }

    /** 
     * Trim the text in a node by removing leading and trailing spaces.
     * Does the same for all children nodes.
     * 
     * @param node     the {@link Node} of the document.
     *
     * @since           1.0
     **/
    public void trimNode(Node node)
    {
       Node  child;
       Node  sibling;
       if(node.getNodeType() == Node.TEXT_NODE) node.setNodeValue(node.getNodeValue().trim());
       if(node.hasChildNodes()) { // Output all children
          child = node.getFirstChild();
          sibling = child;
          while(sibling != null) {
             trimNode(sibling);
             sibling = sibling.getNextSibling();
          }
       }
    }


    
    /** 
     * Dump the CData sections of the XML
     * 
     * @param out     the stream to print the element to.
     *
     * @since           1.0
     **/
    public void dumpData(PrintStream out)
       throws Exception
    {
        Node node;
        DOMSource  source = new DOMSource(mDocument);
        
        node = source.getNode();
        dumpNode(out, node);
    }

    /** 
     * Dump the nodes of the XML
     * 
     * @param out     the stream to print the element to.
     *
     * @since           1.0
     **/
    public void dumpNode(PrintStream out, Node node)
    {
       Node  child;
       Node  sibling;
       out.println(node.getNodeType() + ": " + node.getNodeName() + ": \"" + node.getNodeValue() + "\"");
       if(node.hasChildNodes()) { // Output all children
          child = node.getFirstChild();
          sibling = child;
          while(sibling != null) {
             dumpNode(out, sibling);
             sibling = sibling.getNextSibling();
          }
       }
    }

   /** 
    * Find the next node with the given name.
    *
    * @param node       the {@link Node} to start at.
    * @param name       the name of the node to look for.
    *
    * @since           1.0
    **/
   public Node findNode(Node node, String name)
       throws Exception
   {
       String   buffer;
       Node  child;
       Node  sibling;

       if(node == null) return null;	// Nothing to search

       // Process all children
       if(node.hasChildNodes()) { // Output all children
          child = node.getFirstChild();
          sibling = child;
          while(sibling != null) {
             buffer = sibling.getNodeName();
             if(buffer == null) continue;
             // Elements in this object
             if(igpp.util.Text.isMatch(name, buffer)) return sibling;

             // Next node
             sibling = sibling.getNextSibling();
          }
       }
       return null;	// Not found
   }

	 /** 
     * Obtain a StreamSource to the default XML Style Sheet.
     * 
     * @return          a {@link StreamSource} which can be used to read the default
     *             style sheet.
     *
     * @since           1.0
     **/
    static public StreamSource getDefaultStyleSheet()
    {
       StringReader reader = new StringReader(
         "<!DOCTYPE stylesheet ["
          + "   <!ENTITY cr \"<xsl:text> </xsl:text>\">"
          + "]>"
          + "<xsl:stylesheet"
          + "   xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\""
          + "   xmlns:xalan=\"http://xml.apache.org/xslt\""
          + "   version=\"1.0\""
          + ">"
          + ""
          + "<xsl:output method=\"xml\" indent=\"yes\" xalan:indent-amount=\"4\"/>"
          + ""
          + "<!-- copy out the xml -->"
          + "<xsl:template match=\"* | @*\">"
          + "   <xsl:copy><xsl:copy-of select=\"@*\"/><xsl:apply-templates/></xsl:copy>"
          + "</xsl:template>"
          + "</xsl:stylesheet>"
       );
       
       return new StreamSource(reader);
    }

    /** 
     * Obtain the Document containing the representation of the parsed XML files.
     * 
     * @return          a {@link Document} containing the representation of the parsed XML file.
     *
     * @since           1.0
     **/
    public Document getDocument()
    {
       return mDocument;
    }

    /** 
     * Walk the nodes of the Document and populate the resource classes.
     * The name of each element encountered is used to find a method
     * to store the value. The method search for has the element name
     * with the prefix "set" and an parameter of type "{@link String}". 
     * If such a method is found the text contained by the
     * element tags is passed to the method. Then a method with the prefix of "set" is
     * with a parameter of of type "{@link Node}" is attempted. If such a method is found 
     * then method is called with the {@link Node} representing the element is passed as the argument. 
     * 
     * @param node       the {@link Node}.
     *
     * @since           1.0
     **/
    public void processNode(Node node)
       throws Exception
    {
       String   name;
       Node  child;
       Node  sibling;

       if(node == null) return;

       // Process all children
       if(node.hasChildNodes()) { // Output all children
          child = node.getFirstChild();
          sibling = child;
          while(sibling != null) {
             name = sibling.getNodeName();
             name = igpp.util.Text.toProperCase(name);
             if(name == null) continue;

             switch(sibling.getNodeType()) {
             case Node.ELEMENT_NODE:
                // Check if element is supported.
                setMember(name, getNodeText(sibling));
                setMember(name, sibling);
             }

             // Next node      
             sibling = sibling.getNextSibling();
          }
       }
    }

    /** 
     * Call the set() method with a given name suffix and a {@link String}
     * as an argument.
     *
     * @param value      the {@link String} value to set.
     *
     * @since           1.0
     **/
    public String setMember(String name, String value)
    {
       String      member;
       String      methodName = "";
       Object[] passParam = new Object[1];
       Method   method;
       XMLParser   parent;

       parent = getMemberParent(name);
       member = getMemberName(name);
       if(parent == null) return "No parent";

       try {
          // Signature and parameters for "set" methods
          Class[]  argSig = new Class[1];
          argSig[0] = Class.forName("java.lang.String");

          methodName = "set" + igpp.util.Text.toProperCase(member);
             method = parent.getClass().getMethod(methodName, argSig);
          passParam[0] = value;            
             method.invoke(parent, passParam);
       } catch(Exception e) {
          // Ignore that the method doesn't exist
          return "error using class: " + parent.getClassName() + " and method name " + methodName;
       }        
       return parent.getClassName();
    }

	/** 
	* Call the set() method with a given name suffix and an array of {@link String}
	* values as an argument.
	*
	* @param value      the {@link String} value to set.
	*
	* @since           1.0
	**/
	public void setMember(String name, String[] value)
	{
		String      member;
		String      methodName;
		Object[] passParam = new Object[1];
		Method      method;
		XMLParser   parent;
		java.lang.String[]   base = new java.lang.String[1];
		
		parent = getMemberParent(name);
		member = getMemberName(name);
		if(parent == null) return;
		
		try {
			// Signature and parameters for "set" methods
			Class[]  argSig = new Class[1];
			argSig[0] = base.getClass();
			
			methodName = "set" +  igpp.util.Text.toProperCase(member);
			method = parent.getClass().getMethod(methodName, argSig);
			passParam[0] = value;            
			method.invoke(parent, passParam);
		} catch(Exception e) {
			e.printStackTrace();
			// Ignore that the method doesn't exist
		}        
    }
     
	/** 
	* Call the set() method with a given name suffix and a DOM {@link Node}
	* as an argument.
	*
	* @param name       the name of the member (suffix for setXXX() method).
	* @param value      the {@link String} value to set.
	*
	* @since           1.0
	**/
   public void setMember(String name, Node value)
   {
		String      member;
		String      methodName;
		Object[] passParam = new Object[1];
		Method   method;
		XMLParser   parent;
		
		parent = getMemberParent(name);
		member = getMemberName(name);
		if(parent == null) return;
		
		// Signature and parameters for "set" methods
		try {
			Class[]  argSig = new Class[1];
			argSig[0] = Class.forName("org.w3c.dom.Node");
			
			methodName = "set" + igpp.util.Text.toProperCase(member);
			method = parent.getClass().getMethod(methodName, argSig);
			passParam[0] = value;            
			method.invoke(parent, passParam);
		} catch(Exception e) {
			// Ignore that the method doesn't exist
		}        
	}

	/** 
	* Find the parent class for a member.
	* The syntax is [parent/]member
	* where "parent/" can have multiple levels.
	* If "parent" is blank, then its self-referential.
	* 
	* @param name       the XPath name for the member.
	*
	* @since           1.0
	**/
	public XMLParser getMemberParent(String name)
	{
		XMLParser parent = this;
		String[] part = name.split("/", 2);
		if(part.length > 1 && part[0].length() > 0) {
			parent = getMemberNode(part[0]);
			if(parent != null) parent = parent.getMemberParent(part[1]);
		}
		return parent;
	}
      
 /** 
	* Find the top level parent class for a member.
	* The syntax is [parent/]member
	* where "parent/" can have multiple levels.
	* If "parent" is blank, then its self-referential.
	* 
	* @param name       the XPath name for the member.
	*
	* @since           1.0
	**/
	public XMLParser getTopParent(String name)
	{
		XMLParser parent = this;
		String[] part = name.split("/", 2);
		if(part.length > 1 && part[0].length() > 0) {
			parent = getMemberNode(part[0]);
		}
		return parent;
	}
      
	/** 
	* Extract the path to a member from an XPath string.
	* The path is the text preceeding the last
	* node delimiter.
	* The syntax is [parent/]member
	* 
	* @param name       the XPath name for the member.
	*
	* @since           1.0
	**/
	public String getMemberPath(String name)
	{
		int n;
		
		n = name.lastIndexOf("/");
		if(n == -1) return "";
		
		return name.substring(0, n);
	}
      
	/** 
	* Extract the member name from an XPath string.
	* The member name is the text following the last
	* node delimiter.
	* The syntax is [parent.]member
	* 
	* @param name       the XPath name for the member.
	*
	* @since           1.0
	**/
	public String getMemberName(String name)
	{
		int n;
		
		n = name.lastIndexOf("/");
		if(n == -1) return name;
		
		return name.substring(n+1);
	}
      
	/** 
	* Locates a node in a structure based on the the "name" associated 
	* with the member. A "node" is a member of a class which also has
	* members. To be implemented in each derived class.
	* A name may have an option index. The full syntax is:
	* "name[index]"
	*
	* @param name       the name associated with a member.
	*
	* @since           1.0
	**/
	public XMLParser getMemberNode(String name)
	{
		return null;
	}
      
	/** 
	* Extracts the name portion of node reference.
	* A node reference has the syntax "name[index]".
	* The index ("[index]") is optional.
	*
	* @param name       the name associated with a member.
	*
	* @since           1.0
	**/
	public String getMemberNodeName(String name)
	{
		int n;
		
		n = name.lastIndexOf("[");
		if(n == -1) return name;
		
		return name.substring(0, n);
	}
      
	/** 
	* Extracts the index portion of node reference.
	* A node reference has the syntax "name[index]".
	* The index ("[index]") is optional. The default 
	* index is zero.
	*
	* @param name       the name associated with a member.
	*
	* @since           1.0
	**/
	public int getMemberNodeIndex(String name)
	{
		int n;
		int   j;
		
		n = name.indexOf("[");
		if(n == -1) return 0;
		
		j = name.indexOf("]");
		if(j == -1) j = name.length();
		
		return Integer.parseInt(name.substring(n+1, j));
	}
      
	/** 
	* Concatenate all the text under a node.
	* 
	* @param node       the {@link Node}.
	*
	* @since           1.0
	**/
	static public String getNodeText(Node node)
	{
		Node  child;
		Node  sibling;
		String   buffer = "";
		
		if(node.hasChildNodes()) { // Output all children
			child = node.getFirstChild();
			sibling = child;
			while(sibling != null) {
				if(sibling.getNodeType() == Node.TEXT_NODE
				|| sibling.getNodeType() == Node.CDATA_SECTION_NODE) {
					 buffer += sibling.getNodeValue();
				}
			sibling = sibling.getNextSibling();
			}
		}
		
		return entityEncode(buffer);
	}
 
 
	/** 
	* Concatenate all the text under a node.
	* 
	* @param node       the {@link Node}.
	*
	* @since           1.0
	**/
	static public String getBranchText(Node node)
	throws Exception
	{
		StringBuffer   buffer;
		int      n;
		
		if(!node.hasChildNodes()) return "";
		
		StringWriter   writer = new StringWriter();
		
		TransformerFactory transfac = TransformerFactory.newInstance();
		// Needed for Java 1.5 to work properly
		transfac.setAttribute("indent-number", new Integer(4));
		Transformer trans = transfac.newTransformer(getDefaultStyleSheet());
		
		// Set up desired output format
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		
		// create string from xml tree
		StreamResult result = new StreamResult(writer);
		DOMSource source = new DOMSource(node);
		
		trans.transform(source, result);
		
		// Strip node tags - we want just content
		buffer = writer.getBuffer();
		n = buffer.indexOf(">");
		if(n != -1) buffer = buffer.delete(0, n+1);
		
		n = buffer.lastIndexOf("<");
		if(n != -1) buffer = buffer.delete(n, buffer.length());
		
		return buffer.toString();
	}
 
	/**
	* Return the pathname of the parsed document
	*
	* @since           1.0
	**/
	public String getPathName() { return mPathName; }
     
	/**
	* Return the path to the parsed document
	*
	* @since           1.0
	**/
	public String getPath() 
	{ 
		if(mPathName.length() == 0) return "";
		
		try {
			File file = new File(mPathName); 
			return file.getParent();
		} catch(Exception e) {
		}
		return "";
	}
     
	/**
	* Return the indent value.
	*
	* @since           1.0.3
	**/
	public Integer getIndent() { return mIndent; }
     
	/**
	* Set the indent value.
	*
	* @since           1.0.3
	**/
	public void setIndent(int n) 
	{
		mIndent = n;
	}
	
	/**
	* Return a string with content enclosed in tags with the passed name.
	*
	* @since           1.0
	**/
	public String makeTagContent(String tag, String content) { return "<" + tag + ">" + content + "</" + tag + ">\n"; }
    
	/**
	 * Return a string with spaces to format to indicated indentation level.
	 *
	 * @since           1.0
	**/
	static public String indent(int level) 
	{ 
		String buffer = ""; 
		for(int i = 0; i < level; i++) buffer += "   "; 
		return buffer; 
	}
    
	/**
 	 * Prints the XML document to the currently define System.out.
 	 * Each node is labeled with its XPath.
	 *
	 * @since           1.0
	**/
	public void dump() 
	{ 
		ArrayList<Pair> list = getXPathPairs();
		Pair<String, String> pair;
		
		if(list == null) return;
		
		for(int i = 0; i < list.size(); i++) {
			pair = list.get(i);
			System.out.println(pair.getLeft() + ": " + pair.getRight());
		}
	}

	/**
 	 * Return an ArrayList of string Pairs which contains for
 	 * each node in the XML document an XPath as the "left" value
 	 * of the pair and the value as the "right" value of pair.
	 *
	 * @since           1.0
	**/
	public ArrayList<Pair> getXPathPairs() { return getXPathPairs("", 0); }
	
	/**
 	 * Return an ArrayList of string Pairs which contains for
 	 * each node in the XML document an XPath as the "left" value
 	 * of the pair and the value as the "right" value of pair.
 	 * The Prefix will be added to each path. If index is other than
 	 * zero then the prefix will be modified to include the index.
	 *
	 * @param prefix     the leading path to add to each XPath.
	 * @param index		the index of the item if part of an array.
	 *                   Indexes start a 1, a value of 0 indicates it is
	 *                   not part of an array. 
	 *
	 * @since           1.0
	**/
	public ArrayList<Pair> getXPathPairs(String prefix, int index) { return null; }
	
	/**
 	 * Return a string containing an XML representation of this instance.
	 * To be implemented in each derived class.
	 *
	 * @param n       the number of levels to indent the document.
	 *
	 * @since           1.0
	**/
	public String getXMLDocument(int n) { return getXMLDocument(n, null, 0, false); }

	/**
 	 * Return a string containing an XML representation of this instance.
	 * To be implemented in each derived class.
	 *
	 * @param n       the number of levels to indent the document.
	 * @param inUseOnly	indicates whether to include only those elements currently in use.
	 *
	 * @since           1.0
	**/
	public String getXMLDocument(int n, boolean inUseOnly) { return getXMLDocument(n, null, 0, inUseOnly); }

	/**
	* Return a string containing an XML representation of this isntance.
	* To be implemented in each derived class.
	*
	* @param n       the number of levels to indent the document.
	* @param path    the path to the element.
	* @param key     the index (key) of the element at the path.
	*
	* @since           1.0
	**/
	public String getXMLDocument(int n, String path, int key) { return getXMLDocument(n, path, key, false); }

	/**
	* Return a string containing an XML representation of this isntance.
	* To be implemented in each derived class.
	*
	* @param n       the number of levels to indent the document.
	* @param path    the path to the element.
	* @param key     the index (key) of the element at the path.
	*
	* @since           1.0
	**/
	public String getXMLDocument(int n, String path, int key, boolean inUseOnly) { return ""; }

	/**
	* Create a new member. 
	* To be implemented in each derived class.
	*
	* @param name     the name of the member to create.
	*
	* @since           1.0
	**/
	public void makeNewMember(String name)  { return; }
    
	/**
	* Create an instance of all nodes for use when editing. 
	* To be implemented in each derived class.
	*
	* @since           1.0
	**/
	public void makeEditNodes()  { return; }

	/**
	* Create all nodes for use when editing which currently 
	* do not exist.
	* To be implemented in each derived class.
	*
	* @since           1.0
	**/
	public void makeSkeletonNodes()  { return; }
    
	/**
	* Remove an elment of a member. 
	* To be implemented in each derived class.
	*
	* @param name       the name of the member to create.
	* @param index      the index of the element to remove.
	*
	* @since           1.0
	**/
	public void removeMember(String name, int index)  { return; }
    
	/**
	* Return the list of nodes of a member given the XPath like reference to the member.
	* For example, if you want a list of "AccessURL" nodes in the
	* "ResourceHeader" then the path would be "ResourceHeader.AccessURL"
	* <p>
	* Only returns nodes for items that are nodes.
	* <p>
	* Always returns a list with at least one entry. 
	* If no nodes are found then the returned entry is blank.
	*
	* @since           1.0
	**/
	public ArrayList<XMLParser> getNodes(String path)
	{
		return getNodes(path, true);
	}
 
	/**
	* Return the list of nodes of a member given the XPath like reference to the member.
	* For example, if you want a list of "AccessURL" nodes in the
	* "ResourceHeader" then the path would be "ResourceHeader.AccessURL"
	* <p>
	* Only returns nodes for items that are nodes.
	* <p>
	* If no nodes are found then the returned list is empty
	* unless alwaysOne is set to true.
	*
	* @since           1.0
	**/
	public ArrayList<XMLParser> getNodes(String path, boolean alwaysOne)
	{
		String      member;
		String      name;
		XMLParser   parent;
		ArrayList<XMLParser> list;
		
		parent = getMemberParent(path);
		member = getMemberName(path);
		name = getMemberNodeName(member);
		
		list = getNodes(parent, name);
		
		if(alwaysOne) {
			try {
				if(list.size() == 0) list.add(parent.getClass().newInstance());
			} catch(Exception e) {
				// Ignore errors
			}
		}
		
		return list;
	}

	/**
	* Return the list of nodes given the name of the member.
	* <p>
	* Only returns nodes for items that are nodes.
	* <p>
	* If no nodes are found then the returned list is empty.
	*
	* @since           1.0
	**/
	public ArrayList<XMLParser> getNodes(XMLParser parent, String name)
	{
		String   methodName = "";
		Method   method;
		Object   response;
		ArrayList value;
		ArrayList<XMLParser> list = new ArrayList<XMLParser>();
		XMLParser	item;
		
		if(parent == null) return list;
	
		try {
			// Signature and parameters for "get" method
			methodName = "get" + igpp.util.Text.toProperCase(name);
			method = parent.getClass().getMethod(methodName);
			response = method.invoke(parent);
			if(response instanceof XMLParser) { 
				item = (XMLParser) response; 
				list.add(item);
			}
			if(response instanceof ArrayList) { 
				value = (ArrayList) response;
				if(value.size() > 0 && value.get(0) instanceof XMLParser) {
					for(int i = 0; i < value.size(); i++) {
						item = (XMLParser) value.get(i);
						list.add(item);
					}
				}
			}
		} catch(Exception e) {
			// Ignore that the method doesn't exist
			// return "error using class: " + parent.getClassName() + " and method name " + methodName;
		}
		return list;
	}
     
 /**
  * Return the list of values of a member given the XPath like reference to the member.
  * The XPath is a canonical path which includes node names only (no array indexes)
  * For example, if you want all instances of the "URL" in "AccessURL" under
  * "ResourceHeader" then the path would be "ResourceHeader/AccessURL/URL"
  * <p>
  * Only returns values for items that return {@link String} values.
  * <p>
  * Always returns a list with at least one entry. 
  * If no nodes are found then the returned entry is blank.
  *
  * @since           1.0
  **/
 public ArrayList<String> getAllValues(String path)
 {
    XMLParser   item;
    ArrayList<XMLParser>   parent;
    ArrayList<String> value = new ArrayList<String>();
    
    String[] part = path.split("/", 2);
    if(part.length > 1 && part[0].length() > 0) {	// If a parent and its a node, then run on decendent
       parent = getNodes(part[0], false);
       if(parent.size() != 0) {	// Process each node
	       for(int i = 0; i < parent.size(); i++) {
	       	item = parent.get(i);
	       	value.addAll(item.getAllValues(part[1]));
	       }
	       return value;
       }
    }
    
    // If we reach here then we have a member to process
    value.addAll(getValues(path, false));
    return value;
  }

 /**
  * Return the list of values of a member given the XPath like reference to the member.
  * For example, if you want the "URL" in the second "AccessURL" item in the
  * "ResourceHeader" then the path would be "ResourceHeader/AccessURL[1]/URL"
  * <p>
  * Only returns values for items that return {@link String} values.
  * <p>
  * Always returns a list with at least one entry. 
  * If no nodes are found then the returned entry is blank.
  *
  * @since           1.0
  **/
 public ArrayList<String> getValues(String path)
 {
 	return getValues(path, true);
 }
 
 /**
  * Return the list of values of a member given the XPath like reference to the member.
  * For example, if you want the "URL" in the second "AccessURL" item in the
  * "ResourceHeader" then the path would be "ResourceHeader/AccessURL[1]/URL"
  * <p>
  * Only returns values for items that return {@link String} values.
  * <p>
  * If no nodes are found then the returned list is empty
  * unless alwaysOne is set to true.
  *
  * @since           1.0
  **/
 public ArrayList<String> getValues(String path, boolean alwaysOne)
 {
    String      member;
    String      methodName = "";
    Object   response;
    Method   method;
    XMLParser   parent;
    ArrayList<String> value = new ArrayList<String>();
    ArrayList   list;
    String      buffer;
    
     parent = getMemberParent(path);
     member = getMemberName(path);
     if(parent == null) { value.add(""); return value; }
    
     try {
        // Signature and parameters for "get" method
        methodName = "get" + igpp.util.Text.toProperCase(member);
        method = parent.getClass().getMethod(methodName);
        response = method.invoke(parent);
        if(response instanceof String) { 
          buffer = (String) response; 
          value.add(buffer);
        }
        if(response instanceof ArrayList) { 
          list = (ArrayList) response;
          if(list.size() > 0 && list.get(0) instanceof String) {
             for(int i = 0; i < list.size(); i++) {
                buffer = (String) list.get(i);
                value.add(buffer);
             }
          } 
        }
     } catch(Exception e) {
        // Ignore that the method doesn't exist
        // return "error using class: " + parent.getClassName() + " and method name " + methodName;
     }
     if(alwaysOne && value.size() == 0) value.add("");

     return value;
  }


	/**
	* Return a {@link StringReader} for an XML representation of the document.
	* Commonly used to pass the document through an XML stylesheet transformation
	* using {@link Transform}.
	*
	* @since           1.0
	**/
	public StringReader getStringReader()
	{
		return new StringReader(getXMLDocument(0));
	}
	
	/**
	* Return a value enclosed in a tag and properly indented.
	*
	* @since           1.0
	**/
	static public String getTaggedValue(int level, String tagName, String value)
	{
		if(value == null) value = "";
	 	return indent(level) + "<" + tagName + ">" + value + "</" + tagName + ">\n";
	}
       
	/**
	* Return a value enclosed in a tag and properly indented. If inUseOnly is true then
	* only those elements that are required or have values defined are returned.
	*
	* @since           1.0
	**/
	public String getTaggedValue(int level, String tagName, String value, boolean inUseOnly)
	{
		if(!inUseOnly) return getTaggedValue(level, tagName, value);
		
		if(isInUse(tagName, value)) {
		 	return getTaggedValue(level, tagName, value);
		}
		
		return "";
	}
       
	/**
	 * Return an open tag with the proper indentation.
	 *
	 * @since           1.0
	**/
	static public String getTagOpen(int level, String tagName)
	{
		return indent(level) + "<" + tagName + ">\n";
	}
       
	/**
	 * Return an close tag with the proper indentation.
	 *
	 * @since           1.0
	**/
	static public String getTagClose(int level, String tagName)
	{
		return indent(level) + "</" + tagName + ">\n";
	}
       
	/**
	 * Return a list of tagged values, properly indented.
	 *
	 * @since           1.0
	**/
	static public String getTaggedList(int level, String tagName, ArrayList<String> list)
	{
		Iterator i;
		String   buffer = "";
		
		i = list.iterator();
		while(i.hasNext()) {
			buffer += getTaggedValue(level, tagName, (String) i.next());
		}
		return buffer;
	}
 
	/**
	 * Return a list of tagged values, properly indented. If inUseOnly is true then
	 * only those elements that are required or have values defined are returned.
	 *
	 * @since           1.0
	**/
	public String getTaggedList(int level, String tagName, ArrayList<String> list, boolean inUseOnly)
	{
		Iterator i;
		String   buffer = "";
		
		i = list.iterator();
		while(i.hasNext()) {
			buffer += getTaggedValue(level, tagName, (String) i.next(), inUseOnly);
		}
		return buffer;
	}
 
	/**
	* Construct an abbreviated XPath for a a class node. A path has the form:
	*<br>
	*  parent/className[key]
	*<br>
	*where "parent/" and "[key]" is optional
	*
	* @since           1.0
	**/
    public String getElementPath(String base, int key) 
    {
       String   path = "";
       String   delim = "";
       
       if(key == -2) return base; // Don't alter base
       
       if(base == null) return null;
       path = base;
       
       if(base.length() > 0) delim = "/";
       // path += delim + toImproperCase(getClassName());
       path += delim + getClassName();
       
       if(key != -1) path += "[" + key + "]";
       
       return path;
    }

	/**
	* Determine if a word is a common word. 
	*
	* @since           1.0
	 **/
	public boolean isCommonWord(String value) 
	{
		if(value.length() < 2) return true;	// Any one-letter word is "common"
		if(Character.isDigit(value.charAt(0))) return true;	// Starts with a digit
		
		if(value.compareToIgnoreCase("an") == 0) return true;
		if(value.compareToIgnoreCase("and") == 0) return true;
		if(value.compareToIgnoreCase("are") == 0) return true;
		if(value.compareToIgnoreCase("at") == 0) return true;
		if(value.compareToIgnoreCase("but") == 0) return true;
		if(value.compareToIgnoreCase("by") == 0) return true;
		if(value.compareToIgnoreCase("for") == 0) return true;
		if(value.compareToIgnoreCase("he") == 0) return true;
		if(value.compareToIgnoreCase("in") == 0) return true;
		if(value.compareToIgnoreCase("is") == 0) return true;
		if(value.compareToIgnoreCase("it") == 0) return true;
		if(value.compareToIgnoreCase("our") == 0) return true;
		if(value.compareToIgnoreCase("own") == 0) return true;
		if(value.compareToIgnoreCase("of") == 0) return true;
		if(value.compareToIgnoreCase("on") == 0) return true;
		if(value.compareToIgnoreCase("or") == 0) return true;
		if(value.compareToIgnoreCase("she") == 0) return true;
		if(value.compareToIgnoreCase("the") == 0) return true;
		if(value.compareToIgnoreCase("that") == 0) return true;
		if(value.compareToIgnoreCase("to") == 0) return true;
		if(value.compareToIgnoreCase("we") == 0) return true;
		if(value.compareToIgnoreCase("was") == 0) return true;
		if(value.compareToIgnoreCase("you") == 0) return true;
		if(value.compareToIgnoreCase("your") == 0) return true;
		
		return false;
	}
	 
   /** 
    * Parse a string into words and return a list of unique words.
    * Common words, such as "a", "the", etc., are not included in the list.
    * 
    * @since           1.0
    **/
   public ArrayList<String> parseWords(String text)
   {
		String	delimiters = "[ \\t\\.,;:?!@#$%(){}'\"/_]";
		ArrayList<String>	words = new ArrayList<String>();
		String[]	split;
		
		String[]	part = text.split(delimiters);
		
		for(int i = 0; i < part.length; i++) {
			split = splitMixed(part[i]);	// Split Mixed case words
			for(int j = 0; j < split.length; j++) {
				if(isCommonWord(part[i])) continue;
				if(! igpp.util.Text.isInList(part[i], words)) words.add(part[i]);
			}
		}
		
		return words;
   }
   
   /**
    * Divide a string on capital letters that follow lowercase letters
    *	
    * @since           1.0
    **/
   public String[] splitMixed(String text)
   {
   	boolean isLower = false;
   	int	n = 0;
   	char	c;
   	ArrayList<String> list = new ArrayList<String>();
   	
   	for(int i = 0; i < text.length(); i++) {
   		c = text.charAt(i);
   		if(isLower && Character.isUpperCase(c)) {
   			list.add(text.substring(n, i));
   			n = i;
   		}
   		if(Character.isLowerCase(c)) isLower = true;
   		else isLower = false;
   	}
   	if(n < text.length()) list.add(text.substring(n));
   	
   	return list.toArray(new String[0]);
   }
   
   /** 
    * Walk the internals of a class and collect a unique list
    * of words from all String fields. By convention the first letter
    * of each member field starts with "m" so we drop the first letter
    * when using the get() utility methods (i.e., getValues())
    * We also exclude anything that ends with "ID" since there are
    * identifiers and have no language relevance.
    * 
    * @since           1.0
    **/
   public ArrayList<String> getWords()
   {
		String	buffer = "";
		String	baseName = "";
		ArrayList<String> value;
		ArrayList<XMLParser> node;
		XMLParser	xmlParserType = new XMLParser();
		String 		stringType = "";
		ArrayList	arrayListType = new ArrayList();
		ArrayList<String>	words = new ArrayList<String>();
		ArrayList<String>	newWords;
		ArrayList<String>	part;
		
		Field[] field = this.getClass().getDeclaredFields();
		for(int i = 0; i < field.length; i++) {
	      baseName = field[i].getName().substring(1);	// Drop first letter (always "m")
	      if(baseName.endsWith("ID")) continue;	// Skip items that end with "ID"
			if(field[i].getType().isInstance(stringType)) {	// String values
				value = this.getValues(baseName, false);
				for(int j = 0; j < value.size(); j++) {
					buffer = value.get(j).toUpperCase();
					part = parseWords(buffer);
					for(int n = 0; n < part.size(); n++) {
						if(! igpp.util.Text.isInList(part.get(n), words)) words.add(part.get(n));
					}
				}
			} else if(field[i].getType().isInstance(arrayListType)) {	// Arrays
				// Try as array of strings
				value = this.getAllValues(baseName);
				for(int j = 0; j < value.size(); j++) {
					buffer = value.get(j).toUpperCase();
					part = parseWords(buffer);
					for(int n = 0; n < part.size(); n++) {
						if(! igpp.util.Text.isInList(part.get(n), words)) words.add(part.get(n));
					}
				}
				// Try as array of nodes
				node = this.getNodes(baseName, false);
				for(int j = 0; j < node.size(); j++) {
					newWords = node.get(j).getWords();
					for(int n = 0; n < newWords.size(); n++) {
						if(! igpp.util.Text.isInList(newWords.get(n), words)) words.add(newWords.get(n));
					}
				}
			} else if(field[i].getType().getSuperclass().isInstance(xmlParserType)) {	// XMLParser
				node = this.getNodes(baseName, false);
				for(int j = 0; j < node.size(); j++) {
					newWords = node.get(j).getWords();
					for(int n = 0; n < newWords.size(); n++) {
						if(! igpp.util.Text.isInList(newWords.get(n), words)) words.add(newWords.get(n));
					}
				}
			}
		}
		return words;
	}	 
		
   /** 
    * Determine if an element is in use. An element is in use if the value in
    * not empty or the element is required.
    * 
    * @param name      the name of the element.
    * @param value     the value associated with the element.
    *
    * @since           1.0
    **/
   public boolean isInUse(String name, String value)
   {
   	if(value.length() > 0) return true;
   	return isRequired(name);
   }
   
   /** 
    * Determine if an element is required. An element is required is it is in
    * the list of required elements (see setRequired())
    * 
    * @param name      the name of the element.
    *
    * @since           1.0
    **/
   public boolean isRequired(String name)
   {
   	return igpp.util.Text.isInList(name, mRequired);
   }
   
   /** 
    * Get a list of values labeled with an XPath.
    * With XPath the first item has index of 1.
    * 
	* @param prefix     the XPath to the desired members.
	* @param list       the list of member names to retrieve.
    *
    * @since           1.0
    **/
   static public ArrayList<Pair> getXPathList(String prefix, ArrayList<String> list)
   {
   	ArrayList<Pair> pairList = new ArrayList<Pair>();

      for(int i = 0; i < list.size(); i++) {
	   	Pair<String, String> pair = new Pair<String, String>(prefix + "[" + (i+1) + "]", list.get(i));
	   	pairList.add(pair);
      }
      return pairList;
   }

	/**
	 * Returns the header line required for XML files.
	 *
	 **/
	static public String getXMLHeader()
	{
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	}
	
	/**
	 * Convert special characters in a string for use in an
	 * HTML document. Converts only the special ASCII characters of &, ", ', < and > 
	 * to the corresponding HTML entities .
	 *
	 **/
	public static String entityEncode(String text)
	{
		String	buffer;
		
		if(text == null) return text;
		
		buffer = text;
		buffer = buffer.replace("&", "&amp;");
		buffer = buffer.replace("\"", "&quot;");
		buffer = buffer.replace("'", "&#39;");
		buffer = buffer.replace("<", "&lt;");
		buffer = buffer.replace(">", "&gt;");
		
		return buffer;
	}

	public void setClassName(String name) { mClassName = name; }
	public String getClassName() { return mClassName; }
    
	public void addRequired(String value) { mRequired.add(value); }
	public ArrayList getRequired() { return mRequired; }
}
