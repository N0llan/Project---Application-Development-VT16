package se.miun.daje1400.bathingsites;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    public static final String degreesSymbol = "\u00B0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), NewBathingSiteActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BathingSitesView bathingSitesView = (BathingSitesView) getSupportFragmentManager().findFragmentById(R.id.bathingSiteFragment).getView().findViewById(R.id.bathingSitesView);
        bathingSitesView.setNumBathingSites();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_settings: getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).addToBackStack(null).commit();
                return true;
            case R.id.action_download_bathsite: Intent intent = new Intent(this, DownloadBathingSites.class);
                startActivity(intent);
                return true;
            case R.id.action_map: Intent intent1 = new Intent(this, MapsActivity.class);
                startActivity(intent1);
            default: return super.onOptionsItemSelected(item);
        }
    }
}
