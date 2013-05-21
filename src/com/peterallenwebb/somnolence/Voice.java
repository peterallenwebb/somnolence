package com.peterallenwebb.somnolence;

// This class is not re-entrant. One thread at a time, please.
public class Voice {
	
	private SomnolenceContext context;
	private float phase;
	private float phaseIncrement;
	private float freq;
	private float blend;
	
	private static final float twoPi = (float)(2.0 * Math.PI);
	
	public Voice(SomnolenceContext ctx) {
		context = ctx; 
		phase = 0.0f;
		freq = 440.0f;
		phaseIncrement = (float)(freq / context.sampleRate);
	}
	
	public void setFreq(float f) {
		freq = f;
		phaseIncrement = (float)(freq / context.sampleRate);
	}
	
	public void setBlend(float b) {
		blend  = b;
	}
	
	public Block getNextBlock() {
		
		Block signal = new Block(context.blockSize, false);
		
		signal.left = getSinTriBlend(phase, freq);
		signal.right = signal.left;
		
		phase = phase + context.blockSize * phaseIncrement;
		phase = phase - (float)Math.floor(phase);
		
		return signal;
	}
	
	// 0.0 = pure sine, 1.0 = pure triangle
	public float[] getSinTriBlend(float currPhase, float fixedFreq) {
		
		float[] triSig = getTriSig(currPhase + 0.5f, fixedFreq);
		float[] finalSig = new float[triSig.length];
				
		float fixedPhaseInc = fixedFreq / context.sampleRate;
		for (int i = 0; i < triSig.length; i++) {
			finalSig[i] = ((1.0f - blend) * (float)Math.sin(2.0 * Math.PI * currPhase) + blend * triSig[i] )/ 2;
			
			currPhase += fixedPhaseInc;
			currPhase = currPhase - (float)Math.floor(currPhase);
		}
		
		return finalSig;
	}
	
	// 0.0 = pure sine, 1.0 = pure triangle
	public float[] getSinSawBlend(float currPhase, float fixedFreq) {
		
		float phaseBias = 0.33f;
		float[] sawSig = getSawSig(currPhase + phaseBias, fixedFreq);
		float[] finalSig = new float[sawSig.length];
				
		float fixedPhaseInc = fixedFreq / context.sampleRate;
		for (int i = 0; i < sawSig.length; i++) {
			finalSig[i] = ((1.0f - blend) * (float)Math.sin(2.0 * Math.PI * currPhase) + blend * sawSig[i] )/ 2;
			
			currPhase += fixedPhaseInc;
			currPhase = currPhase - (float)Math.floor(currPhase);
		}
		
		return finalSig;
	}
	
	private float[] sawSig;
	private float[] getSawSig(float currPhase, float fixedFreq)
	{
		if (sawSig == null)
			sawSig = new float[context.blockSize];
		
		float[] sawBase = getPpSawBase(currPhase, fixedFreq);
		
		float scale = 1 / 8.0f / fixedFreq * context.sampleRate;
		for (int i = 0; i < sawSig.length; i++) {
			sawSig[i] = (sawBase[i + 2] - sawBase[i]) * scale;
		}
		
		return sawSig;
	}
	
	// Generate a piecewise parabolic base wave for triangular
	// quasi-bandlimited triangle wave signal.
	private float[] sawBase; 
	private float[] getPpSawBase(float currPhase, float fixedFreq) {
		
		float fixedPhaseInc = fixedFreq / context.sampleRate;
		
		if (sawBase == null)
			sawBase = new float[context.blockSize + 2];
		
		// Back up a sample to produce a bit more signal, which we need
		// for the differentiation step.
		currPhase = currPhase - fixedPhaseInc;
		currPhase = currPhase - (float)Math.floor(currPhase);
				
		for (int i = 0; i < context.blockSize + 2; i++) {
			sawBase[i] = 2.0f * currPhase - 1.0f;
			sawBase[i] *= sawBase[i];
			
			currPhase += fixedPhaseInc;
			currPhase = currPhase - (float)Math.floor(currPhase);
		}
		
		return sawBase;
	}
	
	
	
	private float[] triSig;
	private float[] getTriSig(float currPhase, float fixedFreq)
	{
		if (triSig == null)
			triSig = new float[context.blockSize];
		
		float[] triBase = getPpTriBase(currPhase, fixedFreq);
		
		float scale = 1 / 8.0f / fixedFreq * context.sampleRate;
		for (int i = 0; i < triSig.length; i++) {
			triSig[i] = (triBase[i + 2] - triBase[i]) * scale;
		}
		
		return triSig;
	}
	
	
	
	// Generate a piecewise parabolic base wave for triangular
	// quasi-bandlimited triangle wave signal.
	private float[] triBase; 
	private float[] getPpTriBase(float currPhase, float fixedFreq) {
		
		float fixedPhaseInc = fixedFreq / context.sampleRate;
		
		if (triBase == null)
			triBase = new float[context.blockSize + 2];
		
		// Back up a sample to produce a bit more signal, which we need
		// for the differentiation step.
		currPhase = currPhase - fixedPhaseInc;
		currPhase = currPhase - (float)Math.floor(currPhase);
		
		float triVal;
		float triInc;
		float signFactor;
		
		if (currPhase < 0.25f) {
			triVal = 4.0f * currPhase;
			triInc = fixedPhaseInc * 4.0f;
			signFactor = 1.0f;
		}
		else if (currPhase < .75f) {
			triVal = -4.0f * (currPhase - 0.50f);
			triInc = fixedPhaseInc * -4.0f;
			signFactor = -1.0f;
		}
		else {
			triVal = 4.0f * (currPhase - 1.0f);  
			triInc = fixedPhaseInc * 4.0f;
			signFactor = 1.0f;
		}
		
		for (int i = 0; i < context.blockSize + 2; i++) {	
			
			triBase[i] = signFactor * (1.0f -  triVal * triVal);
			triVal += triInc;
			
			if (triVal > 1.0f) {
				triVal = 2.0f - triVal;
				triInc = fixedPhaseInc * -4.0f;
				signFactor = -1.0f;
			} else if (triVal < -1.0){
				triVal = -2.0f - triVal;
				triInc = fixedPhaseInc * 4.0f;
				signFactor = 1.0f;
			}
		}
		
		return triBase;
	}
}
