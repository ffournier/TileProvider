package com.android2ee.tileprovider;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
public class MainActivity extends ActionBarActivity implements LocationListener {

	// Declaration
	GoogleMap map;
	MyTileProvider tileProvider;
	LocationManager locationManager;
	Marker myPositionMarker;
	TextView textView;
	Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textView = (TextView) findViewById(R.id.textview);
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		   
		// Init Map
		if (map != null){
			// set the type to TYPE_NONE
	    	map.setMapType(GoogleMap.MAP_TYPE_NONE);
	    	// create the tileProvider
	    	tileProvider = new MyTileProvider(this, "tiles-ign.mbtiles");
	    	// Add the Provider in Map
	    	map.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
	    	// center Map
	    	LatLngBounds bounds = tileProvider.getBounds();
	    	//double zoom = tileProvider.getMinZoom();
	    	CameraUpdate upd = CameraUpdateFactory.newLatLngBounds(bounds,MyTileProvider.TILE_WIDTH , MyTileProvider.TILE_HEIGHT, 0);
	    	map.moveCamera(upd);
	    	// TODO can display location like this
	    	//map.setMyLocationEnabled(true);
	    }
	    
	    locationManager = (LocationManager)  getSystemService(LOCATION_SERVICE);
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		// maybe change the provider
		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			updateMarker(location);
		}
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (tileProvider != null) {
			tileProvider.close();
		}
	}
	
	private Runnable myRunnable = new Runnable() {
		
		@Override
		public void run() {
			textView.setVisibility(View.GONE);
			handler = null;
		}
	};
	
	
	private void displayInfo(String info) {
		if (textView != null) {
			textView.setText(info);
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
