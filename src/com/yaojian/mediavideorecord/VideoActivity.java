package com.yaojian.mediavideorecord;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

public class VideoActivity extends Activity implements OnClickListener{
	private static final int HANDLE_UPDATE_TIME_PROGRESS = 31;
	/**
	 * 
	 */
	public static final String INTENT_DATA_TIME_LIMIT = "intent_data_time_limit";
	
	private boolean isRecording;
	private File file;
	private Camera camera;
	/**
	 * the video max last time, default is 10 seconds.
	 */
	private int limitTime = 10000;
	private MediaRecorder mediaRecorder;
	private long startTime = Long.MAX_VALUE;
	
	private ImageButton btn_VideoStart, btn_VideoStop, btn_VideoCancel;
	private SurfaceView sv_view;
	private ProgressBar progressBar ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		initVar();
		initView();
		DisplayMetrics dm = new DisplayMetrics();  
		dm = getResources().getDisplayMetrics(); 
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		sv_view.getLayoutParams().width = screenWidth;
		sv_view.getLayoutParams().height = screenWidth * 128/72;//64/48;
		
	}
	private void initVar(){
		String path = getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT);
		if (null != path && !path.equals("")) {
			file = new File(path);
		} else {
			file = new File(Environment.getExternalStorageDirectory()+File.separator+System.currentTimeMillis()+".mp4");
		}
		limitTime = getIntent().getIntExtra(INTENT_DATA_TIME_LIMIT, 10000);
	}
	
	private void initView(){
		btn_VideoStart = (ImageButton) findViewById(R.id.btn_VideoStart);
		btn_VideoStop = (ImageButton) findViewById(R.id.btn_VideoStop);
		btn_VideoCancel = (ImageButton) findViewById(R.id.btn_VideoCancel);
		sv_view = (SurfaceView) findViewById(R.id.sv_view);
		progressBar = (ProgressBar) findViewById(R.id.progress_time);
		
		sv_view.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		btn_VideoStop.setEnabled(false);

		btn_VideoStart.setOnClickListener(this);
		btn_VideoStop.setOnClickListener(this);
		btn_VideoCancel.setOnClickListener(this);
	}
	
	protected void start() {
		try {
//			file = new File(Environment.getExternalStorageDirectory()+File.separator+System.currentTimeMillis()+".mp4");
			if (file.exists()) {
				file.delete();
			}
			mediaRecorder = new MediaRecorder();
			
			List<Size> sizeList = null;
			{
				camera = Camera.open();
				if (camera != null) {
					Parameters parameters = camera.getParameters();  
					   
					
					sizeList =  parameters.getSupportedVideoSizes();
					if (null == sizeList) {
						sizeList = parameters.getSupportedPreviewSizes();
					}
					camera.setDisplayOrientation(90);
					camera.unlock();
					mediaRecorder.setCamera(camera);
				}
				mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				mediaRecorder.setOrientationHint(90);//��Ƶ��ת90��
			}
			
			mediaRecorder.reset();
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//CAMERA
			
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//MPEG_4//DEFAULT
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			/*
			 * OutputFormat.DEFAULT
			 * AudioEncoder DEFAULT AAC_ELD(phone ok) ACC(phone ok) HE_AAC(GALAXY not ok
			 * VideoEncoder.MPEG_4_SP
			 */
			mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);//MPEG_4_SP

			
            // 获取自己手机合适的尺寸  
//            List<Size> sizeList = parameters.getSupportedPreviewSizes();  
			for (Size size : sizeList) {
				Log.v(VideoActivity.class.getSimpleName(), "SupportedVideoSize :"+size.width+", "+size.height);
				if (size.width<900) {
					mediaRecorder.setVideoSize(size.width, size.height);
					break;
				}
			}
//			mediaRecorder.setVideoSize(640, 480);
//			mediaRecorder.setVideoSize(1280, 720);
			mediaRecorder.setMaxDuration(limitTime);// �������?
			
			// some device not available
//			mediaRecorder.setVideoFrameRate(24);
			mediaRecorder.setOutputFile(file.getAbsolutePath());
			mediaRecorder.setPreviewDisplay(sv_view.getHolder().getSurface());
			
			mediaRecorder.setOnErrorListener(new OnErrorListener() {
				
				@Override
				public void onError(MediaRecorder mr, int what, int extra) {
					if (null != mediaRecorder) {
						mediaRecorder.stop();
						mediaRecorder.release();
						mediaRecorder = null;
					}
					if (camera != null) {
						camera.release();
						camera = null;
					}
					isRecording=false;
					btn_VideoStart.setEnabled(true);
					btn_VideoStop.setEnabled(false);
					Toast.makeText(VideoActivity.this, "Record Error", Toast.LENGTH_LONG).show();
				}
			});
			mediaRecorder.prepare();
			mediaRecorder.start();
			startTime = System.currentTimeMillis();
			mHandler.sendEmptyMessage(HANDLE_UPDATE_TIME_PROGRESS);
			btn_VideoStart.setEnabled(false);
			btn_VideoStop.setEnabled(true);
			isRecording = true;
			Toast.makeText(VideoActivity.this, "Start Record", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO [yaojian] release resource
		}

	}

	private void stop() {
		if (isRecording) {
			if (null != mediaRecorder) {
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
			}
			
			if (camera != null) {
				camera.stopPreview();
				camera.release();
				camera = null;
			}
			
			isRecording=false;
			btn_VideoStart.setEnabled(true);
//			btn_VideoStop.setEnabled(false);
			if (null != file && file.exists()) {
				try {
					ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
					exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, ""+ExifInterface.ORIENTATION_ROTATE_270);
					exifInterface.saveAttributes();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (isRecording) {
			mHandler.removeMessages(HANDLE_UPDATE_TIME_PROGRESS);
			if (null != mediaRecorder) {
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
			}
			
			if (camera != null) {
				camera.release();
				camera = null;
			}
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_VideoStart:{
			start();
		}
			break;
		case R.id.btn_VideoStop:{
			mHandler.removeMessages(HANDLE_UPDATE_TIME_PROGRESS);
			stop();
			setResult(RESULT_OK);
			finish();
			Toast.makeText(VideoActivity.this, "Record Over, "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		}
			break;
		case R.id.btn_VideoCancel:{
			if (isRecording) {
				mHandler.removeMessages(HANDLE_UPDATE_TIME_PROGRESS);
				if (null != mediaRecorder) {
					mediaRecorder.stop();
					mediaRecorder.release();
					mediaRecorder = null;
				}
				if (null != file && file.exists()) {
					file.delete();
				}
				if (camera != null) {
					camera.release();
					camera = null;
				}
			}
			setResult(RESULT_CANCELED);
			finish();
		}
			break;
		default:
			break;
		}
		
	}

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_UPDATE_TIME_PROGRESS:{
				long timeSpan = System.currentTimeMillis() - startTime;
				int percent = (int) (timeSpan * 100 / limitTime);
				progressBar.setProgress(percent);
				if (percent>=100) {
					stop();
					Toast.makeText(VideoActivity.this, "Record Over", Toast.LENGTH_SHORT).show();
				} else {
					mHandler.sendEmptyMessageDelayed(HANDLE_UPDATE_TIME_PROGRESS, 500);
				}
			}
				
				break;

			default:
				break;
			}
		}
		
	};
}
