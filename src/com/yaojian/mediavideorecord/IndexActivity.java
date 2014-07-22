package com.yaojian.mediavideorecord;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class IndexActivity extends Activity {
	public static final int REQUEST_CODE_TAKE_VIDEO = 21;
	
	/**
	 * The directory to save video.
	 */
	public static final String DIR_VIDEO =
			Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+ "Video";

	/**
	 * video path, include file name 
	 */
	private static String videoFullName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		String videoFileName =  "VIDEO_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()).toString() + ".mp4";
		File dirFile = new File(DIR_VIDEO);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		File file = new File(DIR_VIDEO+File.separator+videoFileName);
		videoFullName = file.getAbsolutePath();
		if(file.exists()){
			file.delete();
		}
		Intent intent = new Intent(IndexActivity.this, VideoActivity.class);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, videoFullName);
		intent.putExtra(VideoActivity.INTENT_DATA_TIME_LIMIT, 8000);
		startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v("IndexActivity", "--- onActivityResult ---");
		switch (requestCode) {
		case REQUEST_CODE_TAKE_VIDEO:{
			// TODO handle the video file
			if (resultCode == RESULT_OK) {
				finish();
			} else {
				
			}
		}
		}
		
	}
}
