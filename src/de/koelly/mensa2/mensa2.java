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
 * @author      Christopher köllmayr
 * @version     0.0.11
 */


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.content.SharedPreferences;


public class mensa2 extends ListActivity  implements OnClickListener
{
	//Globale Variablen
	boolean firstRun;
	String [][] values = new String [200][10];
	StringBuilder sb = new StringBuilder();
	final String [] meals = new String[16];
    String str;
    private final String db_name = "db_meals";
    private final String t_landshut = "t_landshut";
    ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
    SQLiteDatabase myDB = null;
    
    //CSV URLS
    String urlLaCur = "http://www.stwno.de/splan/spldw.csv";
    String urlLaNex = "http://www.stwno.de/splan/splnw.csv";
    String urlPaCur = "http://www.stwno.de/splan/sppdw.csv";
    String urlPaNex = "";
    String urlRCur = "http://www.stwno.de/splan/sprdw.csv";
    String urlRNex = "";
    String urlDegCur = "http://www.stwno.de/splan/spddw.csv";
    String urlDegNex = "";

    // Speicherort der Einstellungen
    final String PREFERENCES = "mensa2_prefs";
    String cLocation = "";

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
	
	// Fling Effects
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    // Progressbar
    ProgressDialog myProgressDialog = null;
	
	// Angezeigter Tag
	String selectedDay = currentDate(0);

	
	
