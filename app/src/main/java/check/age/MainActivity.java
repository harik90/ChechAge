package check.age;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {
	
	private DatePicker datePicker;
	private Button calculateButton;
	private TextView ageTextView;
	private LinearLayout textl;
	private LinearLayout relativeLayout;
	private int[] drawableIds = {R.drawable.bg1, R.drawable.bg2, R.drawable.bg3};
	private int currentImageIndex = 0;
	private NetworkReceiver networkReceiver;
	Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		configureSystemUI();
		setContentView(R.layout.activity_main);
		initializeUI();
		registerNetworkReceiver();
		startImageChangeHandler();
	}
	
	private void configureSystemUI() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			window.setNavigationBarColor(Color.TRANSPARENT);
			window.getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			);
		}
	}
	
	private void initializeUI() {
		datePicker = findViewById(R.id.datePicker);
		calculateButton = findViewById(R.id.calculateButton);
		ageTextView = findViewById(R.id.ageTextView);
		textl = findViewById(R.id.texl);
		
		calculateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				textl.setVisibility(View.VISIBLE);
				calculateAge();
			}
		});
		
		ScrollView scrollView = findViewById(R.id.scrollView);
		scrollView.setScrollbarFadingEnabled(false);
	}
	
	private void registerNetworkReceiver() {
		networkReceiver = new NetworkReceiver();
		IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkReceiver, intentFilter);
	}
	
	private void startImageChangeHandler() {
		relativeLayout = findViewById(R.id.amm);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				relativeLayout.setBackgroundResource(drawableIds[currentImageIndex]);
				currentImageIndex = (currentImageIndex + 1) % drawableIds.length;
				handler.postDelayed(this, 5000);
			}
		};
		handler.post(runnable);
	}
	
	private void calculateAge() {
		int year = datePicker.getYear();
		int month = datePicker.getMonth();
		int day = datePicker.getDayOfMonth();
		Calendar dob = Calendar.getInstance();
		Calendar current = Calendar.getInstance();
		dob.set(year, month, day);
		long diffInMillis = current.getTimeInMillis() - dob.getTimeInMillis();
		long years = diffInMillis / (1000L * 60 * 60 * 24 * 365);
		long months = (diffInMillis / (1000L * 60 * 60 * 24)) % 365 / 30;
		long days = (diffInMillis / (1000L * 60 * 60 * 24)) % 30;
		String ageText = String.format(Locale.getDefault(), "Age: %d years, %d months, %d days", years, months, days);
		ageTextView.setText(ageText);
	}
	
	private class NetworkReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connectivityManager = (ConnectivityManager)
			context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		}
	}
	
	private void unregisterNetworkReceiver() {
		try {
			unregisterReceiver(networkReceiver);
			} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	private void stopImageChangeHandler() {
		handler.removeCallbacksAndMessages(null);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopImageChangeHandler();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		startImageChangeHandler();
	}
}