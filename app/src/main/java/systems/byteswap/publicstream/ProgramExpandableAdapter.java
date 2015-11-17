package systems.byteswap.publicstream;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Expandable list adapter, to show the program list
 */

//TODO: clicklistener
    //TODO: grafisch etwas aufpeppen...
public class ProgramExpandableAdapter extends BaseExpandableListAdapter
{

    private Activity activity;
    private LayoutInflater inflater;

    private ArrayList<ORFParser.ORFProgram> listToday;
    private ArrayList<ORFParser.ORFProgram> listTodayMinus1;
    private ArrayList<ORFParser.ORFProgram> listTodayMinus2;
    private ArrayList<ORFParser.ORFProgram> listTodayMinus3;
    private ArrayList<ORFParser.ORFProgram> listTodayMinus4;
    private ArrayList<ORFParser.ORFProgram> listTodayMinus5;
    private ArrayList<ORFParser.ORFProgram> listTodayMinus6;
    private ArrayList<ORFParser.ORFProgram> listTodayMinus7;

    // constructor
    public ProgramExpandableAdapter(ArrayList<ORFParser.ORFProgram> programListToday,
                                    ArrayList<ORFParser.ORFProgram> programListTodayMinus1,
                                    ArrayList<ORFParser.ORFProgram> programListTodayMinus2,
                                    ArrayList<ORFParser.ORFProgram> programListTodayMinus3,
                                    ArrayList<ORFParser.ORFProgram> programListTodayMinus4,
                                    ArrayList<ORFParser.ORFProgram> programListTodayMinus5,
                                    ArrayList<ORFParser.ORFProgram> programListTodayMinus6,
                                    ArrayList<ORFParser.ORFProgram> programListTodayMinus7)
    {
        listToday = programListToday;
        listTodayMinus1 = programListTodayMinus1;
        listTodayMinus2 = programListTodayMinus2;
        listTodayMinus3 = programListTodayMinus3;
        listTodayMinus4 = programListTodayMinus4;
        listTodayMinus5 = programListTodayMinus5;
        listTodayMinus6 = programListTodayMinus6;
        listTodayMinus7 = programListTodayMinus7;
    }

    public void setInflater(LayoutInflater inflater, Activity activity)
    {
        this.inflater = inflater;
        this.activity = activity;
    }

    // method getChildView is called automatically for each child view.
    //  Implement this method as per your requirement
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        final ArrayList<ORFParser.ORFProgram> child;
        switch(groupPosition) {
            case 0:
                child = listToday;
                break;
            case 1:
                child = listTodayMinus1;
                break;
            case 2:
                child = listTodayMinus2;
                break;
            case 3:
                child = listTodayMinus3;
                break;
            case 4:
                child = listTodayMinus4;
                break;
            case 5:
                child = listTodayMinus5;
                break;
            case 6:
                child = listTodayMinus6;
                break;
            case 7:
                child = listTodayMinus7;
                break;
            default:
                child = null;
                break;
        }

        TextView textView = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_view, null);
        }

        // get the textView reference and set the title/info
        textView = (TextView) convertView.findViewById(R.id.textViewTitle);
        textView.setText(child.get(childPosition).time + " - " + child.get(childPosition).shortTitle);
        textView = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        textView.setText(child.get(childPosition).info);

        // set the ClickListener to handle the click event on child item
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.e("PUBLICSTREAM","Click: " + child.get(childPosition).shortTitle);
            }
        });
        return convertView;
    }

    // method getGroupView is called automatically for each parent item
    // Implement this method as per your requirement
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.parent_view, null);
        }

        //Create calendar object (today)
        Calendar today = new GregorianCalendar();
        android.text.format.DateFormat df = new android.text.format.DateFormat();

        TextView itemName = (TextView) convertView.findViewById(R.id.dateName);

        //TODO: richtiges datum einfügen...
        switch(groupPosition) {
            case 0:
                itemName.setText("Heute " + listToday.size() + " Beiträge");
                break;
            case 1:
                today.add(Calendar.DAY_OF_MONTH,-1);
                itemName.setText(df.format("dd.MM.yyyy", today).toString() + " " + listTodayMinus1.size() + " Beiträge");
                break;
            case 2:
                today.add(Calendar.DAY_OF_MONTH,-2);
                itemName.setText(df.format("dd.MM.yyyy", today).toString() + " " + listTodayMinus2.size() + " Beiträge");
                break;
            case 3:
                today.add(Calendar.DAY_OF_MONTH,-3);
                itemName.setText(df.format("dd.MM.yyyy", today).toString() + " " + listTodayMinus3.size() + " Beiträge");
                break;
            case 4:
                today.add(Calendar.DAY_OF_MONTH,-4);
                itemName.setText(df.format("dd.MM.yyyy", today).toString() + " " + listTodayMinus4.size() + " Beiträge");
                break;
            case 5:
                today.add(Calendar.DAY_OF_MONTH,-5);
                itemName.setText(df.format("dd.MM.yyyy", today).toString() + " " + listTodayMinus5.size() + " Beiträge");
                break;
            case 6:
                today.add(Calendar.DAY_OF_MONTH,-6);
                itemName.setText(df.format("dd.MM.yyyy", today).toString() + " " + listTodayMinus6.size() + " Beiträge");
                break;
            case 7:
                today.add(Calendar.DAY_OF_MONTH,-7);
                itemName.setText(df.format("dd.MM.yyyy", today).toString() + " " + listTodayMinus7.size() + " Beiträge");
                break;


        }
        //convertView.setChecked(isExpanded);

        return convertView;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        switch(groupPosition) {
            case 0:
                return listToday.size();
            case 1:
                return listTodayMinus1.size();
            case 2:
                return listTodayMinus2.size();
            case 3:
                return listTodayMinus3.size();
            case 4:
                return listTodayMinus4.size();
            case 5:
                return listTodayMinus5.size();
            case 6:
                return listTodayMinus6.size();
            case 7:
                return listTodayMinus7.size();
            default:
                return 0;

        }
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return null;
    }

    @Override
    public int getGroupCount()
    {
        return 8;
    }

    @Override
    public void onGroupCollapsed(int groupPosition)
    {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition)
    {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return 0;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return false;
    }

    public void update(ArrayList<ORFParser.ORFProgram> programListToday,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus1,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus2,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus3,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus4,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus5,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus6,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus7) {
        listToday = programListToday;
        listTodayMinus1 = programListTodayMinus1;
        listTodayMinus2 = programListTodayMinus2;
        listTodayMinus3 = programListTodayMinus3;
        listTodayMinus4 = programListTodayMinus4;
        listTodayMinus5 = programListTodayMinus5;
        listTodayMinus6 = programListTodayMinus6;
        listTodayMinus7 = programListTodayMinus7;
    }
}
