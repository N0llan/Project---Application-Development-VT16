package se.miun.daje1400.bathingsites;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class DownloadBathingSites extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private String URLL;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        WebView webView = new WebView(this);
        URLL = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
        setContentView(webView);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                new DownloadFileFromURL().execute(url); //Startar nedladdningen
            }
        });

        webView.setWebViewClient(new webViewClient());
        webView.loadUrl(sharedPreferences.getString(getResources().getString(R.string.get_bathingsites_url),getResources().getString(R.string.default_bathingsites_url)));
        database = new Database(getApplicationContext());
        initProgressDialog();

    }

    //Init the dialog
    public void initProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.download_file));
        progressDialog.setProgress(0);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
                    //I chose to set cancelable to false since the asynctask is doing queries to database.
                    //On emulator this resulted in errors since multiple queries to the same database happend
                    //at the same time if this is set to true. I didnt notice anything on a real device.
        progressDialog.setCancelable(false);
    }

    //-------------------------- webViewClient
    private class webViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            view.loadUrl(url);
            return true;
        }
    }

    //-------------------------- DownbloadFileFromURL
    private class DownloadFileFromURL extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();  //Shows dialog
        }

        //Handles background functionality, downloads and saves to database.
        protected String doInBackground(String... f_url){
            int count;
            database.open();
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();   //Ansluter
                int lengtOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);    //Skapar inputstream
                OutputStream outputStream = new FileOutputStream(URLL+"/"+Uri.parse(f_url[0]).getLastPathSegment());
                byte data [] = new byte[1024];
                long total = 0;

                while ((count = input.read(data))!= -1){        //Medan vi laddar ner filen
                    total += count;
                    publishProgress("" + (int) ((total*100)/lengtOfFile));  //Uppdatera progressbaren
                    outputStream.write(data,0,count);           //Skriv fildata
                }
                //SystemClock.sleep(1000);
                readInput(Uri.parse(f_url[0]).getLastPathSegment());
                new File(URLL+"/"+Uri.parse(f_url[0]).getLastPathSegment()).delete();   //radera filen
                outputStream.flush();
                outputStream.close();
                input.close();
                database.close();
            } catch (Exception e){
                Log.e("Error back: ", e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(String... progress){
                progressDialog.setProgress(Integer.parseInt(progress[0]));
            }

        protected void onPostExecute(String file_url){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Changes the progressdialogs message and progress
                    progressDialog.setMessage(getResources().getString(R.string.download_file));
                    progressDialog.setProgress(0);
                }
            });
            progressDialog.dismiss();
        }

        //Function reads downloaded file and saves to databse
        public void readInput(String file){
            try{
                //Changes the progressdialogs message and progress
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage(getResources().getString(R.string.saving_bathsites));
                        progressDialog.setProgress(0);
                    }
                });
                String tmp;
                File dataFile = new File(URLL,file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile),"windows-1252"));
                long fileLength = dataFile.length();
                long total = 0;
                while ((tmp = reader.readLine()) != null){
                    total += tmp.getBytes().length;
                    String data[] = tmp.split(",",4);
                    List<String> toDatabase = new ArrayList<>();
                    toDatabase.add(data[2].replace("\"",""));
                    toDatabase.add(null);
                    if (data.length == 4){
                        toDatabase.add(data[3].replace("\"",""));
                    } else {
                        toDatabase.add("");
                    }
                    toDatabase.add(data[0].replace("\"",""));
                    toDatabase.add(data[1].replace("\"",""));
                    toDatabase.add(null);
                    toDatabase.add(null);
                    toDatabase.add(null);
                    if (!database.exists(toDatabase.get(3),toDatabase.get(4))){
                        database.saveBathingSite(toDatabase);
                    }
                    publishProgress("" + (int) ((total*100)/fileLength));
                    toDatabase.clear();
            }
            } catch (Exception e){
                Log.d("Read error", e.getMessage());
            }

        }
    }
}

