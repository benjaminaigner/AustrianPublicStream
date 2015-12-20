package systems.byteswap.publicstream;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Expandable list adapter, to show the program list
 */

//TODO: bei click farbig hinterlegen...
public class ProgramExpandableAdapter extends BaseExpandableListAdapter
{

    private MainActivity activity;
    private LayoutInflater inflater;

    private ArrayList<ORFParser.ORFProgram> listPrograms[] = new ArrayList[9];

    public void setInflater(LayoutInflater inflater, MainActivity activity)
    {
        this.inflater = inflater;
        this.activity = activity;
    }

    // method getChildView is called automatically for each child view.
    //  Implement this method as per your requirement
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        final ArrayList<ORFParser.ORFProgram> child;
        if(listPrograms != null && listPrograms[groupPosition] != null) {
            child = listPrograms[groupPosition];
        } else {
            child = null;
        }

        TextView textView = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_view, null);
        }

        // get the textView/ImageView reference and set the title/info
        textView = (TextView) convertView.findViewById(R.id.textViewTitle);
        ImageView imageViewDownload = (ImageView) convertView.findViewById(R.id.childDownloadImage);
        if(groupPosition == 8) {
            textView.setText(child.get(childPosition).dayLabel + " - " + child.get(childPosition).shortTitle);
            imageViewDownload.setVisibility(View.INVISIBLE);
        } else {
            textView.setText(child.get(childPosition).time + " - " + child.get(childPosition).shortTitle);

            // set the ClickListener to handle the click events on the download button
            imageViewDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Create calendar object (today)
                    Calendar today = new GregorianCalendar();
                    android.text.format.DateFormat df = new android.text.format.DateFormat();
                    today.add(Calendar.DAY_OF_MONTH, -groupPosition);
                    activity.programDownloadClickListener(child.get(childPosition), df.format("dd.MM.yyyy", today).toString());
                }
            });
        }
        textView = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        textView.setText(child.get(childPosition).info);

        // set the ClickListener to handle the click event on child item
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                activity.programClickListener(child.get(childPosition));
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

        switch(groupPosition) {
            case 0:
                if(listPrograms != null && listPrograms[groupPosition] != null) {
                    itemName.setText("Heute: " + listPrograms[groupPosition].size() + " Beiträge");
                } else {
                    itemName.setText("Heute: 0 Beiträge");
                }
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                today.add(Calendar.DAY_OF_MONTH,-groupPosition);
                if(listPrograms != null && listPrograms[groupPosition] != null) {
                    itemName.setText(df.format("dd.MM.yyyy", today).toString() + ": " + listPrograms[groupPosition].size() + " Beiträge");
                } else {
                    itemName.setText(df.format("dd.MM.yyyy", today).toString() + ": 0 Beiträge");
                }
                break;
            case 8:
                if(listPrograms != null && listPrograms[groupPosition] != null) {
                    itemName.setText("Offline Beiträge:" + listPrograms[groupPosition].size());
                } else {
                    itemName.setText("Offline Beiträge: 0");
                }
                break;
            default:
                itemName.setText("???");
                break;
        }
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
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                if(listPrograms != null && listPrograms[groupPosition] != null) {
                    return listPrograms[groupPosition].size();
                } else {
                    return 0;
                }
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
        return 9;
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
        return true;
    }

    public void update(ArrayList<ORFParser.ORFProgram> programListToday,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus1,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus2,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus3,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus4,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus5,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus6,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinus7,
                       ArrayList<ORFParser.ORFProgram> programListTodayMinusOffline) {
        listPrograms[0] = programListToday;
        listPrograms[1] = programListTodayMinus1;
        listPrograms[2] = programListTodayMinus2;
        listPrograms[3] = programListTodayMinus3;
        listPrograms[4] = programListTodayMinus4;
        listPrograms[5] = programListTodayMinus5;
        listPrograms[6] = programListTodayMinus6;
        listPrograms[7] = programListTodayMinus7;
        listPrograms[8] = programListTodayMinusOffline;
    }

}
