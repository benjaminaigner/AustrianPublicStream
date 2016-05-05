package systems.byteswap.publicstream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * TODO: header neu schreiben (ist ja keine route mehr :-))
 * This file provides storage access to insert/delete/update any route associated
 * data.
 * There are 2 tables:
 * AIPRoute_Routes
 *
 * contains all routes (also attached via CursorAdapter to the main window) and all
 * necessary data.
 *
 * AIPRoute_SSIDs
 * In order to provide the possibility to attach one route to a specific SSID, the second table is
 * used. There is a 1 to n relation between routes and ssids
 *

 Copyright (C) 2015  Benjamin Aigner

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class StorageProvider extends SQLiteOpenHelper {
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

    private static final String LISTENED_TABLE_CREATE =
            "CREATE TABLE " + LISTENED_TABLE_NAME + " (" +
                    KEY_LISTENED_ROWID + " INTEGER PRIMARY KEY ASC," +
                    KEY_LISTENED_ID + " TEXT" +
                    ");";


    StorageProvider(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public boolean isListened(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(LISTENED_TABLE_NAME, new String[]{KEY_LISTENED_ROWID, KEY_LISTENED_ID},
                KEY_LISTENED_ID + "='" + id + "'", null, null, null, null);
        if(cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            if(cursor != null) cursor.close();
            return false;
        }
    }

    public void setListened(String id) {
        if(id == null) return;
        if(id.equals("")) return;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put(KEY_LISTENED_ID,id);
        db.insert(LISTENED_TABLE_NAME,null,content);
    }

    public void addOffline(ORFParser.ORFProgram program) {
        if(program == null) return;
        if(isOffline(String.valueOf(program.id))) {
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
        db.insert(OFFLINE_TABLE_NAME, null, content);
    }

    public  boolean deleteOffline(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(OFFLINE_TABLE_NAME, KEY_OFFLINE_ID + "='" + id + "'", null) != 0;
    }

    public boolean isOffline(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(OFFLINE_TABLE_NAME, new String[]{KEY_OFFLINE_ROWID, KEY_OFFLINE_ID},
                KEY_OFFLINE_ROWID + "='" + id + "'", null, null, null, null);
        if(cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            if(cursor != null) cursor.close();
            return false;
        }
    }

    public ArrayList<ORFParser.ORFProgram> getOffline() {
        boolean iterate = true;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ORFParser.ORFProgram> list = new ArrayList<>();
        Cursor result = db.query(OFFLINE_TABLE_NAME, new String[]{KEY_OFFLINE_ROWID, KEY_OFFLINE_ID, KEY_OFFLINE_TIME,
                KEY_OFFLINE_TITLE, KEY_OFFLINE_SHORTTITLE, KEY_OFFLINE_INFO, KEY_OFFLINE_URL, KEY_OFFLINE_DAYLABEL},
                "", null, null, null, null);

        if (result != null && result.getCount() > 0) {
            result.moveToFirst();

            while (iterate) {
                ORFParser.ORFProgram program = new ORFParser.ORFProgram();

                program.id = Integer.parseInt(result.getString(result.getColumnIndex(KEY_OFFLINE_ID)));
                program.time = result.getString(result.getColumnIndex(KEY_OFFLINE_TIME));
                program.title = result.getString(result.getColumnIndex(KEY_OFFLINE_TITLE));
                program.shortTitle = result.getString(result.getColumnIndex(KEY_OFFLINE_SHORTTITLE));
                program.info = result.getString(result.getColumnIndex(KEY_OFFLINE_INFO));
                program.url = result.getString(result.getColumnIndex(KEY_OFFLINE_URL));
                program.dayLabel = result.getString(result.getColumnIndex(KEY_OFFLINE_DAYLABEL));
                program.isListened = this.isListened(String.valueOf(program.id));

                list.add(program);
                if (result.isLast()) {
                    iterate = false;
                } else {
                    result.moveToNext();
                }
            }
            result.close();
        }
        return list;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(OFFLINE_TABLE_CREATE);
        db.execSQL(LISTENED_TABLE_CREATE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
