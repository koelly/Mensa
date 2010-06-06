package de.koelly.mensa2;
/**
 * Mensa
 * 
 * Die Mensa App zeigt den Speiseplan der aktuellen Woche
 * der HS Landshut an.
 * Geplant ist die Anzeige aller Mensen des Studentenwerks
 * Niederbayern/Oberpfalz
 * 
 * VORSICHT: Extrem schlechter Code!
 * Macht Kopfweh! Möglichst erst in ein paar Monaten 
 * komplett durchlesen!
 * 
 * Copyright (C) 2010 Christopher Köllmayr
 * This program is free software; 
 * you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License 
 * as published by the Free Software Foundation; 
 * either version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General 
 * Public License along with this program; 
 * if not, see http://www.gnu.org/licenses/.
 * 
 * Github Link: git@github.com:koelly/Mensa.git
 * 
 * @author      Christopher köllmayr
 * @version     0.0.11
 * @licence		GPL v3
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SimpleAdapter;




public class mensa2 extends ListActivity  implements OnClickListener
{
	// DB Handler
	MyDBHelper mdh = new MyDBHelper();
	
	// Tabellen für die verschiedenen Mensen
	private static final String TBL_LA  = "tbl_la";
	private static final String TBL_R   = "tbl_r";
	private static final String TBL_PA  = "tbl_pa";
	private static final String TBL_DEG = "tbl_deg";
	
    //CSV URLS
    String urlLaCur  = "http://www.stwno.de/splan/spldw.csv";
    String urlLaNex  = "http://www.stwno.de/splan/splnw.csv";
    String urlPaCur  = "http://www.stwno.de/splan/sppdw.csv";
    String urlPaNex  = "http://www.stwno.de/splan/sppnw.csv";
    String urlRCur   = "http://www.stwno.de/splan/sprdw.csv";
    String urlRNex   = "http://www.stwno.de/splan/sprnw.csv";
    String urlDegCur = "http://www.stwno.de/splan/spddw.csv";
    String urlDegNex = "http://www.stwno.de/splan/spddw.csv";
	
    // Speicherort der Einstellungen
    final String PREFERENCES = "mensa.prf";
	
	public class MyDBHelper {
		private static final String DB_NAME = "mensa.db";
		SQLiteDatabase DB = null;

		public void createDB() {
			try {
				DB = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
			} finally {
				
			}
		}

		public void createTable(String tableName){
			DB.execSQL("CREATE TABLE IF NOT EXISTS "
	                + tableName
	                + " (id integer AUTO_INCREMENT PRIMARY KEY, date VARCHAR(10), type VARCHAR, name VARCHAR, p_stud VARCHAR, p_clerk VARCHAR, p_guest VARCHAR);"
	                );
			Log.d("Mensa", "Tabelle " + tableName + " erstellt");
		}
		
		public void dropTable(String tableName){
	    	DB.execSQL("DROP TABLE IF EXISTS " + tableName + ";");
	    	Log.d("Mensa", "Tabelle " + tableName + " gelöscht");
		}
		
		public void insert(String tableName, String date, String type, String name, String p_stud, String p_clerk, String p_guest){
			ContentValues werte =  new ContentValues();
				werte.put("date", date);
				werte.put("type", type);
				werte.put("name", name);
				werte.put("p_stud", p_stud);
				werte.put("p_clerk", p_clerk);
				werte.put("p_guest", p_guest);
				DB.insert(tableName, null, werte);
				Log.d("Mensa", "Gericht " + name + " in die Tabelle " + tableName + " eingefuegt");
		}
		
		public boolean dateInDB(String date2search, String tableName){
			Cursor cDateInDB = DB.query(
				false, 				// distinct?
				tableName,
				new String[] { 		// SELECT
				    "date",
				    },
				  "date = ?", 		// WHERE-Bedingung
				  new String[] { 	// Parameter für WHERE
				        date2search
				  },
				  null,       		// GROUP BY
				  null,       		// HAVING
				  null, 			// ORDER BY
				  null       		// LIMIT
				);
	        
	        if(cDateInDB.getCount() > 0){
	        	cDateInDB.close();	
	        	return true;
	        } else {
	        	cDateInDB.close();
	        	return false;
	        }
		}
		
		public ArrayList<HashMap<String, String>> getData(String tableName, String date){
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> item;
			list.clear();
			
			Cursor cData = DB.query(
					  false, 			// distinct?
					  tableName,
					  new String[] { 	// SELECT
					    "date",
					    "name",
					    "p_stud",
					    "p_clerk",
					    "p_guest"
					    },
					  "date = ?", 		// WHERE-Bedingung
					  new String[] { 	// Parameter für WHERE
					        date
					  },
					  null,       		// GROUP BY
					  null,       		// HAVING
					  null, 			// ORDER BY
					  null       		// LIMIT
					);

			int name = cData.getColumnIndex("name");
			int p_stud = cData.getColumnIndex("p_stud");
			int p_clerk = cData.getColumnIndex("p_clerk");
			int p_guest = cData.getColumnIndex("p_guest");
			
			cData.moveToFirst();
			if (cData != null) {
				if(cData.isFirst()){
						do {
							String meal = cData.getString(name);
							String pr_stud = cData.getString(p_stud);
							String pr_clerk = cData.getString(p_clerk);
							String pr_guest = cData.getString(p_guest);
							item = new HashMap<String, String>();
							item.put("name", meal );
							item.put("price", "Stud: " + pr_stud + "€ | Angest: " + pr_clerk + "€ | Gast: " + pr_guest + "€" );
							list.add(item);

						} while(cData.moveToNext());
					} 
				} 
			
			
			return list;
			
		}
		
		public void closeDB(){
			DB.close();
		}
		
	}
	
	public class MyDownloadhelper{

		/**
		 * Öffnet eine URL und übergibt den Text in einen InputStream
		 * Quelle: http://www.devx.com/wireless/Article/39810/1954
		 * 
		 * @param	urlString	Ort der herunterzuladenden Datei
		 * @return				InputStream  des Inhalts	
		 */
		private InputStream OpenHttpConnection(String urlString) throws IOException
		    {
				Log.d("Mensa", "Daten von " + urlString + " werden geholt...");
		        InputStream in = null;
		        int response = -1;
		               
		        URL url = new URL(urlString); 
		        URLConnection conn = url.openConnection();
		                 
		        if (!(conn instanceof HttpURLConnection))                     
		            throw new IOException("Not an HTTP connection");
		        
		        try{
		            HttpURLConnection httpConn = (HttpURLConnection) conn;
		            httpConn.setAllowUserInteraction(false);
		            httpConn.setInstanceFollowRedirects(true);
		            httpConn.setRequestMethod("GET");
		            httpConn.connect(); 

		            response = httpConn.getResponseCode();                 
		            if (response == HttpURLConnection.HTTP_OK) {
		                in = httpConn.getInputStream();                                 
		            }                     
		        }
		        catch (Exception ex)
		        {
		            throw new IOException("Error connecting");            
		        }
		        return in;     
		    }
			
		/**
		 * Lädt den Text in einen String
		 * Quelle: http://www.devx.com/wireless/Article/39810/1954
		 * 
		 * @param	URL		URL der herunterzuladenden Datei
		 * @return			String des Inhalts	
		 */	
		private String DownloadText(String URL) throws UnsupportedEncodingException
	    {
	        int BUFFER_SIZE = 2000;
	        InputStream in = null;
	        try {
	            in = OpenHttpConnection(URL);
	        } catch (IOException e1) {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
	            return "";
	        }
	        Log.d("Mensa", "Daten aus " + URL + " werden in String gelesen");
	        InputStreamReader isr = new InputStreamReader(in, "ISO-8859-1");
	        int charRead;
	          String str = "";
	          char[] inputBuffer = new char[BUFFER_SIZE];          
	        try {
	            while ((charRead = isr.read(inputBuffer))>0)
	            {                    
	                //---convert the chars to a String---
	                String readString = String.copyValueOf(inputBuffer, 0, charRead);                    
	                str += readString;
	                inputBuffer = new char[BUFFER_SIZE];
	            }
	            in.close();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	            return "";
	        }    
	        return str;        
	    }

		/**
		 * Lädt den Text in in die DB
		 * Quelle: http://www.devx.com/wireless/Article/39810/1954
		 * 
		 * @param	URL			URL der herunterzuladenden Datei
		 * @param	mdh			DB Helper vom Typ MyDBHelper
		 * @param	tableName	Tabellenname in den gespeichert werden soll	
		 */
		public void parse2db(String URL, MyDBHelper mdh, String tableName){
			String [][] values = new String [200][10];
			String str = null;
			
			try {
				str = DownloadText(URL);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
			int lineNumber = 0, tokenNumber = 0;
			StringTokenizer lineToken = new StringTokenizer(str, "\n");						// Jede Zeile
			if (str.length() > 1){
				while (lineToken.hasMoreTokens()){
					lineNumber++;
					StringTokenizer st = new StringTokenizer(lineToken.nextToken(), ";");	// Jeder Token der Zeile
					tokenNumber = 0;
					while(st.hasMoreTokens()){
						values[lineNumber][tokenNumber] =  st.nextToken();
						tokenNumber++;
					}if (values[lineNumber][4] == null){
						lineNumber--;
					} else {
						mdh.insert(tableName, values[lineNumber][0], values[lineNumber][2], values[lineNumber][3], values[lineNumber][5], values[lineNumber][6], values[lineNumber][7]);
					}
				}
			}
			
		}
	}
	
	public class MyDateHelper{
		public String getCustomDate(String startdate, int i){
			Date d = null;
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			Calendar c1 = Calendar.getInstance(); 
			
			try {
				d = formatter.parse(startdate);
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			c1.setTime(d);	
			

			 
			c1.add(Calendar.DATE,i);
			Log.d("Mensa: ", "Selected date is : " + formatter.format(c1.getTime()));
			String date = formatter.format(c1.getTime());
			return date;
		}
		
		public String getToday(){
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			Calendar c1 = Calendar.getInstance(); 
			String today = formatter.format(c1.getTime());
			return today;
		}
	
		/**
		 * Gibt String mit Abkürzung des aktuellen 
		 * Tages zurück.
		 * Montag --> Mo
		 * 
		 * @param	day		Datum im Format dd.mm.yyyy
		 * @return			String mit Abkürzung des Wochentages	
		 * @throws			java.text.ParseException 
		 */
		@SuppressWarnings("unused")
		private String getDayName(String day) throws java.text.ParseException{
			 SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			 Calendar c1 = Calendar.getInstance(); 
			 Date d;
			 try {
				 d = formatter.parse(day);
				 c1.setTime(d);
			 } catch (ParseException e) {
				// TODO Auto-generated catch block
				 e.printStackTrace();
			 }
			 SimpleDateFormat formatter2 = new SimpleDateFormat("E", Locale.GERMAN);
			 return formatter2.format(c1.getTime()); 
		}
	}

	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

	protected void onResume(){
    	super.onResume();
    	
        SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        boolean firstRun = settings.getBoolean("FirstRun", true);
        
        if (firstRun){
        	mdh.createDB();
        	mdh.createTable(TBL_LA);
        	mdh.createTable(TBL_PA);
        	mdh.createTable(TBL_R);
        	mdh.createTable(TBL_DEG);
        	
        	SharedPreferences.Editor settingsEditor = settings.edit();
            settingsEditor.putBoolean("FirstRun", false);
            settingsEditor.commit();
        }

        //mdh.createDB();
        //mdh.createTable(TBL_LA);
        MyDownloadhelper download = new MyDownloadhelper();
        download.parse2db(urlLaCur, mdh, TBL_LA);
        
        MyDateHelper myDate = new MyDateHelper();
		
        ArrayList<HashMap<String, String>> list = mdh.getData(TBL_LA, myDate.getToday());
        SimpleAdapter UI = new SimpleAdapter(this, list, R.layout.list_item, new String[] { "name", "price" }, new int[] {R.id.name, R.id.price });
        setListAdapter(UI);
	}


    protected void onPause(){
    	super.onPause();	
    }
    
    protected void onStop(){
    	super.onStop();
    }

	@Override
	public void onClick(View v) {
	}
    

}

