package com.android2ee.tileprovider;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

/**
 * Main Activity
 * @author florian
 *
 */
public class MainActivity extends ActionBarActivity implements LocationListener, OnCameraChangeListener {

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 10;
	// Declaration
	private GoogleMap map;
	private MyTileProvider tileProvider;
	private LocationManager locationManager;
	private Marker myPositionMarker;
	private TextView textView;
	private Handler handler;
	private int minZoom = -1;
	private int maxZoom = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textView = (TextView) findViewById(R.id.textview);
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		   
		if (checkGooglePlayServicesAvailability()) {
			// Init Map
			if (map != null){
				// set the type to TYPE_NONE
		    	map.setMapType(GoogleMap.MAP_TYPE_NONE);
		    	// create the tileProvider
		    	tileProvider = new MyTileProvider(this, "test.mbtiles");
		    	// Add the Provider in Map
		    	map.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
		    	// center Map
		    	LatLngBounds bounds = tileProvider.getBounds();
		    	CameraUpdate upd = CameraUpdateFactory.newLatLngBounds(bounds,MyTileProvider.TILE_WIDTH , MyTileProvider.TILE_HEIGHT, 0);
		    	minZoom = tileProvider.getMinZoom();
		    	maxZoom = tileProvider.getMaxZoom();
		    	map.setOnCameraChangeListener(this);
		    	map.moveCamera(upd);
		    	// TODO can display location like this
		    	//map.setMyLocationEnabled(true);
		    	locationManager = (LocationManager)  getSystemService(LOCATION_SERVICE);
		    }
		}
	}
	
	/**
	 * Check if the device has the google play service available
	 * @return
	 */
	public boolean checkGooglePlayServicesAvailability()
	{
	      int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	      if(resultCode != ConnectionResult.SUCCESS)
	      {
	          Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST);
	          dialog.setCancelable(false);
	          dialog.show();
	      }
	      return resultCode == ConnectionResult.SUCCESS;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  switch (requestCode) {
	    case PLAY_SERVICES_RESOLUTION_REQUEST:
	      if (resultCode == RESULT_CANCELED) {
	    	  // we quit
	        finish();
	      }
	      return;
	  }
	  super.onActivityResult(requestCode, resultCode, data);
	}  
	
	@Override
	public void onCameraChange(CameraPosition position) {
		if (map != null) {
			if (minZoom != -1) {
				if (position.zoom < minZoom)
			        map.animateCamera(CameraUpdateFactory.zoomTo(minZoom));
			}
			if (maxZoom != -1) {
				if (position.zoom > maxZoom)
			        map.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			DialogFragment dialog = new MyDialogFragment();
			Bundle args = new Bundle();
			args.putString(MyDialogFragment.KEY_TEXT, GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));
			dialog.setArguments(args);
			dialog.show(getSupportFragmentManager(), MyDialogFragment.TAG);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		// maybe change the provider
		if (locationManager != null) {
			Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				updateMarker(location);
			}
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (tileProvider != null) {
			tileProvider.close();
		}
	}
	
	private Runnable myRunnable = new Runnable() {
		
		@Override
		public void run() {
			Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.out);
			textView.setAnimation(animation);
			textView.setVisibility(View.INVISIBLE);
			handler = null;
		}
	};
	
	
	private void displayInfo(String info) {
		if (textView != null) {
			textView.setText(info);
			Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in);
			textView.setAnimation(animation);
			textView.setVisibility(View.VISIBLE);
			
			if (handler == null) {
				handler = new Handler();
			} else {
				handler.removeCallbacks(myRunnable);
			}
			handler.postDelayed(myRunnable, 4000);
		}
	}
	
	
	
	/**
	 * Display the current Location on map
	 * @param location
	 */
	private void updateMarker(Location location) {
		if (map != null) {
			LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
			if (myPositionMarker == null) {
				
				myPositionMarker = map.addMarker(new MarkerOptions()
		        .position(latlng)
		        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
		        .title("My Position")
		        .snippet("My Position Snippet"));
			} else {
				myPositionMarker.setPosition(latlng);
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// display on map
		if (map != null && location != null) {
			updateMarker(location);
			displayInfo("New Location");
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		displayInfo("Provider Disabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		displayInfo("Provider Enabled");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		displayInfo("on Status Changed");
	}



}
