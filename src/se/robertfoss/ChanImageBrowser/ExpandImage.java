package se.robertfoss.ChanImageBrowser;

import java.io.File;

import se.robertfoss.MultiTouch.TouchImageView;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class ExpandImage extends Activity {

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expandimage);
        
        TouchImageView tiv = (TouchImageView) findViewById(R.id.touchimageview);
		
        
        String file = this.getIntent().getStringExtra("fileURI");
        TouchImageView.getImgFromFile(new File(file));
        
        tiv.setImage(new File(file), tiv.getWidth(), tiv.getHeight());
		tiv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ExpandImage.this.finish();
			}
		});
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		Viewer.printDebug("onConfigurationChanged()");
	}


}
