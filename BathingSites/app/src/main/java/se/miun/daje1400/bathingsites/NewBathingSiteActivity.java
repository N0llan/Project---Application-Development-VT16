package se.miun.daje1400.bathingsites;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NewBathingSiteActivity extends AppCompatActivity {

    private EditText name, description, address, longitude, latitude, water_temp, water_temp_date;
    private RatingBar grade;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bathing_site);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = new Database(getApplicationContext());
        database.open();
        initViews();
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }

    //init the views
    public void initViews(){
        name = (EditText) findViewById(R.id.name);
        description = (EditText) findViewById(R.id.description);
        address = (EditText) findViewById(R.id.address);
        longitude = (EditText) findViewById(R.id.longitude);
        latitude = (EditText) findViewById(R.id.latitude);
        grade = (RatingBar) findViewById(R.id.grade);
        //Sets listener on ratingbar so value cant be lower then 1
        grade.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(rating < 1.0f){
                    ratingBar.setRating(1.0f);
                }
            }
        });
        water_temp = (EditText) findViewById(R.id.water_temp);
        water_temp_date = (EditText) findViewById(R.id.water_temp_date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_bathing_site, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_clear: clearInput();
                return true;
            case R.id.action_save: saveInput();
                return true;
            case R.id.action_weather: getCurrentWeather();
                return true;
            case R.id.action_settings: getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).addToBackStack(null).commit();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    //Function clears input
    public void clearInput(){
        name.setText("");
        description.setText("");
        address.setText("");
        longitude.setText("");
        latitude.setText("");
        grade.setRating(0);
        water_temp.setText("");
        water_temp_date.setText("");
    }

    //Function saves input and checks required fields
    public void saveInput() {
        boolean correctInput = true;
        if (name.getText().toString().equals("")) {
            name.setError(getResources().getString(R.string.name_error));
            correctInput = false;
        }
        if (isEmpty(address) && (isEmpty(latitude) || isEmpty(longitude))) {
            if (isEmpty(address)) {
                address.setError(getResources().getString(R.string.address_error));
            }
            if (isEmpty(longitude)) {
                longitude.setError(getResources().getString(R.string.longitude_error));
            }
            if (isEmpty(latitude)) {
                latitude.setError(getResources().getString(R.string.latitude_error));
            }
            correctInput = false;

        }

        if (correctInput) {
            if(!database.exists(longitude.getText().toString(),latitude.getText().toString())){
                List<String> bathsiteData = new ArrayList<>();
                bathsiteData.add(name.getText().toString());
                bathsiteData.add(description.getText().toString());
                bathsiteData.add(address.getText().toString());
                bathsiteData.add(longitude.getText().toString());
                bathsiteData.add(latitude.getText().toString());
                bathsiteData.add(Double.toString(grade.getRating()));
                bathsiteData.add(water_temp.getText().toString());
                bathsiteData.add(water_temp_date.getText().toString());

                database.saveBathingSite(bathsiteData);
                bathsiteData.clear();
                Toast.makeText(getBaseContext(),getResources().getString(R.string.save_succesful), Toast.LENGTH_SHORT).show();
                clearInput();
                finish();

            } else {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.bathsite_exists), Toast.LENGTH_SHORT).show();
            }

        } else{
            Toast.makeText(getBaseContext(),getResources().getString(R.string.not_all_fields),Toast.LENGTH_SHORT).show();
        }
    }

    //Checks if a textview is empty
    public boolean isEmpty(EditText editText){
        return editText.getText().toString().equals("");
    }

    //Function calls fragment that downloads weather info to show. Also format the location based
    // on latitude & longitude or address
    public void getCurrentWeather(){
        DownloadWeather fragment = new DownloadWeather();
        Bundle bundle = new Bundle();
        if (!isEmpty(latitude) && !isEmpty(longitude)){
            bundle.putString("location",longitude.getText().toString() + "|" + latitude.getText().toString());
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(),"Download Fragment");
        } else if(!isEmpty(address)){
            bundle.putString("location",address.getText().toString());
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(),"Download Fragment");
        } else {
            Toast.makeText(getBaseContext(), "No location avalible", Toast.LENGTH_SHORT).show();
        }

    }
}
