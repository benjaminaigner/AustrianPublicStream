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


 Contribution:
 A big "thank you" to following projects/webpages providing sourcecode/libraries/information:

 ToxicBakery for its transformer library (https://github.com/ToxicBakery/ViewPagerTransforms)
 VideoLAN for its VLC library (https://wiki.videolan.org/Libvlc/)
 RomanNurik for the Android Asset Studio (https://romannurik.github.io/AndroidAssetStudio/)

 and of course all the experts on stackoverflow for helpful hints!
 **/

package systems.byteswap.publicstream;

import android.content.Context;
import android.database.DataSetObserver;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Adapter for the header (date and some text), necessary to support the sliding views and updates
 */
public class HeaderAdapter implements ListAdapter {
    private int headerPosition = 0;
    private LayoutInflater inflater;

    public HeaderAdapter(int headerPosition) {
        this.headerPosition = headerPosition;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return 1;
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
    public boolean hasStableIds() {
        return true;
    }

    public void setInflater(LayoutInflater inflater)
    {
        this.inflater = inflater;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.child_header, null);
        }

        String dateText = "";
        Calendar day = new GregorianCalendar();

        switch(this.headerPosition) {
            //first tab: offline programs
            case 1:
                dateText = "Offline Beiträge";
                break;
            //today's program
            case 2:
                dateText = "Heute, " + DateFormat.format("dd.MM.yyyy", day).toString();
                break;
            //all previous days
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                day.add(Calendar.DAY_OF_MONTH,(-headerPosition)+2);
                dateText = DateFormat.format("cccc, dd.MM.yyyy", day).toString();
                break;
            default:
                dateText = "Häää????";
                break;
        }


        // get the textView/ImageView reference and set the title/info
        TextView textView = (TextView) convertView.findViewById(R.id.textViewChildHeader );
        textView.setText(dateText);
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
