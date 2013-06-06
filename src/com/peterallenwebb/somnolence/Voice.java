package com.peterallenwebb.somnolence;

// This class is not re-entrant. One thread at a time, please.
public class Voice {
	
	private SomnolenceContext _context;
	private float _phase;
	private float _phaseIncrement;
	private float _freq;
	private float _blend;
	private float _modIncrement;
	private float _modPhase;
	private float _modAmount;
	
	public Voice(SomnolenceContext ctx) {
		_context = ctx; 
		_phase = 0.0f;
		_freq = 440.0f;
		_phaseIncrement = (float)(_freq / _context.sampleRate);
		_modIncrement = (float)(3.0 / _context.sampleRate);
	}
	
	public void setFreq(float f) {
		_freq = f;
		_phaseIncrement = (float)(_freq / _context.sampleRate);
	}
	
	public void setBlend(float b) {
		_blend  = b;
	}
	
	public void setModAmount(float m) {
		_modAmount = m;
	}
	
	public Block getNextBlock() {
		
		Block signal = new Block(_context.blockSize, false);
		
		signal.left = getSinTriBlend(_phase, _freq);
		signal.right = signal.left;
		
		for (int i = 0; i < signal.left.length; i++) {
			signal.left[i] = signal.left[i] * (1.0f - (((float)Math.sin(2.0 * Math.PI * _modPhase) + 1) / 2.0f * _modAmount / 2.0f));
			_modPhase += _modIncrement;
		}
		
		_modPhase = _modPhase - (float)Math.floor(_modPhase);
		
		_phase = _phase + _context.blockSize * _phaseIncrement;
		_phase = _phase - (float)Math.floor(_phase);
		
		return signal;
	}
	
	// 0.0 = pure sine, 1.0 = pure triangle
	public float[] getSinTriBlend(float currPhase, float fixedFreq) {
		
		float[] triSig = getTriSig(currPhase + 0.5f, fixedFreq);
		float[] finalSig = new float[triSig.length];
				
		float fixedPhaseInc = fixedFreq / _context.sampleRate;
		for (int i = 0; i < triSig.length; i++) {
			finalSig[i] = ((1.0f - _blend) * (float)Math.sin(2.0 * Math.PI * currPhase) + _blend * triSig[i] )/ 2;
			
			currPhase += fixedPhaseInc;
		}
		
		// NOTE TO SELF: Move into loop for more accuracy, less speed.
		currPhase = currPhase - (float)Math.floor(currPhase);
		
		return finalSig;
	}
	
	// 0.0 = pure sine, 1.0 = pure triangle
	public float[] getSinSawBlend(float currPhase, float fixedFreq) {
		
		float phaseBias = 0.33f;
		float[] sawSig = getSawSig(currPhase + phaseBias, fixedFreq);
		float[] finalSig = new float[sawSig.length];
				
		float fixedPhaseInc = fixedFreq / _context.sampleRate;
		for (int i = 0; i < sawSig.length; i++) {
			finalSig[i] = ((1.0f - _blend) * (float)Math.sin(2.0 * Math.PI * currPhase) + _blend * sawSig[i] )/ 2;
			
			currPhase += fixedPhaseInc;
			currPhase = currPhase - (float)Math.floor(currPhase);
		}
		
		return finalSig;
	}
	
	private float[] sawSig;
	private float[] getSawSig(float currPhase, float fixedFreq)
	{
		if (sawSig == null)
			sawSig = new float[_context.blockSize];
		
		float[] sawBase = getPpSawBase(currPhase, fixedFreq);
		
		float scale = 1 / 8.0f / fixedFreq * _context.sampleRate;
		for (int i = 0; i < sawSig.length; i++) {
			sawSig[i] = (sawBase[i + 2] - sawBase[i]) * scale;
		}
		
		return sawSig;
	}
	
	// Generate a piecewise parabolic base wave for triangular
	// quasi-bandlimited triangle wave signal.
	private float[] sawBase; 
	private float[] getPpSawBase(float currPhase, float fixedFreq) {
		
		float fixedPhaseInc = fixedFreq / _context.sampleRate;
		
		if (sawBase == null)
			sawBase = new float[_context.blockSize + 2];
		
		// Back up a sample to produce a bit more signal, which we need
		// for the differentiation step.
		currPhase = currPhase - fixedPhaseInc;
		currPhase = currPhase - (float)Math.floor(currPhase);
				
		for (int i = 0; i < _context.blockSize + 2; i++) {
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
			triSig = new float[_context.blockSize];
		
		float[] triBase = getPpTriBase(currPhase, fixedFreq);
		
		float scale = 1 / 8.0f / fixedFreq * _context.sampleRate;
		for (int i = 0; i < triSig.length; i++) {
			triSig[i] = (triBase[i + 2] - triBase[i]) * scale;
		}
		
		return triSig;
	}
	
	// Generate a piecewise parabolic base wave for triangular
	// quasi-bandlimited triangle wave signal.
	private float[] triBase; 
	private float[] getPpTriBase(float currPhase, float fixedFreq) {
		
		float fixedPhaseInc = fixedFreq / _context.sampleRate;
		
		if (triBase == null)
			triBase = new float[_context.blockSize + 2];
		
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
		
		for (int i = 0; i < _context.blockSize + 2; i++) {	
			
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
