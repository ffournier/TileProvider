package com.android2ee.tileprovider;

import java.io.Closeable;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

/**
 * Tile Provider
 * @author florian
 *
 */
public class MyTileProvider implements TileProvider, Closeable {
	
	// declaration
	public static final int TILE_WIDTH = 256;
    public static final int TILE_HEIGHT = 256;

	private Context mContext;
	private DatabaseHelper dbHelper;
	
	public MyTileProvider(Context context, String name) {
		super();
		mContext = context;
		// load bd
		openTiles(name);
	}
	
	/**
	 * Load Db
	 * @param name
	 */
	private void openTiles(String name) {
		dbHelper = new DatabaseHelper(mContext);
	    try {
			dbHelper.createDatabase(name);
		
		    dbHelper.openDataBase();
		    
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Tile getTile(int x, int y, int zoom) {
		Tile result = null;
		// convert colmun due to te mbtile
		int column = ((int) (Math.pow(2, zoom) - y) - 1);
		// call request in db
		Cursor data = dbHelper.getTile(x, column, zoom);
		if (data != null) {
			if (data.moveToFirst()) {
				// get the bitmap
				int clmnindex = data.getColumnIndex("tile_data");
				byte[] img = data.getBlob(clmnindex);
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
	
	/**
	 * Get Bounds of map present in db
	 * @return
	 */
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
	
	/**
	 * Get Min Zoom present in db
	 * @return
	 */
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
	
	@Override
	public void close() {
		if (dbHelper != null) {
			dbHelper.close();
		}
		
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		// try it.
		close();
	}
	
	

	
}
