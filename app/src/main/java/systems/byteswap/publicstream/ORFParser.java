/**
 Copyright:
 2015/2016 Benjamin Aigner

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

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * ORF JSON Parser
 *
 * This class is able to parse the JSON input of a full program list.
 * Usually retrieved via: http://oe1.orf.at/programm/konsole/tag/<yyyymmdd>
 *
 * In addition this class provides the offline programs (accessed via an XML file)
 */
public class ORFParser {
    /** array list of this Parser instance, containing one day */
    private ArrayList<ORFProgram> programList;
    /** base URL, where the programs are stored (the JSON list */
    public final static String ORF_FULL_BASE_URL = "http://oe1.orf.at/programm/konsole/tag/";
    /** live stream URL */
    public final static String ORF_LIVE_URL = "http://mp3stream3.apasf.apa.at:8000/;stream.mp3";

    /* debug settings */
    private final static boolean D = false;
    private final static String TAG = "ORFParser";

    /** storage provider to access the SQLite DB */
    private StorageProvider store;

    /**
     * This method fetches the JSON string from a defined URL (base URL + day string)
     * The combined lines are handled to parse() and the result of the parser is returned.
     * If nothing went wrong, the programList is filled with the parsed ORFPrograms
     *
     * @param orfURL Full URL to fetch
     * @return result of the parse operation, "" if everything went fine, the error string otherwise
     * @throws IOException Is thrown if there are problems regarding the HTTP connection
     */
    private String fetchURL(URL orfURL) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(orfURL.toString());
        if(D) Log.d(TAG,"HTTP fetch of URL " + orfURL.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return this.parse(result.toString());
    }

    /**
     * This method is used to parse the JSON string, received from the HTTP server, into
     * the object representation (ORFParser.ORFProgram).
     * In addition, the DB is fetched for the listened flag of this program
     *
     * @param s JSON string to be parsed (handled by fetchURL)
     * @return Result string, "" if everything went fine, the error message otherwise
     */
    private String parse(String s) {
        //recreate program list each time
        programList = new ArrayList<>();

        try {
            //open JSON object
            JSONObject programOriginal = new JSONObject(s);
            //load only list item (contains all single programs)
            JSONArray programListJSON = programOriginal.getJSONArray("list");

            //iterate
            for(int i = 0; i < programListJSON.length(); i++) {
                //load one program item of the list
                JSONObject programItems = programListJSON.getJSONObject(i);
                //create a temp object
                ORFProgram currentProgram = new ORFProgram();

                //add all parameters
                currentProgram.id = Integer.valueOf((String)programItems.get("id"));
                currentProgram.time = (String)programItems.get("time");
                currentProgram.title = (String)programItems.get("title");
                currentProgram.shortTitle = (String)programItems.get("short_title");
                currentProgram.info = (String)programItems.get("info");
                currentProgram.url = (String)programItems.get("url_stream");
                currentProgram.dayLabel = (String)programItems.get("day_label");
                if(store != null) {
                    currentProgram.isListened = store.isListened(String.valueOf(currentProgram.id), currentProgram.dayLabel);

                }

                //add temp program to list
                programList.add(currentProgram);
            }
            if(D) Log.d(TAG,"Parsed " + programList.size() + " remote programs");

        } catch (JSONException e) {
            Log.e(TAG, "Liste passt nicht...");
            return "Liste ist ungültig, bitte beim ORF beschweren...";
        }
        return "";
    }

    /**
     * This method is used to fetch the available programs for one day.
     * In addition, the storage provider is necessary to enable the search for the listened flag
     *
     * @param day The day which should be fetched (available: today minus 7 days)
     * @param store DB connector
     * @return The array list of all available programs for this day
     */
    public ArrayList<ORFProgram> getProgramsForDay(Date day, StorageProvider store) {
        Calendar dayCalendar = new GregorianCalendar();
        dayCalendar.setTime(day);
        this.store = store;

        if(D) Log.d(TAG,"Fetching/parsing for day: " + day.toString());
        try {
            String month = String.format("%1$02d", dayCalendar.get(Calendar.MONTH)+1);
            String daynr = String.format("%1$02d", dayCalendar.get(Calendar.DAY_OF_MONTH));

            String fullURLString = ORF_FULL_BASE_URL + dayCalendar.get(Calendar.YEAR) + month + daynr;
            this.fetchURL(new URL(fullURLString));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.programList;
    }

    /**
     * Static class as representation of an ORF program.
     */
    public static class ORFProgram {
        //unique id, provided by the remote list
        public int id;
        //original time, when it was broadcasted
        public String time;
        //full title
        public String title;
        //short title
        public String shortTitle;
        //program info
        public String info;
        //URL (either the remote one, or the local one for offline programs)
        public String url;
        //label of the day (not used, just provided by the remote list)
        public String dayLabel;
        //flag of the listened state, true if the program was listened to the end
        public boolean isListened;
    }
}
