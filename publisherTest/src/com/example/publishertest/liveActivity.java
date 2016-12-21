package com.example.publishertest;

import com.eil.eilpublisher.interfaces.LiveEventInterface;
import com.eil.eilpublisher.interfaces.LiveInterface;
import com.eil.eilpublisher.liveConstants.LiveConstants;
import com.eil.eilpublisher.media.LivePushConfig;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class liveActivity extends Activity implements OnClickListener, OnCheckedChangeListener{

	public final static String TAG = "LiveActivity";
	private GLSurfaceView mSurfaceView;
	
	private Button mBtnStartLive;
	private Button mBtnStopLive;
	private Button mBtnSwitchCam;
	private Button mBtnRecord;
	private TextView mText;
	CheckBox mWatermark;
	static int mDefinitionMode = 0;//Ĭ��480�ֱ���
	static String mRtmpUrl = "rtmp://rtmppush.ejucloud.com/ehoush/liuy";
	static int mEncodeMode = 1;//Ĭ��Ӳ����
	private LivePushConfig mLivePushConfig;
	private boolean mRecording = false;
	private boolean mPublishing = false;
	static boolean mWeaknetOptition = true;
	static int mPublishOrientation = 0;
	
	Handler mHandler = null; 
	Runnable mRunnable;
	 
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//���ر���
		setContentView(R.layout.activity_live);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);//����ȫ��
		if(0 == mPublishOrientation)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		mBtnStartLive = (Button) findViewById(R.id.btn_start);
		mBtnStartLive.setOnClickListener(this);
		mBtnStopLive = (Button) findViewById(R.id.btn_stop);
		mBtnStopLive.setOnClickListener(this);
		mBtnStopLive.setEnabled(false);
		mBtnSwitchCam = (Button) findViewById(R.id.btn_switch);
		mBtnSwitchCam.setOnClickListener(this);
		mBtnRecord = (Button) findViewById(R.id.btn_record);
		mBtnRecord.setOnClickListener(this);
		
		mText = (TextView)findViewById(R.id.tv);
		mText.setVisibility(View.INVISIBLE);
		mWatermark=(CheckBox)findViewById(R.id.checkBox1);
		mWatermark.setOnCheckedChangeListener(this);
		//mWatermark.setChecked(true);
		
		mLivePushConfig = new LivePushConfig();
		updatePushConfig();
		mSurfaceView=(GLSurfaceView)findViewById(R.id.surfaceView1);
		LiveInterface.getInstance().init(mSurfaceView , mLivePushConfig);
		
		mHandler=new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case LiveConstants.PUSH_ERR_NET_DISCONNECT:
                    	showMessage("���ӶϿ���������");
                   	 	updateUI(false);
                        break;
                    case LiveConstants.PUSH_ERR_NET_CONNECT_FAIL:
                    	showMessage("����ʧ��");
                   	 	updateUI(false);
                        break;
                    case LiveConstants.PUSH_ERR_AUDIO_ENCODE_FAIL:
                    	updateUI(false);
                        showMessage("��Ƶ����ʧ��");
                        break;
                    case LiveConstants.PUSH_ERR_VIDEO_ENCODE_FAIL:
                    	updateUI(false);
                        showMessage("��Ƶ����ʧ��");
                        break;
                    case LiveConstants.PUSH_EVT_CONNECT_SUCC:
                    	showMessage("���ӳɹ�");
                        break;
                    case LiveConstants.PUSH_EVT_PUSH_BEGIN:
                    	updateUI(true);
                    	showMessage("��ʼ����");
                        break;
                    case LiveConstants.PUSH_EVT_PUSH_END:
                    	updateUI(false);
                    	showMessage("��������");
                        break;
                    default:
                        break;
                }
            }

        };
