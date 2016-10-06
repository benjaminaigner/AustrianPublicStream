/**
 Copyright:
 2016 Benjamin Aigner

 This file is part of AustrianPublicStream.

 AustrianPublicStream is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 AustrianPublicStream is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with AustrianPublicStream.  If not, see <http://www.gnu.org/licenses/>.
 **/

package systems.byteswap.publicstream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * This file provides storage access to insert/delete/update any offline programs or already listened
 * programs.
 *
 * There are 2 tables:
 * OFFLINE_TABLE -> all downloaded programs (containing a full representation of ORFParser.ORFProgram)
 * LISTENED_TABLE -> this table contains all already listened programs (only the field ORFProgram.id)
 *
 */
public class StorageProvider extends SQLiteOpenHelper {

    /* debug settings */
    private final static boolean D = true;
    private final static String TAG = "StorageProvider";

    private static final String DATABASE_NAME = "PublicStream";
    private static final String OFFLINE_TABLE_NAME = "PublicStream_Offline";
    private static final String LISTENED_TABLE_NAME = "PublicStream_Listened";
    private static final int DATABASE_VERSION = 2;

    //database of all offline programs
    public static final String KEY_OFFLINE_ROWID = "_id";
    public final static String KEY_OFFLINE_ID = "programid";
    public final static String KEY_OFFLINE_TIME = "time";
    public final static String KEY_OFFLINE_TITLE = "title";
    public final static String KEY_OFFLINE_SHORTTITLE = "shorttitle";
    public final static String KEY_OFFLINE_INFO = "info";
    public final static String KEY_OFFLINE_URL = "url";
    public final static String KEY_OFFLINE_DAYLABEL = "daylabel";

    //database for SSIDs (corresponding to one route)
    public static final String KEY_LISTENED_ROWID = "_id";
    public static final String KEY_LISTENED_ID = "programid";
    public static final String KEY_LISTENED_DATE = "programDate";

    /** static string to create the offline table */
    private static final String OFFLINE_TABLE_CREATE =
            "CREATE TABLE " + OFFLINE_TABLE_NAME + " (" +
                    KEY_OFFLINE_ROWID + " INTEGER PRIMARY KEY ASC," +
                    KEY_OFFLINE_ID + " TEXT," +
                    KEY_OFFLINE_TIME + " TEXT," +
                    KEY_OFFLINE_TITLE + " TEXT," +
                    KEY_OFFLINE_SHORTTITLE + " TEXT," +
                    KEY_OFFLINE_INFO + " TEXT," +
                    KEY_OFFLINE_URL + " TEXT," +
                    KEY_OFFLINE_DAYLABEL + " TEXT" +
                    ");";
    /** static string to create the listened table */
    private static final String LISTENED_TABLE_CREATE =
            "CREATE TABLE " + LISTENED_TABLE_NAME + " (" +
                    KEY_LISTENED_ROWID + " INTEGER PRIMARY KEY ASC," +
                    KEY_LISTENED_ID + " TEXT," +
                    KEY_LISTENED_DATE + " TEXT" +
                    ");";


