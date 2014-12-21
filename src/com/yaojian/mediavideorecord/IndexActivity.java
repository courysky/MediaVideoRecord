package com.yaojian.mediavideorecord;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class IndexActivity extends Activity implements OnClickListener{
	private static final int REQUEST_CODE_TAKE_VIDEO = 21;
	private static final int HANDLE_TAKE_VIDEO_SUCCESS = 11;
	
	/**
	 * The directory to save video.
	 */
	public static final String DIR_VIDEO =
			Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+ "Video";

	/**
	 * video path, include file name 
	 */
	private static String videoFullName;
	
	private TextView mVideoPathTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_index);
		mVideoPathTextView = (TextView) findViewById(R.id.tlt_video_path);
		
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
				mHandler.sendEmptyMessage(HANDLE_TAKE_VIDEO_SUCCESS);
			} else {
				finish();
			}
		}
		}
		
	}
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_TAKE_VIDEO_SUCCESS:{
				mVideoPathTextView.setText(videoFullName);
			}
				
				break;

			default:
				break;
			}
		}
		
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_play:{
			Intent intent = new Intent(Intent.ACTION_VIEW);
	        String type = "video/mp4";
	        Uri uri = Uri.parse(videoFullName);
	        intent.setDataAndType(uri, type);
	        startActivity(intent); 
		}
			break;

		default:
			break;
		}
		
	}
	
}