/*		mRunnable = new Runnable() {
            public void run() {
            	 Log.i(TAG, "LiveEventInterface disconnect and reconnect init"); 
            	 try {
					   	Thread.sleep(10000);
					 } catch (InterruptedException e) {
						 // TODO Auto-generated catch block
						 e.printStackTrace();
					 }
				 LiveInterface.getInstance().start(); 			 
             }
        };
	*/	
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
		LiveInterface.getInstance().uninit();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		LiveInterface.getInstance().pause();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		 if(mRecording)
    	 {
    		 LiveInterface.getInstance().stopRecord();
    		 mBtnRecord.setText("record");
    		 mRecording = false;
    		 mText.setVisibility(View.INVISIBLE);
    	 }
		 if(mPublishing)
		 {
			 LiveInterface.getInstance().stop();
			 updateUI(false);
		 }
		 mWatermark.setChecked(false);
	}
	
	int index = 0;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		 switch (v.getId()) {
		 case R.id.btn_start:
			 int ret = 0;
			 LiveInterface.getInstance().start(mRtmpUrl);
			 mPublishing = true;
			
             break;
         case R.id.btn_stop:
        	 LiveInterface.getInstance().stop();
        	 mPublishing = false;
        	 //updateUI(false);
             break;
         case R.id.btn_switch:
        	 LiveInterface.getInstance().switchCamera();
             break;
         case R.id.btn_record:
        	 if(mRecording)
        	 {
        		 LiveInterface.getInstance().stopRecord();
        		 mBtnRecord.setText("record");
        		 mRecording = false;
        		 mText.setVisibility(View.INVISIBLE);
        	 }else
        	 {
        		 LiveInterface.getInstance().startRecord();
        		 mBtnRecord.setText("endrecord");
        		 mRecording = true;
        		 mText.setVisibility(View.VISIBLE);
        	 }
        	 
             break;
		 }
	}
	
	private void showMessage(String message)
	{
		 Toast toast = Toast.makeText(liveActivity.this, 
				 message,                     
                 Toast.LENGTH_SHORT);                  
		  	 toast.show();
	}
	
	private void updateUI(boolean state)
	{
		if(state)
		{
			mBtnStartLive.setEnabled(false);
			mBtnStopLive.setEnabled(true);
		}else
		{
			mBtnStartLive.setEnabled(true);
			mBtnStopLive.setEnabled(false);
		}
	}
	
	private LiveEventInterface mCaptureStateListener = new LiveEventInterface() {
        @Override
        public void onStateChanged(int eventId) {
        	Message msg =new Message();
        	
            switch (eventId) {
                case LiveConstants.PUSH_ERR_NET_DISCONNECT://����
               // case LiveConstants.PUSH_ERR_NET_CONNECT_FAIL://����ʧ��
                	 Log.i(TAG, "LiveEventInterface disconnect and reconnect"); 
                	 msg.what = LiveConstants.PUSH_ERR_NET_DISCONNECT;
                	 mHandler.sendMessage(msg);               	 
                	 //����
					 mHandler.post(mRunnable);					
	                 break;
                case LiveConstants.PUSH_ERR_NET_CONNECT_FAIL://����ʧ��
                    Log.i(TAG, "LiveEventInterface connect failed");
                    msg.what = LiveConstants.PUSH_ERR_NET_CONNECT_FAIL;
               	 	mHandler.sendMessage(msg);
                    break;
                case LiveConstants.PUSH_ERR_AUDIO_ENCODE_FAIL://��Ƶ�ɼ������߳�����ʧ��
                    Log.i(TAG, "LiveEventInterface audio encoder failed");
                    msg.what = LiveConstants.PUSH_ERR_AUDIO_ENCODE_FAIL;
               	 	mHandler.sendMessage(msg);
                    break;
                case LiveConstants.PUSH_ERR_VIDEO_ENCODE_FAIL://��Ƶ�ɼ������߳�����ʧ��
                    Log.i(TAG, "LiveEventInterface video encoder failed");
                    msg.what = LiveConstants.PUSH_ERR_VIDEO_ENCODE_FAIL;
               	 	mHandler.sendMessage(msg);
                    break;
                case LiveConstants.PUSH_EVT_CONNECT_SUCC: //���ӳɹ�
                	Log.i(TAG, "LiveEventInterface connect ok");
                	msg.what = LiveConstants.PUSH_EVT_CONNECT_SUCC;
                	mHandler.sendMessage(msg);
                    break;
                case LiveConstants.PUSH_EVT_PUSH_BEGIN: //��ʼ����
                	Log.i(TAG, "LiveEventInterface begin ok");
                	msg.what = LiveConstants.PUSH_EVT_PUSH_BEGIN;
                	mHandler.sendMessage(msg);
                    break;
                case LiveConstants.PUSH_EVT_PUSH_END: //��������
                	Log.i(TAG, "LiveEventInterface publish end");
                	msg.what = LiveConstants.PUSH_EVT_PUSH_END;
                	mHandler.sendMessage(msg);
                    break;
            }
        }
    };
    
	private void updatePushConfig()
	{
		mLivePushConfig.setRtmpUrl(mRtmpUrl);
		Bitmap watermarkImage = BitmapFactory.decodeFile("/sdcard/mark.png");
		mLivePushConfig.setWatermark(watermarkImage,1000,100,200,100);
		mLivePushConfig.setRecordPath("/sdcard/");
		mLivePushConfig.setEventInterface(mCaptureStateListener);
		mLivePushConfig.setAppContext(this);
		mLivePushConfig.setAudioChannels(AudioFormat.CHANNEL_IN_MONO);
		mLivePushConfig.setAudioSampleRate(44100);
		mLivePushConfig.setWeaknetOptition(mWeaknetOptition);
		mLivePushConfig.setVideoResolution(mPublishOrientation);
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
				if(0 == mPublishOrientation)
				{
					mLivePushConfig.setVideoSize(640,360);
				}else
				{
					mLivePushConfig.setVideoSize(360,640);
				}
				
				mLivePushConfig.setVideoFPS(15);
				mLivePushConfig.setVideoBitrate(800);
				break;
			case 2:
				if(0 == mPublishOrientation)
				{
					mLivePushConfig.setVideoSize(1280,720);
				}else
				{
					mLivePushConfig.setVideoSize(720,1280);
				}
				
				mLivePushConfig.setVideoFPS(15);
				mLivePushConfig.setVideoBitrate(1200);
				break;	
		}		
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

	public static void setWeaknetOptition(boolean state)
	{
		mWeaknetOptition = state;
	}
	
	public static void setVideoOrientation(int orientation)
	{
		mPublishOrientation = orientation;
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		 if(isChecked){ 
			 LiveInterface.getInstance().setWaterMarkState(true);
         }else{ 
        	 LiveInterface.getInstance().setWaterMarkState(false);
         } 
	}	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getRepeatCount() == 0) {
        	 Intent intent = new Intent();
     		intent.setClass(liveActivity.this,MainActivity.class);
     		startActivity(intent);
     		finish();
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }

}
