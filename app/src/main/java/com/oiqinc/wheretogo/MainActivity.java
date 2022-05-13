package com.oiqinc.wheretogo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.oiqinc.wheretogo.AutorizateSystem.PreferenceHelper;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DrivingSession.DrivingRouteListener  {
    private MapView mapview;
    private static Context mContext;
    private List<MarkModel> marks = new ArrayList<>();
    private JSONObject obj;
    private ProgressBar progressBar;
    private List<MapObject> list = new ArrayList<MapObject>();
    private ConstraintLayout overlay,addpoint;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private MapObjectCollection mapObjects;
    private LocationManager locationManager;
    private Button buttonSave ;
    private CarouselView carouselView;
    private LocationListener locationListener;
    private Point userPos = new Point();
    private TextView text, author;
    private  JSONArray bitmapsjson;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<String> imagesEncodedList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey("9ae17141-adc0-4ff5-a731-ed50ff9a76c3");
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);

        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);



        carouselView = findViewById(R.id.carouselView);

        text = findViewById(R.id.textView);
        author = findViewById(R.id.textView2);
        overlay = findViewById(R.id.overlay);
        addpoint = findViewById(R.id.addpoint);
        buttonSave = findViewById(R.id.buttonSave);
        mContext = this;
        mapview = findViewById(R.id.mapview);
        progressBar= findViewById(R.id.pb);



        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#1164B4"), android.graphics.PorterDuff.Mode.MULTIPLY);




        mapview.getMap().move(
                new CameraPosition(new Point(52.9209000, 87.9869000), 2.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null);

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapview.getMap().getMapObjects().addCollection();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        locationManager = MapKitFactory.getInstance().createLocationManager();
        locationListener = new LocationListener() {
            @Override
            public void onLocationUpdated(@NonNull Location location) {
                CameraPosition cameraPosition = new CameraPosition(location.getPosition(), 10.0f, 0.0f, 0.0f);
                userPos = location.getPosition();
                Animation animation = new Animation(Animation.Type.SMOOTH, 2);
                mapview.getMap().move(cameraPosition, animation, null);
                locationManager.unsubscribe(locationListener);
            }

            @Override
            public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {

            }
        };

        locationManager.subscribeForLocationUpdates(0, 10, 0, false, FilteringMode.OFF, locationListener);

        new JsonTask().execute("http://0iqinc.ru/WhereToGo/getmarks.php");

        Toast toast = Toast.makeText(getApplicationContext(),
                "Идёт загрузка меток!", Toast.LENGTH_LONG);
        toast.show();

    }





    @Override
    public void onDrivingRoutes(List<DrivingRoute> routes) {
        for (DrivingRoute route : routes) {
            mapObjects.addPolyline(route.getGeometry());
        }
    }

    @Override
    public void onDrivingRoutesError(Error error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
    }



    private void addroute(@NonNull Point ROUTE_START_LOCATION, @NonNull Point ROUTE_END_LOCATION) {
        Log.i("points", ROUTE_END_LOCATION.getLatitude() + " " + ROUTE_END_LOCATION.getLatitude());
        DrivingOptions drivingOptions = new DrivingOptions().setRoutesCount(1);
        VehicleOptions vehicleOptions = new VehicleOptions();
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(
                ROUTE_START_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        requestPoints.add(new RequestPoint(
                ROUTE_END_LOCATION,
                RequestPointType.WAYPOINT,
                null));
        drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapview.onStart();
        MapKitFactory.getInstance().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapview.onStop();
        MapKitFactory.getInstance().onStop();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);

                }
                connection.disconnect();
                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
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


            try {



                obj = new JSONObject(result);


                for(int i = 0; i < obj.length(); i = i + 1){
                    JSONObject object = obj.getJSONObject(i+"");

                    try {

                        double p1 = Double.parseDouble(object.getString("markx"));
                        double p2 = Double.parseDouble(object.getString("marky"));


                        Point coordinates = new Point(p1, p2);
                        String id  =  object.getString("id");
                        String text  =  object.getString("marktext");
                        String markname  =  object.getString("markname");
                        String markowner  =  object.getString("markowner");
                        String bitmapsjson =  object.getString("bitmaps");




                        marks.add(new MarkModel( text, coordinates , markname, markowner, bitmapsjson,id));

                        if(marks.size() == obj.length()){
                            addmarks();
                        } else{
                            Log.i("marksstatus", "not filled");
                        }





                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (Throwable t) {
                Log.e("JSON", "Could not parse malformed JSON: \"" + result + "\"");
            }

        }


    }



    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, String gText) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth()+150,
                drawable.getIntrinsicHeight()+150, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth()-150, canvas.getHeight()-150);
        drawable.draw(canvas);


        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }

        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canva = new Canvas(bitmap);
        TextPaint paint=new TextPaint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.rgb(0, 0, 0));

        paint.setTextSize((int) (14 * scale));


        int textWidth = canva.getWidth() - (int) (16 * scale);


        StaticLayout textLayout = new StaticLayout(
                gText, paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        int textHeight = textLayout.getHeight();


        float x = (bitmap.getWidth() - textWidth)/2;
        float y = (bitmap.getHeight() - textHeight)/2;


        canva.save();
        canva.translate(x, y);
        textLayout.draw(canva);
        canva.restore();




        return bitmap;
    }

    private void addmarks (){

        for(int i = 0; i < marks.size(); i = i + 1){
            MarkModel mark = marks.get(i);




            MapObject  mapObject = mapview.getMap().getMapObjects().addPlacemark(mark.getPoint(),  ImageProvider.fromBitmap(getBitmapFromVectorDrawable(this, R.drawable.ic_pin, mark.getMarkname())));

            list.add(mapObject);


            list.get(list.size()-1).addTapListener(listener);
            list.get(list.size()-1).setUserData(list.size()-1);



        }
        progressBar.setVisibility(View.GONE);


    }


    private final MapObjectTapListener listener = new MapObjectTapListener() {
            @Override
            public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {

                Button marsh = findViewById(R.id.btn2);
                marsh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addroute(userPos,point);
                    }
                });

                Button navigator = findViewById(R.id.btn3);
                navigator.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                         Uri uri = Uri.parse("yandexmaps://maps.yandex.ru/?rtext="+userPos.getLatitude()+","+userPos.getLongitude()+"~"+point.getLatitude()+","+point.getLongitude()+"&rtt=auto");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
             ;

                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Требуются Яндекс карты!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
                MarkModel mark = marks.get( Integer.parseInt(mapObject.getUserData().toString()));

                Button addphoto = findViewById(R.id.UploadBtn);
                addphoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent addphotointent = new Intent(MainActivity.this, LoadImages.class);
                        addphotointent.putExtra("id", mark.getId());
                        startActivity(addphotointent);
                    }
                });


                    text.setText(Html.fromHtml(mark.getText(), Html.FROM_HTML_MODE_COMPACT,
                            MainActivity.this::getDrawable,
                            null));
                    author.setText("Создал метку: " + mark.getMarkowner());


                try {
                   bitmapsjson = new JSONArray(mark.getBitmaps());

                   if(bitmapsjson.length() != 0 ) {

                       carouselView.setVisibility(View.VISIBLE);
                       carouselView.setImageListener(imageListener);
                       carouselView.setPageCount(bitmapsjson.length());


                   } else {

                       carouselView.setVisibility(View.GONE);
                   }




                } catch (JSONException e) {
                    e.printStackTrace();
                }



                overlay.setVisibility(View.VISIBLE);
                return true;
            }
        };




    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(final int position, final ImageView imageView) {

            try {
                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(bitmapsjson.getJSONArray(position).getString(1))
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@Nullable Bitmap resource,@Nullable Transition<? super Bitmap> transition) {
                                imageView.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }

                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void onclear (View view){
        mapObjects.clear();
    }
    public void settings (View view){
        Intent settings = new Intent(MainActivity.this, Settings.class);
        startActivity(settings);
    }
    public void addnew (View view){
         mapview.getMap().addInputListener(inputListener);
        Toast toast = Toast.makeText(getApplicationContext(),
                "Режим добавления меток включён!", Toast.LENGTH_LONG);
        toast.show();
    }




    InputListener inputListener = new InputListener() {
        @Override
        public void onMapTap(@NonNull Map map, @NonNull Point point) {

            addpoint.setVisibility(View.VISIBLE);

            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    EditText name = findViewById(R.id.name);
                    EditText description = findViewById(R.id.description);

                    PreferenceHelper helper = new PreferenceHelper(MainActivity.this);

                    String username = helper.getName();


                    new NewQuery().execute("http://0iqinc.ru/wheretogo/addmark.php?markname="+name.getText().toString()+"&markx="+point.getLatitude() + "&marky=" + point.getLongitude() + "&markowner=" + username + "&marktext="+description.getText().toString() );

                    marks.clear();
                    list.clear();
                    mapObjects.clear();
                    mapview.invalidate();
                    new JsonTask().execute("http://0iqinc.ru/WhereToGo/getmarks.php");

                    mapview.getMap().removeInputListener(inputListener);
                    addpoint.setVisibility(View.GONE);
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Метка скоро появится)", Toast.LENGTH_LONG);
                    toast.show();

                }
            });


        }

        @Override
        public void onMapLongTap(@NonNull Map map, @NonNull Point point) {

        }
    };



    public void onback (View view){
        overlay.setVisibility(View.GONE);
    }

    public void onbackedit (View view){

        addpoint.setVisibility(View.GONE);
        mapview.getMap().removeInputListener(inputListener);



    }

    public Drawable getDrawable(String source) {
        LevelListDrawable d = new LevelListDrawable();
        Drawable empty = ContextCompat.getDrawable(MainActivity.this, R.drawable.empty);
        d.addLevel(0, 0, empty);
        d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
        Log.i("download", source);


        new LoadImage().execute(source, d);

        return d;
    }



    class LoadImage extends AsyncTask<Object, Void, Bitmap> { // Скачивание картинок

        private LevelListDrawable mDrawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            mDrawable = (LevelListDrawable) params[1];
            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }



        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                float scale;
                if(bitmap.getWidth()>1000){
                    scale=0.5f;
                } else{
                    scale=1.4f;
                }


                bitmap=Bitmap.createScaledBitmap(bitmap,
                        (int)(bitmap.getWidth()*scale),
                        (int)(bitmap.getHeight()*scale),
                        true); //bilinear filtering // Масштабируем
                BitmapDrawable d = new BitmapDrawable(getResources(),bitmap);
                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                mDrawable.setLevel(1);
                CharSequence t = text.getText();
                text.setText(t);  // Обновляем текствью с картинками
                text.invalidate();

            }
        }
    }


    public void onGeoClick(View view){
        locationManager.subscribeForLocationUpdates(0, 10, 0, false, FilteringMode.OFF, locationListener);
    }

}