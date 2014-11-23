package com.android2ee.tileprovider;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

public class MainActivity extends ActionBarActivity implements LocationListener {

	GoogleMap map;
	MyTileProvider tileProvider;
	LocationManager locationManager;
	Marker myPositionMarker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		   
		// To test
		tileProvider = new MyTileProvider(this);
		
	    if (map != null){
	    	map.setMapType(GoogleMap.MAP_TYPE_NONE);
	    	map.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
	    	LatLngBounds bounds = tileProvider.getBounds();
	    	//double zoom = tileProvider.getMinZoom();
	    	Log.w("Tag", "bounds " + bounds.toString());
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
	
	private void updateMarker(Location location) {
		if (map != null) {
			LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
			Log.w("Tag", "position " + latlng.toString());
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
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	
}
