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

import android.content.Context;
import android.database.DataSetObserver;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Expandable list adapter, to show the program list
 * This adapter is based on a normal list adapter, building
 * a view for each program, consisting of:
 * -) Download icon (not shown if it is an offline program)
 * -) Short title
 * -) Description
 *
 * This adapter is used for each day, so every adapter contains one ArrayList of programs
 * (1 offline page, today's page and 7 days in the past)
 */

public class ProgramExpandableAdapter implements ListAdapter {
    private Context context;
    private LayoutInflater inflater;
    private int groupPosition = 0;

    //list of the programs on the current page
    private ArrayList<ORFParser.ORFProgram> listPrograms;

    //is this the offline page or not?
    private boolean isOffline;

    public ProgramExpandableAdapter(boolean offline) {
        this.isOffline = offline;
    }

    public void setInflater(LayoutInflater inflater, Context context)
    {
        this.inflater = inflater;
        this.context = context;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    //return the current count of list items or 0 if the list is null
    @Override
    public int getCount() {
        if(listPrograms != null) {
            return listPrograms.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    /**
     * Main view creator for each program of this list
     * @param position Current position in the list
     * @param convertView previous view, can be re-used
     * @param parent no idea...
     * @return the View of the current item
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //finalize a copy of the array list
        final ArrayList<ORFParser.ORFProgram> child;

        //either use the current list or create a new one
        if(listPrograms != null) {
            child = listPrograms;
        } else {
            child = new ArrayList<>(0);
        }

        TextView textView;

        //if the previous view cannot be used, create a new one from layout XML
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_view, null);
        }

        // get the textView/ImageView reference and set the title/info (first line)
        textView = (TextView) convertView.findViewById(R.id.textViewTitle);
        // find the download button
        ImageView imageViewDownload = (ImageView) convertView.findViewById(R.id.childDownloadImage);

        //either this is the offline list or not
        if(isOffline) {
            //if this is the offline list, write the day of the past program and the short title to the header
            textView.setText(child.get(position).dayLabel + " - " + child.get(position).shortTitle);
            //and also make the download button invisible (makes no sense to download it again)
            imageViewDownload.setVisibility(View.INVISIBLE);
        } else {
            //if this is the remote list, show the original on-air time and the short title
            textView.setText(child.get(position).time + " - " + child.get(position).shortTitle);

            // set the ClickListener to handle the click events on the download button
            imageViewDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Create calendar object (today)
                    Calendar today = new GregorianCalendar();
                    today.add(Calendar.DAY_OF_MONTH, -groupPosition);
                    ((MainActivity)context).programDownloadClickListener(child.get(position), DateFormat.format("dd.MM.yyyy", today).toString());
                }
            });
            //show the download button
            imageViewDownload.setVisibility(View.VISIBLE);
        }

        //always: write the info text for each program (offline and remote)
        textView = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        textView.setText(child.get(position).info);

        // set the ClickListener to handle the click event on child item (we will play the selected
        // program via the MainActivity)
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)context).programClickListener(child.get(position));
            }
        });

        //either use the "delete" handler for longclicks (offline programs)
        //or the "download" handler (remote programs)
        if(isOffline) {
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((MainActivity) context).programLongClickListener(child.get(position), true, "");
                    return true;
                }
            });
        } else {
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Create calendar object (today)
                    Calendar today = new GregorianCalendar();
                    ((MainActivity) context).programLongClickListener(child.get(position), false, DateFormat.format("dd.MM.yyyy", today).toString());
                    return true;
                }
            });
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    //save the program list to this adapter instance
    public void setListPrograms(ArrayList<ORFParser.ORFProgram> listPrograms) {
        this.listPrograms = listPrograms;
    }
}
