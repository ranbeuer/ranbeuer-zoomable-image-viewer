package se.robertfoss.ChanImageBrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import se.robertfoss.ChanImageBrowser.Fetcher.FetcherManager;
import se.robertfoss.ChanImageBrowser.Target.TargetUrl;
import se.robertfoss.MultiTouch.TouchImageView;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class Viewer extends Activity {

	public static final boolean isDebug = true;
	public static final File tempDir = new File(Environment
			.getExternalStorageDirectory(), "/4Chan/temp/");
	public static final File baseDir = new File(Environment
			.getExternalStorageDirectory(), "/4Chan/");
	
	public static final int NBR_IMAGES_TO_DOWNLOAD_DIRECTLY = 75; //16
	public static final int NBR_IMAGES_TO_DOWNLOAD_INCREMENT = 16; //16
	public static final int NBR_IMAGES_TO_DOWNLOAD_AHEAD = 30; //32
	public static final int NBR_IMAGES_TO_DISPLAY_MAX = 75; //38
	
	private ArrayList<File> fileList;
	private GridView gridView;
	private ImageAdapter imgAdapter;
	private FetcherManager man;
	private File lastImageClicked = null;
	private TouchImageView currentImageDisplayed = null;
	
	private static final int MENU_RELOAD = 0;
	//private static final int MENU_MORE = 1;
	
	private static TargetUrl imageTarget;
	private static TargetUrl linkTarget;

	// Parse images from reddit.com/pics
	// private static final String BASE_INDEX = "http://www.reddit.com/r/pics/";
	// private static final String IMAGE_REGEX =
	// "http://imgur.com/[A-Za-z0-9]*.(jpg|gif|png)";
	// private static final String MORE_LINKS_REGEX = "http://";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		printDebug("onCreate()");
		
		fileList = new ArrayList<File>();
		imgAdapter = new ImageAdapter(this, NBR_IMAGES_TO_DISPLAY_MAX);

		
		ArrayList<String> filler = new ArrayList<String>();
		ArrayList<Integer> usableMatcherGroups = new ArrayList<Integer>();
		
		filler.add("http://images.4chan.org/b/src/");
		usableMatcherGroups.add(Integer.valueOf(1));
		filler.add(".");
		usableMatcherGroups.add(Integer.valueOf(2));

		imageTarget = new TargetUrl("http://img.4chan.org/b/imgboard.html",
				"http://images.4chan.org/b/src/(\\d*).(jpg|gif|png)",
				filler,
				usableMatcherGroups);
		
		
		filler = new ArrayList<String>();
		usableMatcherGroups = new ArrayList<Integer>();
		
		filler.add("http://boards.4chan.org/b/res/");
		usableMatcherGroups.add(Integer.valueOf(1));
		linkTarget = new TargetUrl("http://img.4chan.org/b/imgboard.html",
				"<a href=\"res/(\\d*)",
				filler,
				usableMatcherGroups);
		
		printDebug("Initializing manager thread");
		man = new FetcherManager(this,
				NBR_IMAGES_TO_DOWNLOAD_DIRECTLY,
				linkTarget, 
				imageTarget);

		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(imgAdapter);
		
//		gridView.setOnItemLongClickListener(listener)
		
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				printDebug("Image " + position + " was clicked!");
				getImgFromFile(imgAdapter.getItem(position));

				currentImageDisplayed = new TouchImageView(Viewer.this);

				lastImageClicked = (File) imgAdapter.getItem(position);
				
				currentImageDisplayed.setImage(getImgFromFile(lastImageClicked), gridView.getWidth(),
						gridView.getHeight());

				currentImageDisplayed.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Viewer.this.setContentView(gridView);
						v.setVisibility(View.GONE);
						lastImageClicked = null;
						currentImageDisplayed = null;

					}
				});

				setContentView(currentImageDisplayed);
			}
		});

		printDebug("Running manager thread");
		man.execute();

	}

	public void setCurrentView(View view) {
		Viewer.this.setContentView(view);
	}

	@Override
	protected void onResume() {	
		printDebug("onResume()");
		super.onResume();
		
	}

	@Override
	protected void onPause() {
	
		printDebug("onPause()");
		super.onPause();
		
	}

	@Override
	protected void onDestroy() {
		man.destroyFetchers();
		if (tempDir.exists()) {
			File[] files = tempDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}

		man = null;
		printDebug("onDestroy()");
		
		super.onDestroy();
		finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		printDebug("onConfigurationChanged()");
		super.onConfigurationChanged(newConfig);
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, MENU_RELOAD, 0, "Reload");
	    //menu.add(0, MENU_MORE, 0, "More");
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case MENU_RELOAD:
	        imgAdapter.clearContents();
	        man.downloadXAdditionalImages(NBR_IMAGES_TO_DOWNLOAD_DIRECTLY - man.getNumberOfImageToDownload());
	        return true;
	    /*case MENU_MORE:
	        int imagesForDisplay = man.getNbrImagesToDownload() + imgAdapter.getCount();
	        int imagesToAdd = NBR_IMAGES_TO_DISPLAY_MAX - imagesForDisplay;
	        imagesToAdd = imagesToAdd > 16 ? 16 : imagesToAdd;
	        return true;
	    */
	    }
	    return false;
	}

	public void addCompleteImage(File file) {

		Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(file.toString(), options);

		fileList.add(file);
		printDebug("New picture addded, " + file.toString()
				+ " for a total of " + fileList.size());
		addFileToAdapter(file);
	}

	private void addFileToAdapter(File file) {
		imgAdapter.addItem(new File(tempDir, file.toString()));

	}
	
	public void downloadMoreImages(int number){
		imgAdapter.deleteXFirstImages(number);
		man.downloadXAdditionalImages(number);
	}

	public synchronized static void printDebug(String str) {
		if (isDebug) {
			System.out.println(str);
		}
	}

	public static Bitmap getImgFromFile(File file) {

		Bitmap pic = BitmapFactory.decodeFile(file.toString());
		if (pic == null) {
			printDebug("	Tried to read image: " + file.toString());
			printDebug("	Image from file is null");
		}

		return pic;
	}
}