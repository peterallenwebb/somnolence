package com.peterallenwebb.somnolence;

import java.util.ArrayList;
import java.util.List;

public class Choir {

	public List<Voice> voices;
	private SomnolenceContext context;
	
	public Choir(SomnolenceContext ctx) {
		context = ctx;
		voices = new ArrayList<Voice>();
	}

	public Block getNextBlock()
	{
		Block sum = new Block(context.blockSize);
	
		for (Voice v : voices) {
			Block b = v.getNextBlock();
			
			for (int i = 0; i < b.size; i++) {
				sum.left[i] += b.left[i] / 4.0f;
				sum.right[i] += b.right[i] / 4.0f;
			}
		}
		
		return sum;
	}
}
