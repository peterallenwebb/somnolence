package com.peterallenwebb.somnolence;


import java.util.concurrent.ArrayBlockingQueue;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class TrackFeeder implements Runnable {

	public Choir choir;
	public AudioTrack track;
	private SomnolenceContext context;
	public boolean running;
	public ArrayBlockingQueue<Block> buffer;
	
	public TrackFeeder(SomnolenceContext ctx, Choir c) {
		context = ctx;
		choir = c;
		running = false;
		buffer = new ArrayBlockingQueue<Block>(context.blockLatency);
		
		int buffSize = AudioTrack.getMinBufferSize(
				context.sampleRate,
				AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT);
		
		buffSize = Math.max(buffSize, 32000);
		
		track = new AudioTrack(
				AudioManager.STREAM_MUSIC,
				context.sampleRate,
				AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT,
				buffSize,
				AudioTrack.MODE_STREAM);
	
		Thread t = new Thread(new Feed(choir, buffer));
		t.start();
	}
	
	public void run() {
		
		running = true;
		
		track.play();
		
		while (running) {
			
			Block b = null;
			
			try {
				// Note that this call is blocking, so we will wait here for a block
				// to arrive if it is not ready, which should never happen if the
				// block production is keeping up with demand.
				b = buffer.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			byte [] audioData = new byte[4*context.blockSize];
			
			for (int i = 0; i < context.blockSize; i++)
			{
				float l = b.left[i];
				l = l >  1.0f ?  1.0f : l;
				l = l < -1.0f ? -1.0f : l;
				short ls = (short)(l * Short.MAX_VALUE);
				
				float r = b.right[i];
				r = r >  1.0f ?  1.0f : r;
				r = r < -1.0f ? -1.0f : r;
				short rs = (short)(r * Short.MAX_VALUE);
				
				audioData[4*i+0] = (byte)ls;
				audioData[4*i+1] = (byte)((ls >> 8) & 0xff);
				audioData[4*i+2] = (byte)rs;
				audioData[4*i+3] = (byte)((rs >> 8) & 0xff);
			}
			
			track.write(audioData, 0, 4 * context.blockSize);
		}
			
	}
	
	public void pause() {
		
	}
	
	private class Feed implements Runnable {

		public Choir choir;
		public ArrayBlockingQueue<Block> buffer;
		
		public Feed(Choir c, ArrayBlockingQueue<Block> b) {
			choir = c;
			buffer = b;
		}
		
		@Override
		public void run() {
			
			while (true) {
				Block b = choir.getNextBlock();
				
				try {
					buffer.put(b);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
