package se.robertfoss.ChanImageBrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.widget.ImageView;

public class Viewer extends Activity {
	
    /** Called when the activity is first created. */

    private ArrayList<File> fileList;

    private int currentlyDisplayed;
    private ImageView imgView;
    private FetcherManager man;
    private static final boolean isDebug = true;
    
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        fileList = new ArrayList<File>();

        currentlyDisplayed = -1;
        imgView = (ImageView)findViewById(R.id.picView);
        
        imgView.setImageResource(R.drawable.icon);
        printDebug("Initializing manager thread");
        man = new FetcherManager(this);
        printDebug("Running manager thread");
        imgView.setImageResource(R.drawable.icon);
        man.execute((Void)null);
        
    }
    
    
    public void addCompleteImage(File file){
    	
    	fileList.add(file);
    	printDebug("New picture addded, " + file.toString() + " for a total of " + fileList.size());
    	if (currentlyDisplayed == -1){
    		currentlyDisplayed=0;
    	}
    	displayPicture(fileList.size()-1);
    }
        
    public void displayPicture(int index){
    	printDebug("Pictures in storage: " + fileList.size());
    	Bitmap pic = getImgFromFile(fileList.get(index));
    	if (pic != null) {
    		printDebug("Displaying picture that isnt null");
    		imgView.setImageBitmap(getImgFromFile(fileList.get(fileList.size()-1)));
    	} else {
    		printDebug("Trying to display null-picture");
    	}
    }

    
    public Bitmap getImgFromFile(File file){
    	
    	Bitmap pic = null;

		try {
			InputStream is = openFileInput(file.toString());
			pic = BitmapFactory.decodeStream(is);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return pic;
    }
    
    synchronized static void printDebug(String str){
    	if (isDebug){
    		System.out.println(str);
    	}
    }
    
    private static InputStream getHTTPConnectionInputStream(String sUrl){
    	
    	URL url = null;
    	InputStream is = null;
		try {
			url = new URL(sUrl);
		} catch (MalformedURLException e1) {
			printDebug("Malformed url");
			e1.printStackTrace();
		}
		try {
    	HttpURLConnection connection  = (HttpURLConnection) url.openConnection();
    	connection.setRequestMethod("GET");
    	connection.setDoOutput(true);
    	connection.setConnectTimeout(5000);
    	connection.setReadTimeout(5000);
		connection.connect();
		is = connection.getInputStream();
		} catch (IOException e) {
			printDebug("Couldnt connect to: " + sUrl);
			e.printStackTrace();
		}
    	return is;
    }
    
    public static  String getUrlContent(String sUrl) throws IOException{
    	BufferedReader rd = new BufferedReader(new InputStreamReader(getHTTPConnectionInputStream(sUrl)));
    	String content = "", line;
    	while ((line = rd.readLine())  != null){
    		content += line + "\n";
    	}  return content;
    }
    
    public static Bitmap getImgFromUrl(String sUrl) throws IOException  {
    	final int TARGET_HEIGHT = 800;
    	final int TARGET_WIDTH = 400;
    	
    	printDebug("Getting image content - " + sUrl);
    	
        InputStream is = getHTTPConnectionInputStream(sUrl);   
        
        Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(is, null, options);

	     // Only scale if we need to 
	     Boolean scaleByHeight = Math.abs(options.outHeight - TARGET_HEIGHT) >= Math.abs(options.outWidth - TARGET_WIDTH);
	     printDebug("Image size: " + options.outWidth + "x" + options.outHeight);
	     if(options.outHeight * options.outWidth * 2 >= 150*150*2){
	         // Load, scaling to smallest power of 2 that'll get it <= desired dimensions
	         double sampleSize = scaleByHeight
	             ? options.outHeight / TARGET_HEIGHT
	             : options.outWidth / TARGET_WIDTH;
	         options.inSampleSize = 
	        	 (int)Math.pow(2d, Math.floor(
	            		  Math.log(sampleSize)/Math.log(2d)));

	     }
	
	     // Do the actual decoding
	     options.inJustDecodeBounds = false;
	     
	     is.close();
	     is = getHTTPConnectionInputStream(sUrl);
	     Bitmap img = BitmapFactory.decodeStream(is, null, options);

    	return img;
    }
}