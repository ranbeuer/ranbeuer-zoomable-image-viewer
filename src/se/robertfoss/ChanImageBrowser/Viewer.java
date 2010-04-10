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

import se.robertfoss.MultiTouch.TouchImageView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class Viewer extends Activity {

	/** Called when the activity is first created. */

	private ArrayList<File> fileList;

	private GridView gridView;
	private ImageAdapter imgAdapter;
	private FetcherManager man;
	public static final boolean isDebug = true;
	public static final File tempDir = new File(Environment
			.getExternalStorageDirectory(), "/4Chan/temp/");
	public static final File baseDir = new File(Environment
			.getExternalStorageDirectory(), "/4Chan/");
	private ProgressDialog dialog;

	private static final String BASE_INDEX = "http://img.4chan.org/b/imgboard.html";
	private static final String IMAGE_REGEX = "http://images.4chan.org/b/src/(\\d*).(jpg|gif|png)";
	private static final String MORE_LINKS_REGEX = "http://";


//	Parse images from reddit.com/pics 
//	private static final String BASE_INDEX = "http://www.reddit.com/r/pics/"; 
//	private static final String IMAGE_REGEX = "http://imgur.com/[A-Za-z0-9]*.(jpg|gif|png)"; 
//	private static final String MORE_LINKS_REGEX = "http://";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		printDebug("onCreate()");

		fileList = new ArrayList<File>();
		imgAdapter = new ImageAdapter(this);
		dialog = new ProgressDialog(this);
		printDebug("Initializing manager thread");
		man = new FetcherManager(this, dialog);

		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(imgAdapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				printDebug("Image " + position + " was clicked!");
				getImgFromFile(imgAdapter.getItem(position));

				TouchImageView temp = new TouchImageView(Viewer.this);

				File file = (File) imgAdapter.getItem(position);
				temp.setImage(getImgFromFile(file), gridView.getWidth(),
						gridView.getHeight());

				temp.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Viewer.this.setContentView(gridView);
						v.setVisibility(View.GONE);

					}
				});

				setContentView(temp);
			}
		});

		printDebug("Running manager thread");
		man.execute(BASE_INDEX, IMAGE_REGEX, MORE_LINKS_REGEX);

	}

	public void setCurrentView(View view) {
		Viewer.this.setContentView(view);
	}

	@Override
	protected void onResume() {
		super.onResume();
		printDebug("onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		printDebug("onPause()");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (tempDir.exists()) {
			File[] files = tempDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
		printDebug("onDestroy()");

		finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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

	public void addFileToAdapter(File file) {
		imgAdapter.addItem(new File(tempDir, file.toString()));

	}

	public static Bitmap getImgFromFile(File file) {

		Bitmap pic = BitmapFactory.decodeFile(file.toString());
		if (pic == null) {
			printDebug("	Tried to read image: " + file.toString());
			printDebug("	Image from file is null");
		}

		return pic;
	}

	synchronized static void printDebug(String str) {
		if (isDebug) {
			System.out.println(str);
		}
	}

	private static InputStream getHTTPConnection(String sUrl) {

		URL url = null;
		InputStream is = null;
		try {
			url = new URL(sUrl);
		} catch (MalformedURLException e1) {
			printDebug("Malformed url");
			e1.printStackTrace();
		}
		try {
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(60000);
			connection.connect();
			is = connection.getInputStream();
		} catch (IOException e) {
			printDebug("Couldnt connect to: " + sUrl);
			e.printStackTrace();
		}
		return is;
	}

	public static String getUrlContent(String sUrl) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				getHTTPConnection(sUrl)));
		String content = "", line;
		while ((line = rd.readLine()) != null) {
			content += line + "\n";
		}
		return content;
	}

	public static File getFileFromUrl(String sUrl, String outputName)
			throws IOException {
		InputStream in = getHTTPConnection(sUrl);
		tempDir.mkdirs();
		File file = new File(tempDir, outputName);

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