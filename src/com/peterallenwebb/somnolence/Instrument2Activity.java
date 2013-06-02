package com.peterallenwebb.somnolence;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Instrument2Activity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instrument2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.instrument2, menu);
		return true;
	}

}