    /** CTOR, with the current app context */
    StorageProvider(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Request the listened status of a dedicated program
     *
     * @param id The id of the program which should be determined to be already listened to
     * @return is this program already listened to the end?
     */
    public boolean isListened(String id, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(LISTENED_TABLE_NAME, new String[]{KEY_LISTENED_ROWID, KEY_LISTENED_ID},
                KEY_LISTENED_ID + "='" + id + "' AND " + KEY_LISTENED_DATE + "='" + date + "'", null, null, null, null);
        if(cursor != null && cursor.getCount() > 0) {
            if(D) Log.d(TAG, "Program is listened: " + id + "," + date);
            cursor.close();
            return true;
        } else {
            if(cursor != null) cursor.close();
            return false;
        }
    }

    /**
     * Set a program's flag to "listened"
     *
     * @param id ID of the program (ORFProgram.id)
     */
    public void setListened(String id, String date) {
        if(id == null) return;
        if(id.equals("")) return;

        if(D) Log.d(TAG, "Program set listened: " + id + "," + date);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put(KEY_LISTENED_ID,id);
        content.put(KEY_LISTENED_DATE,date);
        db.insert(LISTENED_TABLE_NAME,null,content);
    }

    /**
     * Add a program to the offline available list.
     *
     * @param program ORFProgram, which is available offline
     */
    public void addOffline(ORFParser.ORFProgram program) {
        if(program == null) return;
        if(isOffline(String.valueOf(program.id))) {
            if(D) Log.w(TAG, "Program was already offline: " + program.id);
            deleteOffline(String.valueOf(program.id));
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put(KEY_OFFLINE_ID, program.id);
        content.put(KEY_OFFLINE_TIME, program.time);
        content.put(KEY_OFFLINE_TITLE, program.title);
        content.put(KEY_OFFLINE_SHORTTITLE, program.shortTitle);
        content.put(KEY_OFFLINE_INFO, program.info);
        content.put(KEY_OFFLINE_URL, program.url);
        content.put(KEY_OFFLINE_DAYLABEL, program.dayLabel);
        if(D) Log.d(TAG, "Program added offline: " + program.id);
        db.insert(OFFLINE_TABLE_NAME, null, content);
    }


    /**
     * Remove a program from the offline list (if it is deleted)
     *
     * @param id ORFProgram.id
     * @return Number of deleted rows (should not exceed 1)
     */
    public  boolean deleteOffline(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(D) Log.d(TAG, "Program deleted offline: " + id);
        return db.delete(OFFLINE_TABLE_NAME, KEY_OFFLINE_ID + "='" + id + "'", null) != 0;
    }


    /**
     * Determine if a program is available offline
     *
     * @param id ORFProgram.id
     * @return true if available, false if not
     */
    public boolean isOffline(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(OFFLINE_TABLE_NAME, new String[]{KEY_OFFLINE_ROWID, KEY_OFFLINE_ID},
                KEY_OFFLINE_ROWID + "='" + id + "'", null, null, null, null);
        if(cursor != null && cursor.getCount() > 0) {
            if(D) Log.d(TAG, "Program is offline: " + id);
            cursor.close();
            return true;
        } else {
            if(cursor != null) cursor.close();
            return false;
        }
    }

    /**
     * Fetch all offline available programs in one ArrayList
     *
     * @return ArrayList of all available programs
     */
    public ArrayList<ORFParser.ORFProgram> getOffline() {
        boolean iterate = true;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ORFParser.ORFProgram> list = new ArrayList<>();
        //fetch a cursor with all available fields and no where clause (fetch all)
        Cursor result = db.query(OFFLINE_TABLE_NAME, new String[]{KEY_OFFLINE_ROWID, KEY_OFFLINE_ID, KEY_OFFLINE_TIME,
                KEY_OFFLINE_TITLE, KEY_OFFLINE_SHORTTITLE, KEY_OFFLINE_INFO, KEY_OFFLINE_URL, KEY_OFFLINE_DAYLABEL},
                "", null, null, null, null);

        //are there any results?
        if (result != null && result.getCount() > 0) {
            //go to the last entry, because the last one should be shown first
            result.moveToLast();

            //as long as we can iterate, store the entry to the arraylist
            while (iterate) {
                ORFParser.ORFProgram program = new ORFParser.ORFProgram();

                program.id = Integer.parseInt(result.getString(result.getColumnIndex(KEY_OFFLINE_ID)));
                program.time = result.getString(result.getColumnIndex(KEY_OFFLINE_TIME));
                program.title = result.getString(result.getColumnIndex(KEY_OFFLINE_TITLE));
                program.shortTitle = result.getString(result.getColumnIndex(KEY_OFFLINE_SHORTTITLE));
                program.info = result.getString(result.getColumnIndex(KEY_OFFLINE_INFO));
                program.url = result.getString(result.getColumnIndex(KEY_OFFLINE_URL));
                program.dayLabel = result.getString(result.getColumnIndex(KEY_OFFLINE_DAYLABEL));
                program.isListened = this.isListened(String.valueOf(program.id),program.dayLabel);

                list.add(program);

                //check if we are at the first entry, than quit the loop
                if (result.isFirst()) {
                    iterate = false;
                } else {
                    result.moveToPrevious();
                }
            }
            //close the cursor (free resources)
            result.close();
        }
        if(D) Log.d(TAG, "Found " + list.size() + "offline programs");
        return list;
    }

    /**
     * Create the tables for this DB
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        if(D) Log.d(TAG, "Create database");
        db.execSQL(OFFLINE_TABLE_CREATE);
        db.execSQL(LISTENED_TABLE_CREATE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO: on upgrade should be implemented...
    }
}
