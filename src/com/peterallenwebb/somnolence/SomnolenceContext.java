package com.peterallenwebb.somnolence;

import java.util.Queue;

public class SomnolenceContext {
	public int sampleRate = 44100;
	public int blockSize = 512;
	public int blockLatency = 3;
    
	public Queue<Block> blockPool;
	
}
