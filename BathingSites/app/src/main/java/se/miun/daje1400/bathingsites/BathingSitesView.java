package se.miun.daje1400.bathingsites;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BathingSitesView extends LinearLayout {
    private TextView numBathingSites;
    private Database database;

    public BathingSitesView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public BathingSitesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    //Sets the text that shows number of bathingsites in database
    public void setNumBathingSites(){
        numBathingSites.setText(getNumBathingSites() + " " + getResources().getString(R.string.app_name));
    }

    //Gets number of bathingsites from database
    public int getNumBathingSites(){
        invalidate();
        database = new Database(getContext().getApplicationContext());
        database.open();
        int numBathingsites = database.getNumOfBathsites();
        database.close();
        return  numBathingsites;
    }
    //Init function for class
    private void init(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater layout = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addView(layout.inflate(R.layout.sample_bathing_sites_view, null));
        numBathingSites = (TextView) findViewById(R.id.numBathingSites);
        setNumBathingSites();
    }
}
