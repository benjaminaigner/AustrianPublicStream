package systems.byteswap.publicstream;

import android.os.Bundle;
import android.app.Fragment;
import java.util.ArrayList;

/**
 * Basic fragment to store all list, even on runtime changes (resize/orientation change)
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
}