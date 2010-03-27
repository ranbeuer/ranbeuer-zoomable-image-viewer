package se.robertfoss.ChanImageBrowser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;

public class FetcherManager extends AsyncTask<Void, Void, Void>{
	
    private IndexFetcher indexFetcher;
    private ArrayList<String> visitedUrls;
    private ArrayList<String> urlList;
    private ArrayList<Fetcher> fetchers;
    private final int NUMBER_OF_FETCHERS = 1;
	private Viewer parent;
	
	FetcherManager(Viewer view){
		Viewer.printDebug("Creating Fetchers \n");
		parent = view;
		visitedUrls = new ArrayList<String>();
		urlList = new ArrayList<String>();
        indexFetcher = new IndexFetcher(this);
        fetchers = new ArrayList<Fetcher>();
        
        for (int i = 0; i < NUMBER_OF_FETCHERS; i++){
        	Viewer.printDebug("Fetcher-" + i + " created");
        	Fetcher temp = new Fetcher(this);
        	fetchers.add(temp);
        }
	}

	public Void doInBackground(Void... params){
		Viewer.printDebug("Running Fetcher \n");
		indexFetcher.run();
		while (urlList.size() == 0){
			try {
				Viewer.printDebug("Manager is waiting..");
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
        for (Fetcher i : fetchers){
        	i.run();
        }
        return null;
	}
	
	public synchronized void addCompleteImage(final File file, final Bitmap img){
		Viewer.printDebug("Saving complete Image \n");
    			try { 	    
    				
    			    FileOutputStream fos = parent.openFileOutput(file.toString(), Context.MODE_PRIVATE);
    		   
    			    img.compress(CompressFormat.JPEG, 100, fos);
    			    fos.flush();	           
    			    fos.close();	      
    			    img.recycle();
    			} catch (Exception e) {	        
    			    e.printStackTrace();        
    			}
    			parent.runOnUiThread(new Runnable() {
    			    public void run() { 
    			    	parent.addCompleteImage(file);
    			    	}
    			});
    			
    			/*Message msg = Message.obtain();
    			msg.obj = file;
    			handler.handleMessage(msg);*/
    		} 			

	
	public synchronized String getNextImageName(){
		if (urlList.size() != 0) {
			
			String temp = urlList.get(urlList.size()-1);
			Viewer.printDebug("Delivered next url -  " + temp);
			visitedUrls.add(temp);
			Viewer.printDebug("Trying to remove url-" + urlList.size() + "  from urlList");
			urlList.remove(urlList.size()-1);
			return temp;
		}
		return null;
	}
	
	public synchronized void addUrl(String url){
		Viewer.printDebug("Trying to add url");
		if (!urlList.contains(url) && !visitedUrls.contains(url)){
			System.out.println("Added url  -  " + url);
			urlList.add(url);
		}
	}
}

