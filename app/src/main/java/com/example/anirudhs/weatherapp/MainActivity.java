package com.example.anirudhs.weatherapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = null;
    Handler handler = new Handler();
    void Displayerror(final String Errortext){


        handler.post(new Runnable() {

            @Override
            public void run() {
                 foreCast.setEnabled(false);
                Toast.makeText(MainActivity.this, Errortext, Toast.LENGTH_SHORT).show();

            }

        });
    }

    TextView weatherText,cityName;


    Button clickMe,foreCast;
    ImageView weatherIcon;
    private String imageUrl;
    String cityId,city;



    private ProgressBar loadBar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherText = (TextView)findViewById(R.id.weatherText);

        clickMe = (Button)findViewById(R.id.clickMe);
        foreCast = (Button)findViewById(R.id.foreCast);
        weatherIcon = (ImageView)findViewById(R.id.weatherIcon);
        cityName = (TextView)findViewById(R.id.cityName);

        loadBar = (ProgressBar)findViewById(R.id.loadBar);
        loadBar.setVisibility(View.GONE);
        foreCast.setVisibility(View.GONE);
        EditText editor = new EditText(this);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                city = (String) place.getName();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });



        clickMe.setOnClickListener(
                new Button.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        loadBar.setVisibility(View.VISIBLE);
                        weatherIcon.setVisibility(View.GONE);
                        cityName.setVisibility(View.GONE);
                        weatherText.setVisibility(View.GONE);

                        new weatherTask().execute("http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=5d8758ba5d7b97824054aa8ece907d5c");


                    }
                }
        );
        foreCast.setOnClickListener(
                new Button.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this,ForecastActivity.class);

                         i.putExtra("cityName",cityId);
                        startActivity(i);

                    }
                }
        );

    }







    public class weatherTask extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... params) {
            BufferedReader reader = null;

            HttpURLConnection connection = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                if(connection.getResponseCode()==200){
                    enableButton();
                    connection.connect();
                }
                else
                {   VisibilityFunc();
                    Displayerror("Invalid City Name");

                    return null;
                }
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);

                }

                String finalJSON = buffer.toString();

                try{
                    JSONObject parentObject = new JSONObject(finalJSON);
                    JSONArray weather = parentObject.getJSONArray("weather");
                    JSONObject weatherObject = weather.getJSONObject(0);
                    JSONObject main = parentObject.getJSONObject("main");


                    String cityName = parentObject.getString("name");
                    String description = weatherObject.getString("description");
                    String iconId = weatherObject.getString("icon");
                    float temp = main.getInt("temp");
                    int pressure = main.getInt("pressure");
                    int humidity = main.getInt("humidity");
                    float temp_min = main.getInt("temp_min");
                    float temp_max = main.getInt("temp_max");
                    long  cityidno = parentObject.getInt("id");
                    cityId = "" + cityidno;



                    temp = temp - 273;
                    temp_max = temp_max - 273;
                    temp_min = temp_min - 273;
                    imageUrl = "http://openweathermap.org/img/w/" + iconId + ".png";

                    return  "Current temperature: " + temp + "\u2103 \n\n" + "Weather :      " + description + "\n"  + "Pressure:     "
                            + pressure + " hpa\n" + "Min./Max. Temp :  "  + temp_min + "\u2103/" + temp_max + "\u2103 \nHumidity:   " + humidity  + "%";
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loadBar.setVisibility(View.GONE);
            cityName.setText(city);
            weatherText.setVisibility(View.VISIBLE);
            weatherText.setText(result);


            if(result!=null) {
                weatherIcon.setVisibility(View.VISIBLE);
                cityName.setVisibility(View.VISIBLE);
            }
            Picasso.with(getApplicationContext()).load(imageUrl).into(weatherIcon);
            foreCast.setVisibility(View.VISIBLE);

        }
    }

    private void enableButton() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                foreCast.setEnabled(true);


            }

        });
    }

    private void VisibilityFunc() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                cityName.setVisibility(View.GONE);
                weatherIcon.setVisibility(View.GONE);


            }

        });

    }


}
