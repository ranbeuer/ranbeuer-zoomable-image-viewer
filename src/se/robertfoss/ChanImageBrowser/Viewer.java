package se.robertfoss.ChanImageBrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

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
        man.execute(null);
        
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
    
    public void displayPicture(String fileName){
    	printDebug("Pictures in storage: " + fileList.size());
    	Bitmap pic = getImgFromFile(new File(fileName));
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
    
    private synchronized static void printDebug(String str){
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
    	//final int IMG_BUFFER_SIZE = 25384;
    	printDebug("Getting image content - " + sUrl);
    	
        InputStream is = getHTTPConnectionInputStream(sUrl);   
        
        Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(is, null, options);

	     // Only scale if we need to 
	     // (16384 buffer for img processing)
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
	     //options.inTempStorage = new byte[IMG_BUFFER_LEN];
	     
	     is.close();
	     is = getHTTPConnectionInputStream(sUrl);
	     Bitmap img = BitmapFactory.decodeStream(is, null, options);

    	return img;
    }
    
    public void Toast(String str){
    	Toast.makeText(this, str, Toast.LENGTH_SHORT);
    }
        
    
    public class FetcherManager extends AsyncTask<Void, Void, Void>{
    	
        protected IndexFetcher indexFetcher;
        private ArrayList<String> visitedUrls;
        private ArrayList<String> urlList;
        //private ArrayList<Fetcher> fetchers;
        //private final int NUMBER_OF_FETCHERS = 3;
        private final String url = "http://img.4chan.org/b/imgboard.html";
    	private Viewer parent;
    	
    	FetcherManager(Viewer view){
    		printDebug("Creating Fetchers \n");
    		parent = view;
    		visitedUrls = new ArrayList<String>();
    		urlList = new ArrayList<String>();
            indexFetcher = new IndexFetcher(this);
            //fetchers = new ArrayList<Fetcher>();
            
            /*for (int i = 0; i < NUMBER_OF_FETCHERS; i++){
            	printDebug("Fetcher-" + i + " created");
            	Fetcher temp = new Fetcher(this);
            	fetchers.add(temp);
            }*/
    	}
    	

    	protected Void doInBackground(Void... params){
    		printDebug("Running Fetcher \n");
    		indexFetcher.execute(url); 		
            return null;
    	}
    	
    	
    	public synchronized void addCompleteImage(File file, Bitmap img){
    		printDebug("Saving complete Image \n");
        			try { 	    
        				
        			    FileOutputStream fos = parent.openFileOutput(file.toString(), MODE_PRIVATE);
        		   
        			    img.compress(CompressFormat.JPEG, 100, fos);
        			    fos.flush();	           
        			    fos.close();	      
        			    img.recycle();
        			} catch (Exception e) {	        
        			    e.printStackTrace();        
        			}
        			parent.addCompleteImage(file);
        		} 			

    	
    	private synchronized String getNextImageName(){
    		if (urlList.size() != 0) {
    			
    			String temp = urlList.get(urlList.size()-1);
    			printDebug("Delivered next url -  " + temp);
    			visitedUrls.add(temp);
    			printDebug("Trying to remove url-" + urlList.size() + "  from urlList");
    			urlList.remove(urlList.size()-1);
    			return temp;
    		}
    		return null;
    	}
    	
    	public synchronized void addUrl(String url){
    		//printDebug("Trying to add url");
    		if (!urlList.contains(url) && !visitedUrls.contains(url)){
    			System.out.println("Added url  -  " + url);
    			urlList.add(url);
    		}
    	}


		public synchronized void startFetchers() {
			printDebug("	inside startFetchers()");
            while (urlList.size() > 0){
            	new Fetcher(this).execute(getNextImageName());
            	printDebug("New Fetcher created");
            	try {
					Thread.sleep(250);
					printDebug("startFetchers() is sleeping..");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
		}
    }
    public class Fetcher extends AsyncTask<String, Void, Bitmap> {
    	
    	private FetcherManager manager;
    	private String inputFile;
    	
    	Fetcher(FetcherManager manager){
    		this.manager = manager; 
    	}

    	protected Bitmap doInBackground(String... args){
    		printDebug("Running a Fetcher thread");
    		inputFile = manager.getNextImageName();
    		Bitmap pic = null;
					try {
						printDebug("Fetching picture  -  " + inputFile);
						pic = getImgFromUrl("http://images.4chan.org/b/src/" + inputFile);

						printDebug("Fetcher is adding downloaded a picture");
						if (pic != null){
							return pic;
							//manager.addCompleteImage(new File(inputFile), pic);
						} else {
							printDebug(inputFile + " could'nt be parsed into a Bitmap");
						}
					} catch (Exception e) {
						printDebug("An image wasnt downloaded correctly");
						e.printStackTrace();
					}
			return pic;
    	}
    
	    // can use UI thread here
	    protected void onPostExecute(Bitmap pic) {
	       Viewer.this.displayPicture(inputFile);
	       //Main.this.output.setText(result);
	    }
}
 

    
    public class IndexFetcher extends AsyncTask<String, Void, Void> {
    	
    	private FetcherManager manager;
    	String regex = "http://images.4chan.org/b/src/(\\d*).(jpg|gif|png)";
    	private Pattern imgID;
    	private Matcher matcher;
    	
    	
    	IndexFetcher(FetcherManager manager){
    		this.manager = manager;
    		imgID = Pattern.compile(regex);
    	}
    	protected Void doInBackground(String... params){
    		printDebug("Running a indexfetcher thread");
    		String imgboard = "";
    		try {
				imgboard = Viewer.getUrlContent(params[0]);
			} catch (Exception e) {
				printDebug("Fuckballs! Can't download /b/imgboard.html");
				Toast("img.4chan.org can't be resolved");
				e.printStackTrace();
			}

			matcher = imgID.matcher(imgboard);
			
			while (matcher.find()){
				manager.addUrl(matcher.group(1)+ "." + matcher.group(2));
			}
			manager.startFetchers();		
			return null;
    	}
        protected void onPostExecute(final String result) {
            manager.startFetchers();
            
         }

    }
}