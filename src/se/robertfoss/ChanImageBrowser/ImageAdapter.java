package se.robertfoss.ChanImageBrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;

    private ArrayList<Bitmap> thumbnails;
    private ArrayList<File> fileList;

    public ImageAdapter(Context c) {
        mContext = c;
        //TypedArray a = obtainStyledAttributes(android.R.styleable.Theme);
        //mGalleryItemBackground = a.getResourceId(
        //        android.R.styleable.Theme_galleryItemBackground, 0);
        //a.recycle();
    }

    public int getCount() {
        return thumbnails.size();
    }

    public File getItem(int position) {
        return fileList.get(position);
    }
    
    public void addItem(File file){
    	
    	final int TARGET_HEIGHT = 100;
    	final int TARGET_WIDTH = 150;
    	
    	Viewer.printDebug("Adding image to adapter - " + file.toString());
    	
        FileInputStream is = new FileInputStream(file);
        
        Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.toString(), options);

	     // Only scale if we need to 
	     Boolean scaleByHeight = Math.abs(options.outHeight - TARGET_HEIGHT) >= Math.abs(options.outWidth - TARGET_WIDTH);
	     
	         // Load, scaling to smallest power of 2 that'll get it <= desired dimensions
	         double sampleSize = scaleByHeight
	             ? options.outHeight / TARGET_HEIGHT
	             : options.outWidth / TARGET_WIDTH;
	         options.inSampleSize = 
	        	 (int)Math.pow(2d, Math.floor(
	            		  Math.log(sampleSize)/Math.log(2d)));

	
	     // Do the actual decoding
	     options.inJustDecodeBounds = false;
	     Bitmap img = BitmapFactory.decodeFile(file.toString(), options);
	     
	     fileList.add(file);
	     thumbnails.add(img);
    	BitmapFactory.decodeFile(file.toString());
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(mContext);

        i.setImageBitmap(thumbnails.get(position));
        i.setLayoutParams(new Gallery.LayoutParams(150, 100));
        i.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //i.setBackgroundResource(mGalleryItemBackground);

        return i;
    }
}