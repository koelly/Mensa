package de.koelly.mensa2;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Mensa Landshut
 * @author koelly
 *
 * Permissions:
 * - Internet: Speisepläne holen
 * - Access Network State: Wenn kein Internet, Meldung machen statt abstürzen
 *
 */


public class mensa2 extends ListActivity  implements OnClickListener, Runnable 
{
	
    // Speicherort der Einstellungen
    final String PREFERENCES = "mensa.prf";
    
    // Progressdialog für Laden des Speiseplans
    ProgressDialog myProgressDialog = null;


    // Formatierung des Datums festlegen
	SimpleDateFormat formatter_today_day   = new SimpleDateFormat("dd");
	SimpleDateFormat formatter_today_month = new SimpleDateFormat("MM");
	SimpleDateFormat formatter_today_year  = new SimpleDateFormat("yyyy");
	SimpleDateFormat formatter_today_nice  = new SimpleDateFormat("E dd.MM.yyyy", Locale.GERMAN);
	
	// Heutiges Datum holen
	Calendar c1 = Calendar.getInstance();
	
	// Datum formatieren
	String today_day = formatter_today_day.format(c1.getTime());
	String today_month = formatter_today_month.format(c1.getTime());
	String today_year = formatter_today_year.format(c1.getTime());
	String today_nice = formatter_today_nice.format(c1.getTime());
	
    // Url zu XML von heute
	URL mensa_XML = null;
    
    MensaProvider myMensaProvider = new MensaProvider();
	ArrayList<MealDataSet> data;
	ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();


	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TextView tv1 = (TextView)findViewById(R.id.tv1);
        tv1.setText(today_nice);
 
        SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString("lastParsed", today_nice);
        settingsEditor.commit();
        
        try {
			mensa_XML = new URL("http://koelly-testerei3.appspot.com/Mensa2xml/" + today_year + "/" + today_month + "/" + today_day);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (isOnline()){
	        myProgressDialog = ProgressDialog.show(this, "Bitte warten...", "Speiseplan wird geladen", true);
	        Thread thread = new Thread(this);
	        thread.start();
		} else {
			Toast.makeText(this, "Kein aktive Internetverbindung!", Toast.LENGTH_LONG).show();
		}
		
		
		
    }

	protected void onResume(){
    	super.onResume();

        SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
        String lastParsed = settings.getString("lastParsed", "");
        
        if (!lastParsed.equalsIgnoreCase(today_nice)){
            myProgressDialog = ProgressDialog.show(this, "Bitte warten...", "Speiseplan wird geladen", true);
            Thread thread = new Thread(this);
            thread.start();
        }
        
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString("lastParsed", today_nice);
        settingsEditor.commit();
        
		drawUI();
		
	}
	
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
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
    
	
	
	
	
	public void parseData(){
       	data = myMensaProvider.getData(mensa_XML);
       	
		HashMap<String, String> item;
		list.clear();
		
		Iterator<MealDataSet> itr = data.iterator();
		while (itr.hasNext()) {
			MealDataSet mds = itr.next();
			
			item = new HashMap<String, String>();
			item.put("name", mds.getNameOfMeal() );
			DecimalFormat df = new DecimalFormat("###0.00");
			item.put("price", "Stud: " + df.format((double)mds.getPrice4students()/100) + "€ | Angest: " + df.format((double)mds.getPrice4clerks()/100) + "€ | Gast: " + df.format((double)mds.getPrice4externs()/100) + "€" );
			list.add(item);			
		}
		
		//Wocheende, Ferien, wat weiss ich...
		if (data.size() == 0){
			item = new HashMap<String, String>();
			item.put("name", "Keine Daten vorhanden");
			item.put("price", "");
			list.add(item);
		}
	}
	
	public void drawUI(){
        SimpleAdapter UI = new SimpleAdapter(this, list, R.layout.list_item, new String[] { "name", "price" }, new int[] {R.id.name, R.id.price });
        setListAdapter(UI);
	}

	@Override
	public void run() {
		parseData();
		handler.sendEmptyMessage(0);		
	}
	
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	myProgressDialog.dismiss();
        	drawUI();

        }
    };

}

