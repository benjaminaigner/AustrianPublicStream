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

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Fragment;
import android.widget.ExpandableListView;

import java.util.ArrayList;

/**
 * Basic fragment to store all information, even on runtime changes (resize/orientation change)
 */
public class MainFragment extends Fragment {


    public MainFragment() {
        // Required empty public constructor
    }

    private ArrayList<ORFParser.ORFProgram> programListToday;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus1;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus2;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus3;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus4;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus5;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus6;
    private ArrayList<ORFParser.ORFProgram> programListTodayMinus7;
    private ArrayList<ORFParser.ORFProgram> programListOffline;
    private MediaService mService;
    private ServiceConnection mConnection;
    private Intent mServiceIntent;
    private ProgramExpandableAdapter adapter;
    private ExpandableListView expandableList;

    private String textPlayButton;


    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }


    public ArrayList<ORFParser.ORFProgram> getProgramListToday() {
        return programListToday;
    }

    public void setProgramListToday(ArrayList<ORFParser.ORFProgram> programListToday) {
        this.programListToday = programListToday;
    }

    public ArrayList<ORFParser.ORFProgram> getProgramListTodayMinus1() {
        return programListTodayMinus1;
    }

    public void setProgramListTodayMinus1(ArrayList<ORFParser.ORFProgram> programListTodayMinus1) {
        this.programListTodayMinus1 = programListTodayMinus1;
    }

    public ArrayList<ORFParser.ORFProgram> getProgramListTodayMinus2() {
        return programListTodayMinus2;
    }

    public void setProgramListTodayMinus2(ArrayList<ORFParser.ORFProgram> programListTodayMinus2) {
        this.programListTodayMinus2 = programListTodayMinus2;
    }

    public ArrayList<ORFParser.ORFProgram> getProgramListTodayMinus3() {
        return programListTodayMinus3;
    }

    public void setProgramListTodayMinus3(ArrayList<ORFParser.ORFProgram> programListTodayMinus3) {
        this.programListTodayMinus3 = programListTodayMinus3;
    }

    public ArrayList<ORFParser.ORFProgram> getProgramListTodayMinus4() {
        return programListTodayMinus4;
    }

    public void setProgramListTodayMinus4(ArrayList<ORFParser.ORFProgram> programListTodayMinus4) {
        this.programListTodayMinus4 = programListTodayMinus4;
    }

    public ArrayList<ORFParser.ORFProgram> getProgramListTodayMinus5() {
        return programListTodayMinus5;
    }

    public void setProgramListTodayMinus5(ArrayList<ORFParser.ORFProgram> programListTodayMinus5) {
        this.programListTodayMinus5 = programListTodayMinus5;
    }

    public ArrayList<ORFParser.ORFProgram> getProgramListTodayMinus6() {
        return programListTodayMinus6;
    }

    public void setProgramListTodayMinus6(ArrayList<ORFParser.ORFProgram> programListTodayMinus6) {
        this.programListTodayMinus6 = programListTodayMinus6;
    }

    public ArrayList<ORFParser.ORFProgram> getProgramListTodayMinus7() {
        return programListTodayMinus7;
    }

    public void setProgramListTodayMinus7(ArrayList<ORFParser.ORFProgram> programListTodayMinus7) {
        this.programListTodayMinus7 = programListTodayMinus7;
    }

    public ArrayList<ORFParser.ORFProgram> getProgramListOffline() {
        return programListOffline;
    }

    public void setProgramListOffline(ArrayList<ORFParser.ORFProgram> programListOffline) {
        this.programListOffline = programListOffline;
    }

    public MediaService getMediaService() {
        return mService;
    }

    public void setMediaService(MediaService mService) {
        this.mService = mService;
    }

    public ServiceConnection getMediaConnection() {
        return mConnection;
    }

    public void setMediaConnection(ServiceConnection mConnection) {
        this.mConnection = mConnection;
    }

    public Intent getMediaServiceIntent() {
        return mServiceIntent;
    }

    public void setMediaServiceIntent(Intent mServiceIntent) {
        this.mServiceIntent = mServiceIntent;
    }

    public void setExpandableList(ExpandableListView expandableList) {
        this.expandableList = expandableList;
    }

    public void setAdapter(ProgramExpandableAdapter adapter) {
        this.adapter = adapter;
    }

    public String getTextPlayButton() {
        return textPlayButton;
    }

    public void setTextPlayButton(String textPlayButton) {
        this.textPlayButton = textPlayButton;
    }

    public ProgramExpandableAdapter getAdapter() {
        return adapter;
    }

    public ExpandableListView getExpandableList() {
        return expandableList;
    }
}
