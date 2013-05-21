package com.peterallenwebb.somnolence;

public class Block {
	
	public float[] left;
	public float[] right;
	public int size;
	
	public Block (int s) {
		size = s;
		left = new float[size];
		right = new float[size];
	}
	
	public Block (int s, boolean init) {
		size = s;
		if (init) {
			left = new float[size];
			right = new float[size];		
		}
	}
}