	/**
	 * Öffnet eine URL und übergibt den Text in einen InputStream
	 * Quelle: http://www.devx.com/wireless/Article/39810/1954
	 * 
	 * @param	urlString	Ort der herunterzuladenden Datei
	 * @return				InputStream  des Inhalts	
	 */
	private InputStream OpenHttpConnection(String urlString) 
	    throws IOException
	    {
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
	 * Öffnet oder erstellt die SQlite Datenbank 
	 */
	private void OpenOrCreateDB(){
		try {
			myDB = this.openOrCreateDatabase(db_name, MODE_PRIVATE, null);			
		} finally {
			
		}
	}

	/**
	 * Tabellen der SQlite DB werden
	 * komplett gelöscht.	
	 */
	private void dropTables(){
    	//drop old table
    	myDB.execSQL("DROP TABLE IF EXISTS t_landshut;");
    	Log.d("Mensa: ", "Old table dropped");
	}
	
	/**
	 * Gibt einen String mit Datum zurück.
	 * Format des Datums: TT.MM.YYYY
	 * Mit i kann ein anderes Datum gewählt werden.
	 * 0 = heute, 1=morgen, usw...
	 * 
	 * @param	i	+/- Tage. 0 = heute
	 * @return		String mit Datum +/- i	
	 */
	private String currentDate(int i){
		//Gibt aktuelles Datum +/- i zurück
		 SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
		 
		 Calendar c1 = Calendar.getInstance(); 
			 
		if (i != 0){
			 try {
					Date d = formatter.parse(selectedDay);
					c1.setTime(d);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }

		 
		 c1.add(Calendar.DATE,i);
		 Log.d("Mensa: ", "Selected date is : " + formatter.format(c1.getTime()));
		 String date = formatter.format(c1.getTime());
		 return date;
}
	
	
	/**
	 * Gibt String mit Abkürzung des aktuellen 
	 * Tages zurück.
	 * Montag --> Mo
	 * 
	 * @param	day		Datum im Format dd.mm.yyyy
	 * @return			String mit Abkürzung des Wochentages	
	 */
	private String getDayName(String day){
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
	
	
	/**
	 * Checkt ob das aktuelle Datum in der DB vorhanden ist
	 * Samstags und Sonntags wird <code>FALSE</code> zurückgegeben.
	 * Beim ersten Run wird auf jeden Fall <code>TRUE</code>
	 * zurückgegeben.
	 *
     * @return          <code>true</code> wenn das aktuelle Datum
     * 					in der DB gefunden wurde; 
     *                  <code>false</code> wenn nicht.	
	 */
	private boolean DBup2date(){
		
		Log.d("mensa: ", "Checke Aktualitaet der DB...");
		
		//beim ersten Start müssen Tabellen angelegt werden...
        myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                + t_landshut
                + " (id integer AUTO_INCREMENT PRIMARY KEY, date VARCHAR(10), type VARCHAR, name VARCHAR, p_stud FLOAT(3), p_clerk FLOAT(3), p_guest FLOAT(3));"
                );
		
        boolean success = false;
		
        //Check ob der heutige (nicht der gewünschte) Tag in der DB ist
        // TODO: SA und SO sind nat. nicht in der DB! Abfangen!
		Cursor c = myDB.rawQuery("SELECT date FROM " + t_landshut + " where date=\"" + currentDate(0) + "\"",null);
		c.moveToFirst();
        
        	if(c.getCount() > 0){	// at least on result?
        		success = true;
        	}
        
        // Samstag und Sonntag wird die DB nicht aktualisiert
        String tmp1;
        tmp1 = getDayName(currentDate(0)); 
        if (tmp1.equalsIgnoreCase("Sa") || tmp1.equalsIgnoreCase("So"))
        	success = true;
        
        //Aber wenn die App das erste mal läuft MUSS upgedatet werden!
        if (firstRun)
        	success = false;
        	
        return success;
	}
	

	/**
	 * Zentrale Funktion zum Updaten der DB
	 * Liest den Ort aus den Preferences aus
	 * und aktualisiert aus entsprechender Datenquelle
	 * 	
	 */	private void refresh(){
		SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
    	String cLocation = settings.getString("location","la");
    	
    	dropTables();
    	if (cLocation.equalsIgnoreCase("la"))
    		updateDBnow(urlLaCur);

    	if (cLocation.equalsIgnoreCase("pa"))
    		updateDBnow(urlPaCur);

    	if (cLocation.equalsIgnoreCase("r"))
    		updateDBnow(urlRCur);

    	if (cLocation.equalsIgnoreCase("deg"))
    		updateDBnow(urlDegCur);

    	onResume();
	}

	 
	/**
	 * Aktualisiert die Datenbank mit entsprechender 
	 * URL als Datenquelle
	 * 
	 * @param	url		String der csv Datei	
	 */
	private void updateDBnow(String URL){
    	
        myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                + t_landshut
                + " (id integer AUTO_INCREMENT PRIMARY KEY, date VARCHAR(10), type VARCHAR, name VARCHAR, p_stud FLOAT(3), p_clerk FLOAT(3), p_guest FLOAT(3));"
                );    	
    	
        Log.d("Mensa: ", "Download startet...");
		try {
			str = DownloadText(URL);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Log.d("Mensa: ", "Download erfolgreich");
        
        Log.d("Mensa: ", "Parse Daten...");
        //start parsing 
        int lineNumber = 0, tokenNumber = 0;
		StringTokenizer lineToken = new StringTokenizer(str, "\n");
		if (str.length() > 1){
			while (lineToken.hasMoreTokens()){
					lineNumber++;
					StringTokenizer st = new StringTokenizer(lineToken.nextToken(), ";");
					while(st.hasMoreTokens()){
						values[lineNumber][tokenNumber] =  st.nextToken();
						tokenNumber++;
					}
					if (values[lineNumber][4] == null){
						lineNumber--;
					} else {
						if (lineNumber > 1){
							String insert = "INSERT INTO t_landshut(date, type, name, p_stud, p_clerk, p_guest) VALUES (" + "\"" + values[lineNumber][0] + "\"" + " , " + "\"" + values[lineNumber][2] + "\"" + "," + "\'" + values[lineNumber][3] + "\'" + "," + "\"" + values[lineNumber][5] + "\"" + "," + "\"" +  values[lineNumber][6] + "\"" + "," + "\"" + values[lineNumber][7] + "\""+ ");";
							Log.d("String SQL vorher: " ,insert);
							DatabaseUtils.sqlEscapeString(insert);
							Log.d("String SQL nachher: " ,insert);
							myDB.execSQL(insert);
						}
					}
					tokenNumber = 0;
			}
		}
		Log.d("Mensa: ", "Parsen abgeschlossen");
    	
    }
	

	/**
	 * Holt die Einträge für das Datum in selectDay
	 * aus der DB und schreibt das Ergebnis in die
	 * Arraylist mStrings
	 * 
     * @return          <code>true</code> wenn das Datum gefunden
     * 					wurde und Daten in mStrings abgelegt wurden; 
     *                  <code>false</code> wenn nicht.
	 */
	private boolean readDB(){ 
		Log.d("Mensa", "ReadDB gestartet");
		HashMap<String, String> item;
		list.clear();

		Cursor c = myDB.rawQuery("select date, name, p_stud, p_clerk, p_guest FROM " + t_landshut + " where date=\"" + selectedDay +"\"",null);
		int name = c.getColumnIndex("name");
		int p_stud = c.getColumnIndex("p_stud");
		int p_clerk = c.getColumnIndex("p_clerk");
		int p_guest = c.getColumnIndex("p_guest");
	
		c.moveToFirst();
		if (c != null) {
			if(c.isFirst()){
					do {
						String meal = c.getString(name);
						String pr_stud = c.getString(p_stud);
						String pr_clerk = c.getString(p_clerk);
						String pr_guest = c.getString(p_guest);
						item = new HashMap<String, String>();
						item.put("name", meal );
						item.put("price", "Stud: " + pr_stud + "€ | Angest: " + pr_clerk + "€ | Gast: " + pr_guest + "€" );
						list.add(item);

					} while(c.moveToNext());
				} else {
					return false;
				}
			} 
		return true;
	}

	/**
	 * Wenn für den nächsten Tag etwas in der DB
	 * zu finden ist, wird selectedDay auf den
	 * nächsten Tag gesetzt und die entsprechenden
	 * Einträge aus der DB geholt.
	 * Ausserdem wird die Überschrift samt Datum angepasst.
	 * 
     * @return          <code>true</code> wenn das Datum gefunden
     * 					wurde und Daten gelesen und geschrieben wurden 
     *                  <code>false</code> wenn nicht.
	 */
	private boolean nextDay(){
		Log.d("Mensa: ", "NEXT_DAY pressed");
		String tmp_selectedDay2 = selectedDay;
		selectedDay = currentDate(1);
		
		int i = 0;
		while (readDB() == false && i < 5){
			selectedDay = currentDate(1);
			i++;
		}
		
		if (i == 5){
			selectedDay = tmp_selectedDay2;
			readDB();
		}
		
		TextView tv2 = (TextView)this.findViewById(R.id.tv1);
		tv2.setText(cLocation + ": " + getDayName(selectedDay) + " " +  selectedDay);
		
		readDB();
		
		drawList();
		
		return true;
	}

	
	/**
	 * Wenn für den vorherigen Tag etwas in der DB
	 * zu finden ist, wird selectedDay auf den
	 * vorherigen Tag gesetzt und die entsprechenden
	 * Einträge aus der DB geholt.
	 * Ausserdem wird die Überschrift samt Datum angepasst.
	 * 
     * @return          <code>true</code> wenn das Datum gefunden
     * 					wurde und Daten gelesen und geschrieben wurden 
     *                  <code>false</code> wenn nicht.
	 */
	private boolean prevDay(){
		Log.d("Mensa: ", "PREV_DAY pressed");
		
		String tmp_selectedDay = selectedDay;
		selectedDay = currentDate(-1);
		
		int i = 0;
		while (readDB() == false && i < 5){
			selectedDay = currentDate(-1);
			i++;
		}
		
		if (i == 5){
			selectedDay = tmp_selectedDay;
			readDB();
		}
		
		drawList();
	
		TextView tv1 = (TextView)this.findViewById(R.id.tv1);
		tv1.setText(cLocation + ": " + getDayName(selectedDay) + " " +  selectedDay);
		
		return true;
	}


	/**
	 * Aktualisiert die ListView im UI mit
	 * den aktuellen Daten
	 */
	private void drawList(){
	   	SimpleAdapter notes = new SimpleAdapter(this, list, R.layout.list_item, new String[] { "name", "price" }, new int[] {R.id.name, R.id.price });
	   	setListAdapter(notes);
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        cLocation = settings.getString("location", "la");
        

        // Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (gestureDetector.onTouchEvent(event)) {
        			return true;
        		}
                    return false;
                }
            };

