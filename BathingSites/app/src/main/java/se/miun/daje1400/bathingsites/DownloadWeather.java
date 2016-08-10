package se.miun.daje1400.bathingsites;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class DownloadWeather extends DialogFragment {
    private String[] condition, temp, img;
    private Bundle thisBundle;
    private Drawable drawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.download_weather, container, false);
        getDialog().setTitle(getResources().getString(R.string.downloading_weather));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        thisBundle = this.getArguments();
        String locationURL = sharedPreferences.getString(getResources().getString(R.string.get_weather_url), getResources().getString(R.string.default_weather_url)) + "?location=" +
                thisBundle.getString("location") + "&language=EN";
        new DownloadFileFromURL().execute(locationURL);
        return rootView;
    }

    private class DownloadFileFromURL extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //Downloads weather data
        protected String doInBackground(String... f_url) {
            String content;
            try {
                URL url = new URL(f_url[0]);
                condition = new String[2];
                temp = new String[2];
                img = new String[2];

                BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));    //Skapar inputstream

                while ((content = input.readLine()) != null) {        //Medan vi laddar ner filen
                    if (content.contains("condition")) {
                        condition = content.split(":");
                        condition[1] = condition[1].replace("<br>","");
                    } else if (content.contains("temp_c")) {
                        temp = content.split(":");
                        temp[1] = temp[1].replace("<br>", "");
                    } else if (content.contains("image")) {
                        img = content.split(":", 2);
                        img[1] = img[1].replace("<br>","");

                        try {
                            InputStream is = (InputStream) new URL(img[1]).getContent();
                            drawable = Drawable.createFromStream(is, "src");

                        } catch (Exception e) {
                            Log.d("Drawable error", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            dismiss();
            if (!temp[1].equals("null")){
                createWeatherDialog();
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.error_reading_weather), Toast.LENGTH_SHORT).show();
            }

        }

        //Creates and shows the weatherdialog
        public void createWeatherDialog(){
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity()).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }
            );
            dialog.setTitle(getResources().getString(R.string.current_weather));
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.fragment_show_current_weather, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.currentWeatherImg);
            imageView.setImageDrawable(drawable);
            TextView textView = (TextView) view.findViewById(R.id.degrees);
            textView.setText(temp[1]+MainActivity.degreesSymbol);
            textView = (TextView) view.findViewById(R.id.weatherType);
            textView.setText(condition[1]);
            dialog.setView(view);
            dialog.show();
        }
    }
}

