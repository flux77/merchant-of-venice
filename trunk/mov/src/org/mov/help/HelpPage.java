/* Merchant of Venice - technical analysis software for the stock market.
   Copyright (C) 2002 Andrew Leppard (aleppard@picknowl.com.au)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
*/

package org.mov.help;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.swing.tree.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class HelpPage extends DefaultMutableTreeNode {

    // Location of help docs
    private final static String BASE_PATH = "org/mov/help/doc/";
    private final static String INDEX_DOCUMENT = "org/mov/help/doc/index.xml";
    
    private String name;
    private String link;
    private String text;
    private boolean isLoaded;

    public HelpPage(String name) {
        super(name);
        this.name = name;
        this.link = nameToLink(name);

        isLoaded = false;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public String getText() {
        // Make sure page is loaded
        loadText();

        return text;
    }

    public HelpPage findPageWithLink(String link) {
        for(Enumeration enumeration = preorderEnumeration(); 
            enumeration.hasMoreElements();) {
            HelpPage page = 
                (HelpPage)enumeration.nextElement();

            if(page.getLink().equals(link))
                return page;
        }
        return null;

    }

    private String nameToLink(String name) {
        String link = name.concat(".html");
        
        return link;
    }

    private void loadText() {

        if(!isLoaded) {
            String fileName = BASE_PATH.concat(link);            
            StringBuffer stringBuffer = new StringBuffer();

            // Read file
            try {
                FileReader fr = new FileReader(fileName);
                BufferedReader br = new BufferedReader(fr);		
                
                // ... one line at a time
                String line = br.readLine();
                
                while(line != null) {
                    stringBuffer = stringBuffer.append(line);
                    line = br.readLine();                
                }
                
                br.close();
                fr.close();
            }
            catch(java.io.IOException e) {
                text = "<html><h2>Sorry, help page is missing!</h2></html>";
                return;
            }
            
            text = stringBuffer.toString();
            isLoaded = true;
        }
    }    

    public static HelpPage loadIndex() {
        HelpPage index = null;
        Document document = loadIndexDocument();

        if(document != null) {
            index = new HelpPage("Venice");
            Element root = document.getDocumentElement();
            
            buildIndex(index, root);
        }

        if(index == null)
            index = new HelpPage("Error loading index!");

        return index;
    }

    private static void buildIndex(HelpPage index, Element root) {
        Node node = root.getFirstChild();
       
        while(node != null) {

            // Skip text, comment nodes etc
            if(node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element)node;

                // Make sure it's correclty formed
                assert element.getNodeName().equals("chapter");
                assert element.hasAttribute("name");
                
                HelpPage page = new HelpPage(element.getAttribute("name"));
                index.add(page);
                buildIndex(page, element);
            }
            
            node = node.getNextSibling();
        }
    }

    private static Document loadIndexDocument() {
        Document document = null;

        try {
            URL fileURL = ClassLoader.getSystemResource(INDEX_DOCUMENT);

            if(fileURL != null) {
                File file = new File(fileURL.getFile());

                DocumentBuilderFactory documentBuilderFactory = 
                    DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = 
                    documentBuilderFactory.newDocumentBuilder();
            
                document = documentBuilder.parse(file);
            }
        }
        
        // We don't care about all these individual messages. We can't deal
        // with them all. We only care about dealing with two cases: It either
        // loaded or it didn't. If it didn't, return null. 
        catch(IOException i) { }
        catch(DOMException d) { }
        catch(ParserConfigurationException p) { }
        catch(SAXException e) { }

        return document;
    }
}