            //Textview (Datum) beim OnTouchListener registrieren
            TextView tv1 = (TextView)this.findViewById(R.id.tv1);
            tv1.setOnClickListener(mensa2.this); 
            tv1.setOnTouchListener(gestureListener);

            //Liste beim OnTouchListener registrieren
            ListView lv = (ListView)findViewById(android.R.id.list); 
            //lv.setOnClickListener(mensa2.this);
            lv.setOnTouchListener(gestureListener);

    }

	protected void onResume(){
    	super.onResume();
    	Log.d("Mensa: ", "onResume startet");

        SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        SharedPreferences.Editor settingsEditor = settings.edit();

    	
    	Log.d("Mensa Prefs: ", "Preferences werden gelesen");
    	cLocation = settings.getString("location", "la");
    	
    	// Wenn true wird die DB auf alle Fälle geupdatet
    	firstRun = settings.getBoolean("firstRun", true);
    	Log.d("Mensa Prefs: ", cLocation);

	  		
    		OpenOrCreateDB();
    		
    		//DB up2date?
    		if (!DBup2date()){
            	Log.d("Mensa: ", "DB nicht aktuell");
            	refresh();
            } else {
            	Log.d("Mensa: ", "DB noch aktuell");
            }
            
    		int i = 0;
        	while (readDB() == false && i < 5){
        		selectedDay = currentDate(1);
        		i++;
        	}    	
        	
        	// Wenn keine Daten in der DB sind wird 5 Tage in die "zukunft geschaut
        	// Wenn dann nix da ist, wird abgebrochen und auf heute gesetzt
        	if (i == 5){
        		selectedDay = currentDate(0);
        		readDB();
        	}
        	
            drawList();
            
        	TextView tv1 = (TextView)this.findViewById(R.id.tv1);
        	tv1.setText(cLocation + ": " + getDayName(selectedDay) + " " +  selectedDay);
        	
        	//firstRun auf false setzen
            settingsEditor.putBoolean("firstRun", false);
            settingsEditor.commit();
                        
    }


    protected void onPause(){
    	super.onPause();
    	   	
    	if (myDB.isOpen())
    		myDB.close();

    	Log.d("Mensa: ", "onPause startet");
    }
    
    protected void onStop(){
    	super.onStop();
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	
    	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
    	String cLocation = settings.getString("location","la");
    	Log.d("Mensa: ", "Menu cLocation: " + cLocation);

    	final boolean result = super.onCreateOptionsMenu(menu);
    	
    	menu.clear();
    	
        menu.add(2, PREV_DAY, 0, "Tag zurück");
        menu.add(1, NEXT_DAY, 0, "Tag vor");
    	menu.add(3, REFRESH, 0, "Refresh");
    	menu.add(4, QUIT, 0, "Quit");

    	
    	//SubMenu locMenu = menu.addSubMenu("Mensa");
    	SubMenu locMenu = menu.addSubMenu(5, CHANGE_MENSA, 0, "Mensa");
        	if (cLocation.equalsIgnoreCase("la")){ 
        		locMenu.add(1, LA, 0, "Landshut").setChecked(true);
        	} else {
        		locMenu.add(1, LA, 0, "Landshut").setChecked(false);
        	}
        	
        	if (cLocation.equalsIgnoreCase("r")){
        		locMenu.add(1, REG, 0, "Regensburg").setChecked(true);
        	} else {
        		locMenu.add(1, REG, 0, "Regensburg").setChecked(false);
        	}
            
        	if (cLocation.equalsIgnoreCase("deg")){	
        		locMenu.add(1, DEG, 0, "Deggendorf").setChecked(true);
            } else {
            	locMenu.add(1, DEG, 0, "Deggendorf").setChecked(false);
            }
        	
            if (cLocation.equalsIgnoreCase("pa")){
        		locMenu.add(1, PA, 0, "Passau").setChecked(true);
            } else {
            	locMenu.add(1, PA, 0, "Passau").setChecked(false);
            }
        	
        	locMenu.setGroupCheckable(1, true, true);
        
        return result;
    }
    
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        SharedPreferences.Editor settingsEditor = settings.edit();

        switch (item.getItemId()) {
	        case PREV_DAY:
	        	prevDay();
	        	return true;
	        	
	        case NEXT_DAY:
	        	nextDay();
	        	return true;
	        	
        	case REFRESH:
	        	Log.d("Mensa: ", "Refresh pressed");
	        	
	        	myProgressDialog = ProgressDialog.show(this,"Bitte warten", "Daten werden geladen...", true);
				new Thread() {
					public void run() {
						try{
							refresh();
						} catch (Exception e) {	}
						// Dismiss the Dialog 
						myProgressDialog.dismiss();
					}
				}.start();
	        	
	        	
	        	return true;
	        	
	        case CHANGE_MENSA:
	        	Log.d("Mensa: ", "Mensa pressed");
	        	return true;
	        	
	        case LA:
	        	cLocation = "la";
	        	Log.d("Mensa: ", "LANDSHUT gesetzt");
	            settingsEditor.putString("location", cLocation);
	            settingsEditor.commit();
	            setTitle("Mensa Landshut");
	            refresh();
	            return true;
	        	
	        case REG:
	        	cLocation = "r";
	        	Log.d("Mensa: ", "REGENSBURG gesetzt");
	            settingsEditor.putString("location", cLocation);
	            settingsEditor.commit();
	            setTitle("Mensa Regensburg");
	            refresh();
	        	return true;
	        	
	        case DEG:
	        	cLocation = "deg";
	        	Log.d("Mensa: ", "DEGGENDORF gesetzt");
	            settingsEditor.putString("location", cLocation);
	            settingsEditor.commit();
	            setTitle("Mensa Deggendorf");
	            refresh();
	        	return true;
	            
	        case PA:
	        	cLocation = "pa";
	        	Log.d("Mensa: ", "PASSAU gesetzt");
	            settingsEditor.putString("location", cLocation);
	            settingsEditor.commit();
	            setTitle("Mensa Passau");
	            refresh();
	        	return true;

	        case QUIT:
	        	Log.d("Mensa: ", "Quit pressed");
	        	finish();
	        	return true;

        }
        return false;
    }
    
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(mensa2.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                	nextDay();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(mensa2.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                	prevDay();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
    
    public void onClick(View v) {
        //Filter f = (Filter) v.getTag();
        //mensa2.show(this, input, f);
    }


}

