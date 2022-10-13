package com.ibo.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY="90e9b7264ddd58fd0c274873d273bf96";

    EditText etCityName;
    Button btnSearch;
    ImageView iconWeather;
    TextView tvTemp,tvCity;
    ListView lvDailyWeather;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        etCityName=findViewById(R.id.etCityName);
        btnSearch=findViewById(R.id.btnSearch);
        iconWeather=findViewById(R.id.iconWeather);
        tvTemp=findViewById(R.id.tvTemp);
        tvCity=findViewById(R.id.tvCity);
        lvDailyWeather=findViewById(R.id.lvDailyWeather);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city=etCityName.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this,"Please enter a city name",Toast.LENGTH_SHORT).show();
                }else {
                    //TODO :load weather by city name
                    loadWeatherByCityName(city);
                }

            }
        });
    }


    private void loadWeatherByCityName(String city) {
        Ion.with(this)
                .load("https://api.openweathermap.org/data/2.5/weather?q="+city+"&units=metric&&appid="+API_KEY)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if(e!=null){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        }else {

//                            {"coord": { "lon": 139,"lat": 35},
//                                "weather": [
//                                {
//                                    "id": 800,
//                                        "main": "Clear",
//                                        "description": "clear sky",
//                                        "icon": "01n"
//                                }
//                                          ],
//                                "base": "stations",
//                                    "main": {
//                                "temp": 281.52,
//                                        "feels_like": 278.99,
//                                        "temp_min": 280.15,
//                                        "temp_max": 283.71,
//                                        "pressure": 1016,
//                                        "humidity": 93
//                            },
//                                "wind": {
//                                "speed": 0.47,
//                                        "deg": 107.538
//                            },
//                                "clouds": {
//                                "all": 2
//                            },
//                                "dt": 1560350192,
//                                    "sys": {
//                                "type": 3,
//                                        "id": 2019346,
//                                        "message": 0.0065,
//                                        "country": "JP",
//                                        "sunrise": 1560281377,
//                                        "sunset": 1560333478
//                            },
//                                "timezone": 32400,
//                                    "id": 1851632,
//                                    "name": "Shuzenji",
//                                    "cod": 200
//                            }
                            JsonObject main=result.get("main").getAsJsonObject();
                            double temp=main.get("temp").getAsDouble();
                            tvTemp.setText(temp+"°C");

                            JsonObject sys=result.get("sys").getAsJsonObject();
                            String country=sys.get("country").getAsString();
                            tvCity.setText(city+","+country);

                            JsonArray weather=result.get("weather").getAsJsonArray();
                            String icon=weather.get(0).getAsJsonObject().get("icon").getAsString();
                            loadingIcon(icon);

                            JsonObject coord=result.get("coord").getAsJsonObject();
                            double lat=coord.get("lat").getAsDouble();
                            double lon=coord.get("lon").getAsDouble();
                            laodDailyForecast(lat,lon);


                        }
                    }
                });
    }

    private void laodDailyForecast(double lat, double lon) {
        Ion.with(this)
                .load("https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&exclude=current,minutely,hourly,alerts&units=metric&appid="+API_KEY)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (e != null) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        } else {
                            List<Weather>weatherList=new ArrayList<>();
                            String timezone=result.get("timezone").getAsString();
                            JsonArray daily=result.get("daily").getAsJsonArray();
                            for(int i=1;i<daily.size();i++){
                                Long date=daily.get(i).getAsJsonObject().get("dt").getAsLong();
                                Double temp=daily.get(i).getAsJsonObject().get("temp").getAsJsonObject().get("day").getAsDouble();
                                String icon=daily.get(i).getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("icon").getAsString();
                                weatherList.add(new Weather(date,"timezone",temp,icon));

                            }

                            DailyWeatherAdapter dailyWeatherAdapter=new DailyWeatherAdapter(MainActivity.this,weatherList);
                            lvDailyWeather.setAdapter(dailyWeatherAdapter);
                        }
                    }
                });
    }


    private void loadingIcon(String icon) {
        Ion.with(this)
                .load("https://openweathermap.org/img/w/"+icon+".png").intoImageView(iconWeather);
    }
}