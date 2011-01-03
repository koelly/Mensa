package de.koelly.mensa2;

import java.text.ParseException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MensaHandler extends DefaultHandler {
	
	private boolean document = false;
	private boolean date = false;
	private boolean key = false;
	private boolean dayofweek = false;
	private boolean typeofmeal = false;
	private boolean nameofmeal = false;
	private boolean price4students = false;
	private boolean price4clerks = false; 
	private boolean price4externs = false;
	
	private String _date;
	private String _key;
	private String _dayofweek;
	private String _typeofmeal;
	private String _nameofmeal;
	private int _price4students;
	private int _price4clerks; 
	private int _price4externs;
	
	
	ArrayList<MealDataSet> data = new ArrayList<MealDataSet>();

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    	if (localName.equals("document")) {
            this.document = true;
    	}else if (localName.equals("date")) {
            this.date = true;
            this._date = atts.getValue("date");
    	}else if (localName.equals("key")) {
            this.key = true;
            this._key = atts.getValue("key");
    	}else if (localName.equals("dayofweek")) {
            this.dayofweek = true;
    	}else if (localName.equals("typeofmeal")){
            this.typeofmeal = true;
            this._typeofmeal = atts.getValue("type");
    	}else if (localName.equals("nameofmeal")) {
            this.nameofmeal = true;
    	}else if (localName.equals("price4students")) {
            this.price4students = true;
    	}else if (localName.equals("price4clerks")) {
            this.price4clerks = true;
    	}else if (localName.equals("price4externs")) {
            this.price4externs = true;
    	}
    }
    
    
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    	if (localName.equals("document")) {
            this.document = false;
    	}else if (localName.equals("date")) {
            this.date = false;
            try {
				data.add(new MealDataSet(_date, qName, _dayofweek, _typeofmeal, _nameofmeal, _price4students, _price4clerks, _price4externs));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}else if (localName.equals("key")) {
            this.key = false;
    	}else if (localName.equals("dayofweek")) {
            this.dayofweek = false;
    	}else if (localName.equals("typeofmeal")){
            this.typeofmeal = false;
    	}else if (localName.equals("nameofmeal")) {
            this.nameofmeal = false;
    	}else if (localName.equals("price4students")) {
            this.price4students = false;
    	}else if (localName.equals("price4clerks")) {
            this.price4clerks = false;
    	}else if (localName.equals("price4externs")) {
            this.price4externs = false;
    	}    	
    }
    
	/** Gets be called on the following structure:
     * <tag>characters</tag> */
    @Override
    public void characters(char ch[], int start, int length) {
    	if(this.dayofweek){
    		this._dayofweek = new String(ch, start, length);
   	 	} else if(this.nameofmeal){
   	 		this._nameofmeal = new String(ch, start, length);
   	 		Log.d("TESTXXXXX",""+this._nameofmeal);
   	 	} else if(this.price4students){
   	 		this._price4students = Integer.parseInt(new String(ch, start, length));
   	 	} else if(this.price4clerks){
   	 		this._price4clerks = Integer.parseInt(new String(ch, start, length));
   	 	} else if(this.price4externs){
	 		this._price4externs = Integer.parseInt(new String(ch, start, length));
   	 	}
    }
    
    @Override
    public void startDocument() throws SAXException {
    	 Log.d("SAX startDocument: ","Aufgerufen");
    }

    @Override
    public void endDocument() throws SAXException {
            // Do some finishing work if needed
    	Log.d("SAX endDocument: ", "Aufgerufen");
    }
    
    public ArrayList<MealDataSet> getParsedData(){
    	return data;
    }
}
