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
 */

public class ProgramExpandableAdapter implements ListAdapter {
    private Context context;
    private LayoutInflater inflater;
    private int groupPosition = 0;

    private ArrayList<ORFParser.ORFProgram> listPrograms;
    //private ArrayList<ORFParser.ORFProgram> listPrograms[] = new ArrayList[9];

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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ArrayList<ORFParser.ORFProgram> child;
        if(listPrograms != null) {
            child = listPrograms;
        } else {
            child = new ArrayList<>(0);
        }

        TextView textView;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_view, null);
        }

        // get the textView/ImageView reference and set the title/info
        textView = (TextView) convertView.findViewById(R.id.textViewTitle);
        ImageView imageViewDownload = (ImageView) convertView.findViewById(R.id.childDownloadImage);
        if(isOffline) {
            textView.setText(child.get(position).dayLabel + " - " + child.get(position).shortTitle);
            imageViewDownload.setVisibility(View.INVISIBLE);
        } else {
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
            imageViewDownload.setVisibility(View.VISIBLE);
        }
        textView = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        textView.setText(child.get(position).info);

        // set the ClickListener to handle the click event on child item
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ((MainActivity)context).programClickListener(child.get(position));
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(groupPosition == 8) {
                    ((MainActivity)context).programLongClickListener(child.get(position), true, "");
                } else {
                    //Create calendar object (today)
                    Calendar today = new GregorianCalendar();
                    ((MainActivity)context).programLongClickListener(child.get(position), false, DateFormat.format("dd.MM.yyyy", today).toString());
                }
                return true;
            }
        });
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

    public void setListPrograms(ArrayList<ORFParser.ORFProgram> listPrograms) {
        this.listPrograms = listPrograms;
    }
}
