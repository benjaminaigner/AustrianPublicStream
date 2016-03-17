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
    private Context context;

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

    public void setInflater(LayoutInflater inflater, Context context)
    {
        this.inflater = inflater;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.child_header, null);
        }

        String dateText = "";
        Calendar day = new GregorianCalendar();

        switch(this.headerPosition) {
            case 1:
                dateText = "Heute, " + DateFormat.format("dd.MM.yyyy", day).toString();
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                day.add(Calendar.DAY_OF_MONTH,(-headerPosition)+1);
                dateText = DateFormat.format("dd.MM.yyyy", day).toString();
                break;
            case 9:
                dateText = "Offline Beiträge";
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
