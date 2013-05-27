package com.peterallenwebb.somnolence;

import com.peterallenwebb.somnolence.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class InstrumentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument);
        
        SomnolenceContext ctx = new SomnolenceContext();
        ctx.sampleRate = 44100;
        ctx.blockSize = 256;
        ctx.blockLatency = 5;
        
        final Voice v1 = new Voice(ctx);
        final Voice v2 = new Voice(ctx);
        final Voice v3 = new Voice(ctx);

        Choir c = new Choir(ctx);
        c.voices.add(v1);
        c.voices.add(v2);
        c.voices.add(v3);
        
        SeekBar bar = (SeekBar)this.findViewById(R.id.seekBar);
        bar.setMax(1200);
        
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
		
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				float unity = 0.4f * progress + 28.0f; 
				v1.setFreq(unity);
				v2.setFreq(5.0f / 4.0f * unity + 0.2f);
				v3.setFreq(3.0f / 2.0f * unity + 0.2f);
			}
		});
        
        SeekBar bar2 = (SeekBar)this.findViewById(R.id.seekBar2);
        bar2.setMax(100);
        
        bar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
		
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				v1.setBlend(progress / 200.0f);
				v2.setBlend(1.0f - (progress / 100.0f));
				v3.setBlend(progress / 100.0f);
				
			}
		});
        
        RelativeLayout relativeLayout = (RelativeLayout)this.findViewById(R.id.relativeLayout);
        
        XYThumb t1 = new XYThumb(relativeLayout.getContext());
        relativeLayout.addView(t1);
        
        XYThumb t2 = new XYThumb(relativeLayout.getContext());
        relativeLayout.addView(t2);
        
        
        // TrackFeeder f = new TrackFeeder(ctx, c);
        // new Thread(f).start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.instrument, menu);
        
        return true;
    }
    
}
