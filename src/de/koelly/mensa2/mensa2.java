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
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;




public class mensa2 extends ListActivity  implements OnClickListener
{
	// Tabellen für die verschiedenen Mensen
	private static final String TBL_LA  = "tbl_la";
	private static final String TBL_R   = "tbl_r";
	private static final String TBL_PA  = "tbl_pa";
	private static final String TBL_DEG = "tbl_deg";
	
	private static final String DB_NAME = "mensa.db";
	SQLiteDatabase DB = null;

	// Menüpunkte
	private final int REFRESH = 0;
	private final int QUIT = 1;
	private final int CHANGE_MENSA = 2;
	private final int NEXT_DAY = 3;
	private final int PREV_DAY = 4;
	private final int LA = 5;
	private final int REG = 6;
	private final int DEG = 7;
	private final int PA = 8;
	
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
	boolean firstRun; 
	String location;	// Ort der gewählten Mensa
    String selectedTable = null;
    String[] urls = new String[2];
    String selectedDate;
	
	// Diverses...
	ProgressDialog myProgressDialog = null;
	
	
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
			
			cData.close();
			return list;
			
		}
		
	public void closeDB(){
			DB.close();
		}
		

	

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
	public void parse2db(String URL, String tableName){
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
						insert(tableName, values[lineNumber][0], values[lineNumber][2], values[lineNumber][3], values[lineNumber][5], values[lineNumber][6], values[lineNumber][7]);
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
	
	    
	public void setLocaleUI(){

		
	}
	
	public void drawUI(String selectedDate){
		
		//TODO Preferences werden nicht sofort geschrieben, sondern noch alte gelesen :-/
		MyDateHelper myDate = new MyDateHelper();
		createDB();
    	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        location = settings.getString(location, "la");
		
		
		if (location.equalsIgnoreCase("la")){
	    	selectedTable = TBL_LA;
	    	urls[0] = urlLaCur;
	    	urls[1] = urlLaNex;
	    	setTitle("Mensa Landshut");
	    } else if (location.equalsIgnoreCase("r")) {
	    	selectedTable = TBL_R;
	    	urls[0] = urlRCur;
	    	urls[1] = urlRNex;
	    	setTitle("Mensa Regensburg");
		} else if (location.equalsIgnoreCase("pa")) {
	    	selectedTable = TBL_PA;
	    	urls[0] = urlPaCur;
	    	urls[1] = urlPaNex;
	    	setTitle("Mensa Passau");
		} else if (location.equalsIgnoreCase("deg")) {
	    	selectedTable = TBL_DEG;
	    	urls[0] = urlDegCur;
	    	urls[1] = urlDegNex;
	    	setTitle("Mensa Deggendorf");
		}
        
        // Heute ein Samstag oder Sonntag?

        try {
			while (myDate.getDayName(selectedDate).equalsIgnoreCase("Sa") || myDate.getDayName(selectedDate).equalsIgnoreCase("So")){
				selectedDate = myDate.getCustomDate(selectedDate, 1);
			}
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		
		if (!dateInDB(selectedDate, selectedTable)){
			dropTable(selectedTable);
			createTable(selectedTable);
        	parse2db(urls[0], selectedTable);
        	parse2db(urls[1], selectedTable);
			}
		
		if (!dateInDB(selectedDate, selectedTable)){
			//TODO Fehler ausgeben
			Log.d("Mensa: ,","Datum nicht in der DB zu finden!");
		}
		
		Log.d("Mensa:", "Link zum aktuellen CSV: " + urls[0]);
		Log.d("Mensa:", "Link zum aktuellen CSV: " + urls[1]);
		Log.d("Mensa:", "Speiseplan wird gemalt aus " + selectedTable);
		ArrayList<HashMap<String, String>> list = getData(selectedTable, selectedDate);
        SimpleAdapter UI = new SimpleAdapter(this, list, R.layout.list_item, new String[] { "name", "price" }, new int[] {R.id.name, R.id.price });
        setListAdapter(UI);
        
        TextView tv1 = (TextView)findViewById(R.id.tv1); 
        try {
			tv1.setText(myDate.getDayName(selectedDate) + " " + myDate.getCustomDate(selectedDate, 0));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeDB();
	}
	

		

	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	TextView tv1 = (TextView)findViewById(R.id.tv1); 
        tv1.setText("Daten werden geladen...");
        
    	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        firstRun = settings.getBoolean("FirstRun", true);
        
        if (firstRun){
            createDB();
    		dropTable(TBL_LA);
    		createTable(TBL_LA);
        	dropTable(TBL_PA);
        	createTable(TBL_PA);
        	dropTable(TBL_R);
        	createTable(TBL_R);
        	dropTable(TBL_DEG);
        	createTable(TBL_DEG);

        	parse2db(urlLaCur, TBL_LA);
        	parse2db(urlLaNex, TBL_LA);
        
        	SharedPreferences.Editor settingsEditor = settings.edit();
        	settingsEditor.putBoolean("FirstRun", false);
            settingsEditor.commit();
            
            MyDateHelper myDate = new MyDateHelper();
            selectedDate = myDate.getToday();
            
            closeDB();
        }
        
        
        
        
    }

	protected void onResume(){
    	super.onResume();
    	createDB();
    	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        location = settings.getString(location, "la");


        
		drawUI(selectedDate);
		closeDB();
	}


    protected void onPause(){
    	super.onPause();
    	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
    	SharedPreferences.Editor settingsEditor = settings.edit();
    	settingsEditor.commit();
    	DB.close();
    }
    
    protected void onStop(){
    	super.onStop();
    	DB.close();
    }
    
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
    	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
    	String location = settings.getString("location","la");
    	Log.d("Mensa: ", "Menu location: " + location);

    	final boolean result = super.onCreateOptionsMenu(menu);
    	
    	menu.clear();
    	
        menu.add(2, PREV_DAY, 0, "Tag zurück");
        menu.add(1, NEXT_DAY, 0, "Tag vor");
    	menu.add(3, REFRESH, 0, "Refresh");
    	menu.add(4, QUIT, 0, "Quit");

    	
    	//SubMenu locMenu = menu.addSubMenu("Mensa");
    	SubMenu locMenu = menu.addSubMenu(5, CHANGE_MENSA, 0, "Mensa");
        	if (location.equalsIgnoreCase("la")){ 
        		locMenu.add(1, LA, 0, "Landshut").setChecked(true);
        	} else {
        		locMenu.add(1, LA, 0, "Landshut").setChecked(false);
        	}
        	
        	if (location.equalsIgnoreCase("r")){
        		locMenu.add(1, REG, 0, "Regensburg").setChecked(true);
        	} else {
        		locMenu.add(1, REG, 0, "Regensburg").setChecked(false);
        	}
            
        	if (location.equalsIgnoreCase("deg")){	
        		locMenu.add(1, DEG, 0, "Deggendorf").setChecked(true);
            } else {
            	locMenu.add(1, DEG, 0, "Deggendorf").setChecked(false);
            }
        	
            if (location.equalsIgnoreCase("pa")){
        		locMenu.add(1, PA, 0, "Passau").setChecked(true);
            } else {
            	locMenu.add(1, PA, 0, "Passau").setChecked(false);
            }
        	
        	locMenu.setGroupCheckable(1, true, true);
        
        return result;
    }
    

    public boolean onOptionsItemSelected(MenuItem item) {

    	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        SharedPreferences.Editor settingsEditor = settings.edit();

        switch (item.getItemId()) {
	        case PREV_DAY:
	        	//prevDay();
	        	return true;
	        	
	        case NEXT_DAY:
	        	//nextDay();
	        	return true;
	        	
        	case REFRESH:
	        	Log.d("Mensa: ", "Refresh pressed");
	        	//refresh();
	        	return true;
	        	
	        case CHANGE_MENSA:
	        	Log.d("Mensa: ", "Mensa pressed");
	        	return true;
	        	
	        case LA:
	        	location = "la";
	        	Log.d("Mensa: ", "LANDSHUT gesetzt");
	            settingsEditor.putString("location", location);
	            settingsEditor.commit();
	            drawUI(selectedDate);
	            return true;
	        	
	        case REG:
	        	location = "r";
	        	Log.d("Mensa: ", "REGENSBURG gesetzt");
	            settingsEditor.putString("location", location);
	            settingsEditor.commit();
	            drawUI(selectedDate);
	        	return true;
	        	
	        case DEG:
	        	location = "deg";
	        	Log.d("Mensa: ", "DEGGENDORF gesetzt");
	            settingsEditor.putString("location", location);
	            settingsEditor.commit();
	            drawUI(selectedDate);
	        	return true;
	            
	        case PA:
	        	location = "pa";
	        	Log.d("Mensa: ", "PASSAU gesetzt");
	            settingsEditor.putString("location", location);
	            settingsEditor.commit();
	            drawUI(selectedDate);
	        	return true;

	        case QUIT:
	        	Log.d("Mensa: ", "Quit pressed");
	        	finish();
	        	return true;

        }
        return false;
    }
    
	@Override
	public void onClick(View v) {
	}
    

}

