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
 */
public class ORFParser {
    private ArrayList<ORFProgram> programList;
    public static String ORF_FULL_BASE_URL = "http://oe1.orf.at/programm/konsole/tag/";

    private String fetchURL(URL orfURL) throws IOException {
        //flag of being in the program list
        //boolean inList = false;


        StringBuilder result = new StringBuilder();
        URL url = new URL(orfURL.toString());
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


                //add temp program to list
                programList.add(currentProgram);
            }

        } catch (JSONException e) {
            Log.e("ORFParser", "Liste passt nicht...");
            return "Liste ist ungültig, bitte beim ORF beschweren...";
        }
        return "";
    }

    public ArrayList<ORFProgram> getProgramsForDay(Date day) {
        Calendar dayCalendar = new GregorianCalendar();
        dayCalendar.setTime(day);

        try {
            String fullURLString = ORF_FULL_BASE_URL + dayCalendar.get(Calendar.YEAR) + String.valueOf(Integer.valueOf(dayCalendar.get(Calendar.MONTH))+1) + dayCalendar.get(Calendar.DAY_OF_MONTH);
            this.fetchURL(new URL(fullURLString));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.programList;
    }

    public class ORFProgram {
        public int id;
        public String length;
        public String time;
        public String title;
        public String shortTitle;
        public String info;
        public String url;
    }
}
