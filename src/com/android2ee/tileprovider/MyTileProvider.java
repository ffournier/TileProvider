package com.android2ee.tileprovider;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

public class MyTileProvider implements TileProvider {
	
	public static final int TILE_WIDTH = 256;
    public static final int TILE_HEIGHT = 256;

	private Context mContext;
	private DatabaseHelper dbHelper;
	
	public MyTileProvider(Context context) {
		super();
		// TODO Auto-generated constructor stub
		mContext = context;
		// load bd
		openTiles();
	}
	
	private void openTiles() {
		dbHelper = new DatabaseHelper(mContext);
	    try {
			dbHelper.createDatabase();
		
		    dbHelper.openDataBase();
		    
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Tile getTile(int x, int y, int zoom) {
		Tile result = null;
		Log.w("MyTag", "x : " + x + " y:" + y + " z:" + zoom);
		int column = ((int) (Math.pow(2, zoom) - y) - 1);
		Log.w("MyTag", "row:" + x + " column : " + column + " zoomlevel:" + zoom);
		Cursor data = dbHelper.getTile(x, column, zoom);
		if (data != null) {
			if (data.moveToFirst()) {
				int clmnindex = data.getColumnIndex("tile_data");
				byte[] img = data.getBlob(clmnindex);
				Log.w("MyTag", "data found");
				// TODO here miss height and width of img
				result = new Tile(TILE_WIDTH, TILE_HEIGHT, img);
			}
			data.close();
		}
		if (result == null) {
			// load an empty image
			result = NO_TILE;
		}
		return result;
	}
	
	public LatLngBounds getBounds() {
		LatLngBounds result = null;
		Cursor data = dbHelper.getBoundsMap();
		if (data != null) {
			if (data.moveToFirst()) {
				int columnIndex = data.getColumnIndex("value");
				String value = data.getString(columnIndex);
				String[] values = value.split(",");
				LatLng southwest = new LatLng(Double.parseDouble(values[1]), Double.parseDouble(values[0]));
				LatLng northeast = new LatLng(Double.parseDouble(values[3]), Double.parseDouble(values[2]));
				result = new LatLngBounds(southwest, northeast);
			}
			data.close();
		}
		return result;
	}
	
	public Integer getMinZoom() {
		Integer result = 1;
		Cursor data = dbHelper.getMinZoom();
		if (data != null) {
			if (data.moveToFirst()) {
				int columnIndex = data.getColumnIndex("value");
				result = data.getInt(columnIndex);
				
			}
			data.close();
		}
		return result;
	}
	
	public void close() {
		if (dbHelper != null) {
			dbHelper.close();
		}
		
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		close();
	}
	
	

	
}
