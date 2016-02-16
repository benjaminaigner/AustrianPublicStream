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

    private String dayLabel;
    private boolean isToday;
    private MainActivity activity;
    private LayoutInflater inflater;

    private ArrayList<ORFParser.ORFProgram> listPrograms;
    //private ArrayList<ORFParser.ORFProgram> listPrograms[] = new ArrayList[9];

    private boolean isOffline;

    public ProgramExpandableAdapter(boolean offline, boolean today, String dayLabel) {
        this.isOffline = offline;
        this.isToday = today;
        this.dayLabel = dayLabel;
    }

    public void setInflater(LayoutInflater inflater, MainActivity activity)
    {
        this.inflater = inflater;
        this.activity = activity;
    }

    // method getChildView is called automatically for each child view.
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        final ArrayList<ORFParser.ORFProgram> child;
        if(listPrograms != null) {
            child = listPrograms;
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
        if(isOffline) {
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
            imageViewDownload.setVisibility(View.VISIBLE);
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

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(groupPosition == 8) {
                    activity.programLongClickListener(child.get(childPosition),true, "");
                } else {
                    //Create calendar object (today)
                    Calendar today = new GregorianCalendar();
                    android.text.format.DateFormat df = new android.text.format.DateFormat();
                    activity.programLongClickListener(child.get(childPosition),false, df.format("dd.MM.yyyy", today).toString());
                }
                return true;
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
        
        if(isToday) {
            if(listPrograms != null) {
                itemName.setText("  Heute: " + listPrograms.size() + " Beiträge");
            } else {
                itemName.setText("  Heute: 0 Beiträge");
            }
        }

        if(isOffline) {
            if(listPrograms != null) {
                itemName.setText("  Offline Beiträge: " + listPrograms.size());
            } else {
                itemName.setText("  Offline Beiträge: 0");
            }
        }

        if(!isToday && !isOffline) {
            if(listPrograms != null) {
                itemName.setText("  " + dayLabel + ": " + listPrograms.size() + " Beiträge");
            } else {
                itemName.setText("  " + dayLabel + ": 0 Beiträge");
            }
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
        if(listPrograms != null) {
            return listPrograms.size();
        } else {
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
        return 1;
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

    public void update(ArrayList<ORFParser.ORFProgram> programList) {
        this.listPrograms = programList;
    }

    public void setDayLabel(String label) {
        this.dayLabel = label;
    }

    public String getDayLabel() {
        return this.dayLabel;
    }
}
