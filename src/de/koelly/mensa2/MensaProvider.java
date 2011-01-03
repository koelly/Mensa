package de.koelly.mensa2;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class MensaProvider {
	
	ArrayList <MealDataSet> data;
	MensaHandler myMensaHandler = new MensaHandler();
	
	public ArrayList<MealDataSet> getData(URL _url){
  		
		SAXParserFactory spf = SAXParserFactory.newInstance();    		
		SAXParser sp;
		try {
			
			
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			
			MensaHandler myMensaHandler = new MensaHandler();
			
			xr.setContentHandler(myMensaHandler);
			
			InputSource isource = new InputSource(_url.openStream());
			isource.setEncoding("UTF-8");
			
			xr.parse(isource);
			
			data = myMensaHandler.getParsedData();
			 
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		
		return data;
	}

}
