package com.soft.xiren.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.util.Log;

import com.soft.xiren.record.util.LogUtil;

/**
 * 编码语音录制帮助类
 * @version V1.0
 */
public class RecorderManager
{
	public static RecorderManager sRecorderManager;

	private static final String TAG = "RecorderManager";
	//sampling rate
	private final static int[] sampleRates = {44100, 22050, 16000, 8000};
	// The interval in which the recorded samples are output to the file
	// Used only in uncompressed mode
	private static final int TIMER_INTERVAL = 120;

	// Recorder used for uncompressed recording
	private AudioRecord     audioRecorder = null;
	// Recorder state; see State
	private State          	state;

	public enum State {INITIALIZING, RECORDING, ERROR, STOPPED};

	// Number of channels, sample rate, sample size(size in bits), buffer size, audio source, sample size(see AudioFormat)
	private short                    nChannels;
	private short                    bSamples;
	private int                      bufferSize;

	// Number of frames written to file on each output(only in uncompressed mode)
	private int                      framePeriod;
	// Buffer for output(only in uncompressed mode)
	private byte[]                   buffer;

	public static RecorderManager getInstanse()
	{
		if(sRecorderManager == null)
		{
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/record.wav";
			LogUtil.d("path :"+path);
			sRecorderManager = new RecorderManager(
					AudioSource.MIC,
					sampleRates[0],
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					path);
		}
		return sRecorderManager;
	}

	/**
	 * 主要用来进行权限判断
	 */
	public void init(OnAudioPermissionListener listener){
		try {
			startRecord();
			release();
		}catch (Exception e){
			LogUtil.d("init  :",e);
			if(listener!=null){
				listener.OnAudioPermission(false);
			}
		}
	}
	/**
	 * Default constructor
	 *
	 * Instantiates a new recorder, in case of compressed recording the parameters can be left as 0.
	 * In case of errors, no exception is thrown, but the state is set to ERROR
	 *
	 */
	public RecorderManager(int audioSource, int sampleRate, int channelConfig, int audioFormat, String filePath){
		try {
			if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
				bSamples = 16;
			} else {
				bSamples = 8;
			}
			if (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
				nChannels = 1;
			} else {
				nChannels = 2;
			}
			framePeriod = sampleRate * TIMER_INTERVAL / 1000;
			bufferSize = framePeriod * 2 * bSamples * nChannels / 8;
			if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) {
				// Check to make sure buffer size is not smaller than the smallest allowed one
				bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
				// Set frame period and timer interval accordingly
				Log.w(TAG, "Increasing buffer size to " + Integer.toString(bufferSize));
			}
			//record period anand read buffer
			framePeriod = bufferSize / ( 2 * bSamples * nChannels / 8 );
			buffer = new byte[framePeriod*bSamples/8*nChannels];
			/**
			 * audioSource Audio source
			 */
			audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);

			if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
				throw new Exception("AudioRecord initialization failed");
//			//在此处进行数据读取
//			audioRecorder.setRecordPositionUpdateListener(updateListener);
//			//录音 通知周期 及 录音数据读取 buffer 的设定
//			audioRecorder.setPositionNotificationPeriod(framePeriod);
//			Log.i(TAG,"path :" +filePath);
			//当前状态为初始化状态
			state = State.INITIALIZING;
		} catch (Exception e) {
			Log.e(TAG, "initialize fail",e);
		}
	}


	/**
	 * Starts the recording, and sets the state to RECORDING.
	 * Call after prepare().
	 */
	public void startRecord() {
		if (state == State.INITIALIZING) {
			audioRecorder.startRecording();
			audioRecorder.read(buffer, 0, buffer.length);
			state = State.RECORDING;
		}
		else {
			Log.e(TAG, "start() called on illegal state");
			state = State.ERROR;
		}
	}


	/**
	 *  Stops the recording, and sets the state to STOPPED.
	 * In case of further usage, a reset is needed.
	 * Also finalizes the wave file in case of uncompressed recording.
	 */
	public void stopRecord() {
		if (state == State.RECORDING) {
			audioRecorder.stop();
			state = State.STOPPED;
		}
		else {
			Log.e(TAG, "stop() called on illegal state");
			state = State.ERROR;
		}
		state = State.INITIALIZING;
	}

	/**
	 *  Releases the resources associated with this class, and removes the unnecessary files, when necessary
	 */
	public void release() {
		if (state == State.RECORDING) {
			stopRecord();
		}
		if (audioRecorder != null) {
			audioRecorder.release();
		}
		state = State.INITIALIZING;
		sRecorderManager = null;
		audioRecorder = null;
	}

	/**
	 * Method used for recording.
	 */
	private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener()
	{
		public void onPeriodicNotification(AudioRecord recorder)
		{
			audioRecorder.read(buffer, 0, buffer.length); // Fill buffer
			Log.i(TAG, buffer+"  "+buffer.length);
		}
		public void onMarkerReached(AudioRecord recorder)
		{
			// NOT USED
		}
	};


	public interface OnAudioPermissionListener{
		void OnAudioPermission(boolean flag);
	}

}
