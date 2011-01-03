package de.koelly.mensa2;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class MealDataSet {
	private Date date = null;
	private String key = null;
	private String dayOfWeek = null;
	private String typeOfMeal = null;
	private String nameOfMeal = null;
	private int price4students = 0;
	private int price4clerks = 0;
	private int price4externs = 0;
	
	public MealDataSet(String _date, String _key, String _dayOfWeek, String _typeOfMeal, String _nameOfMeal, int _price4students, int _price4clerks, int _price4externs) throws ParseException{
		DateFormat df = new SimpleDateFormat("E MMM dd hh:mm:ss yyyy", Locale.ENGLISH);
		// Blöder SimpleDateFormatter kann mit einem UTC im String nichts anfangen. Also raus damit...
		_date = _date.replace("UTC", "" );
		this.date = df.parse(_date);
		
		this.key = _key;
		this.dayOfWeek = _dayOfWeek;
		this.typeOfMeal = _typeOfMeal;
		this.nameOfMeal = _nameOfMeal;
		this.price4students = _price4students;
		this.price4clerks = _price4clerks;
		this.price4externs = _price4externs;
	}

	public MealDataSet(){
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getTypeOfMeal() {
		return typeOfMeal;
	}

	public void setTypeOfMeal(String typeOfMeal) {
		this.typeOfMeal = typeOfMeal;
	}

	public String getNameOfMeal() {
		return nameOfMeal;
	}

	public void setNameOfMeal(String nameOfMeal) {
		this.nameOfMeal = nameOfMeal;
	}

	public int getPrice4students() {
		return price4students;
	}

	public void setPrice4students(int price4students) {
		this.price4students = price4students;
	}

	public int getPrice4clerks() {
		return price4clerks;
	}

	public void setPrice4clerks(int price4clerks) {
		this.price4clerks = price4clerks;
	}

	public int getPrice4externs() {
		return price4externs;
	}

	public void setPrice4externs(int price4externs) {
		this.price4externs = price4externs;
	}

}
