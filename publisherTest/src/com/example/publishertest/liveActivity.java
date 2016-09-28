package com.example.publishertest;


import com.eil.eilpublisher.interfaces.LiveEventInterface;
import com.eil.eilpublisher.interfaces.LiveInterface;
import com.eil.eilpublisher.liveConstants.LiveConstants;
import com.eil.eilpublisher.media.LivePushConfig;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class liveActivity extends Activity implements OnClickListener{

	public final static String TAG = "LiveActivity";
	private SurfaceView mSurfaceView;
	
	private Button mBtnStartLive;
	private Button mBtnStopLive;
	private Button mBtnSwitchCam;
	
	static int mDefinitionMode = 0;//Ĭ��480�ֱ���
	static String mRtmpUrl = "";
	static int mEncodeMode = 1;//Ĭ��Ӳ����
	private LivePushConfig mLivePushConfig;
	Handler mHandler = null; 
	Runnable mRunnable;
	 
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//���ر���
		setContentView(R.layout.activity_live);
				
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);//����ȫ��
		
		mBtnStartLive = (Button) findViewById(R.id.btn_start);
		mBtnStartLive.setOnClickListener(this);
		mBtnStopLive = (Button) findViewById(R.id.btn_stop);
		mBtnStopLive.setOnClickListener(this);
		mBtnSwitchCam = (Button) findViewById(R.id.btn_switch);
		mBtnSwitchCam.setOnClickListener(this);
		
		mLivePushConfig = new LivePushConfig();
		updatePushConfig();
		mSurfaceView=(SurfaceView)findViewById(R.id.surfaceView);
		LiveInterface.getInstance().init(mSurfaceView , mLivePushConfig);
		
		mHandler = new Handler(); 
		mRunnable = new Runnable() {
            public void run() {
            	 Log.i(TAG, "LiveEventInterface disconnect and reconnect init"); 
				 LiveInterface.getInstance().init(mSurfaceView , mLivePushConfig);
				 int ret = LiveInterface.getInstance().start(); 
				 if(ret < 0 )
				 {
					 Log.i(TAG, "LiveEventInterface disconnect and reconnect start");
					 back();						   
				 } 
             }
        };
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LiveInterface.getInstance().resume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		LiveInterface.getInstance().pause();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		 switch (v.getId()) {
		 case R.id.btn_start:
			 int ret = LiveInterface.getInstance().start();   
			 if(ret < 0)
			 {
				 Log.i(TAG, "connect failed"); 
				 back();
			 }			      	 
             break;
         case R.id.btn_stop:
        	 LiveInterface.getInstance().stop();
        	 back();
             break;
         case R.id.btn_switch:
        	 LiveInterface.getInstance().switchCamera();
             break;         
		 }
	}
	
	private LiveEventInterface mCaptureStateListener = new LiveEventInterface() {
        @Override
        public void onStateChanged(int eventId) {
 
            switch (eventId) {
                case LiveConstants.PUSH_ERR_NET_DISCONNECT://����
                case LiveConstants.PUSH_ERR_NET_CONNECT_FAIL://����ʧ��
                	 Log.i(TAG, "LiveEventInterface disconnect and reconnect"); 
                	 //LiveInterface.getInstance().stop();
                	 //����
					 try {
					   	Thread.sleep(8000);
					 } catch (InterruptedException e) {
						 // TODO Auto-generated catch block
						 e.printStackTrace();
					 }
					 mHandler.post(mRunnable);
	                 break;
                case LiveConstants.PUSH_ERR_AUDIO_ENCODE_FAIL://��Ƶ�ɼ������߳�����ʧ��
                    Log.i(TAG, "LiveEventInterface audio encoder failed");
                   
                    break;
                case LiveConstants.PUSH_ERR_VIDEO_ENCODE_FAIL://��Ƶ�ɼ������߳�����ʧ��
                    Log.i(TAG, "LiveEventInterface video encoder failed");
                   
                    break;
                case LiveConstants.PUSH_EVT_CONNECT_SUCC: //���ӳɹ�
                	Log.i(TAG, "LiveEventInterface connect ok");
                    break;
            }
        }
    };
    
	private void updatePushConfig()
	{
		mLivePushConfig.setRtmpUrl(mRtmpUrl);
		mLivePushConfig.setEventInterface(mCaptureStateListener);
		mLivePushConfig.setAudioChannels(AudioFormat.CHANNEL_IN_MONO);
		mLivePushConfig.setAudioSampleRate(44100);
		if(0 == mEncodeMode)
		{
			mLivePushConfig.setHWVideoEncode(false);
		}else
		{
			mLivePushConfig.setHWVideoEncode(true);
		}
		
		switch(mDefinitionMode)
		{
			case 0:
				mLivePushConfig.setVideoSize(640,480);
				mLivePushConfig.setVideoFPS(20);
				mLivePushConfig.setVideoBitrate(800);
				break;
			case 1:
				mLivePushConfig.setVideoSize(960,540);
				mLivePushConfig.setVideoFPS(18);
				mLivePushConfig.setVideoBitrate(1000);
				break;
			case 2:
				mLivePushConfig.setVideoSize(1280,720);
				mLivePushConfig.setVideoFPS(15);
				mLivePushConfig.setVideoBitrate(1200);
				break;	
		}
	}
	
    private void back() 
    {
	    Intent myIntent = new Intent();  
        myIntent = new Intent(liveActivity.this, MainActivity.class);  
        startActivity(myIntent);  
        this.finish();
    }

	public static void setDefinitionMode(int i) {
		// TODO Auto-generated method stub
		mDefinitionMode = i;
	}

	public static void setRtmpUrl(String strUrl) {
		// TODO Auto-generated method stub
		mRtmpUrl = strUrl;
	}

	public static void setEncodeMode(int i) {
		// TODO Auto-generated method stub
		mEncodeMode = i;
	}	
	
}
