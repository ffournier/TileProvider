package com.android2ee.tileprovider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The DataBase Helper 
 * @author florian
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	// TODO change it
    private static String DB_PATH = "/data/data/com.android2ee.tileprovider/databases/";
    private static String DB_NAME = "tiles-ign.mbtiles";
    private SQLiteDatabase myDataBase;
    private final Context myContext;

    public DatabaseHelper (Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Create database
     * @param name
     * @throws IOException
     */
    public void createDatabase() throws IOException {
        boolean isExist = isDatabaseExist();

        if (!isExist) {
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * CopyDataBase from Asset to the internal storage
     * @param name
     * @throws IOException
     */
    private void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = myContext.getFilesDir() + DB_PATH + DB_NAME;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    /**
     * Test if the database already exist
     * @return
     */
    private boolean isDatabaseExist() {
        SQLiteDatabase control = null;

        try {
            String myPath = DB_PATH + DB_NAME;
            control = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
        	control = null;
        }

        if (control != null) {
        	control.close();
        }
        return control != null ? true : false;
    }

    /**
     * Open DataBase
     * @throws SQLException
     */
    public void openDataBase() throws SQLException {

        // Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

    }

    /**
     * Get Tile in Database
     * @param row : the row 
     * @param column : the colum,
     * @param zoom : the zoom
     * @return the byte[]
     */
    public Cursor getTile(int row, int column, int zoom) {

        return myDataBase.query("tiles", new String[] {"tile_data"}, "tile_row = ? AND tile_column = ? AND zoom_level = ?", 
        		new String[]{Integer.toString(column), Integer.toString(row), Integer.toString(zoom)}, null, null, null);
    }
    
    /**
     * Get Bounds present in db
     * @return
     */
    public Cursor getBoundsMap() {
        return myDataBase.query("metadata", new String[] {"value"}, "name like \"bounds\"", null, null, null, null);
    }
    
    /**
     * get Min Zoom present in db
     * @return
     */
    public Cursor getMinZoom() {
        return myDataBase.query("metadata", new String[] {"value"}, "name like \"minzoom\"", null, null, null, null);
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
 
}
