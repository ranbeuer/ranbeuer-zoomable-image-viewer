package se.robertfoss.ChanImageBrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
import android.os.Environment;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class Viewer extends Activity {
	
    /** Called when the activity is first created. */

    private ArrayList<File> fileList;

    private GridView gridView;
    private ImageAdapter imgAdapter;
    private FetcherManager man;
    private static final boolean isDebug = true;
    private static final File baseDir = new  File(Environment.getExternalStorageDirectory(), "/4Chan/");
    
    
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        fileList = new ArrayList<File>();
        imgAdapter = new ImageAdapter(this);

        gridView = (GridView)findViewById(R.id.gridview);
        gridView.setAdapter(imgAdapter);
        
        gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				printDebug("Image " + position + " was clicked!");
				getImgFromFile(imgAdapter.getItem(position));
				WebView temp = new WebView(Viewer.this);
				temp.loadUrl("file://" + ((File) imgAdapter.getItem(position)).toString() );

				
			}
        	
        });

        
        printDebug("Initializing manager thread");
        man = new FetcherManager(this);
        printDebug("Running manager thread");
        man.execute((Void)null);
        
    }
    
    
    // Fix me, Im serial. IM SUPER SERIAAL GUYS!
    public void onPause(){
    	
    }
    
    
    public void addCompleteImage(File file){
    	
		Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(file.toString(), options);

		fileList.add(file);
		printDebug("New picture addded, " + file.toString() + " for a total of " + fileList.size());
		addFileToAdapter(file);
    }
        
    public void addFileToAdapter(File file){
    	imgAdapter.addItem(new File(baseDir, file.toString()));

    }


    public static Bitmap getImgFromFile(File file){
    	
    	File temp = new File(baseDir, file.toString());
    	Bitmap pic = BitmapFactory.decodeFile(temp.toString());

    	return pic;
    }
    
    synchronized static void printDebug(String str){
    	if (isDebug){
    		System.out.println(str);
    	}
    }
    
    private static InputStream getHTTPConnection(String sUrl){
    	
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
    	BufferedReader rd = new BufferedReader(new InputStreamReader(getHTTPConnection(sUrl)));
    	String content = "", line;
    	while ((line = rd.readLine()) != null){
    		content += line + "\n";
    	}
    	return content;
    }
    
    public static File getFileFromUrl(String sUrl, String outputName) throws IOException  {
    	InputStream in = getHTTPConnection(sUrl);
    	File file = new File(baseDir, outputName);
    	System.out.println("Saving image to: " + file.toString());
    	FileOutputStream out = new FileOutputStream(file);
    	
        byte[] buf = new byte[4 * 1024]; // 4K buffer
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1) {
          out.write(buf, 0, bytesRead);
        }
        in.close();
    	return new File(outputName);
    }
}