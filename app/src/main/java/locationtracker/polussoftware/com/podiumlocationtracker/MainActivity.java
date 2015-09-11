package locationtracker.polussoftware.com.podiumlocationtracker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;



public class MainActivity extends Activity {

    GpsTracker gps;
    ReusableClass cd;

    Spinner spinnerSchool;
    Spinner spinnerBus;
    Spinner spinnerRoute;
    Spinner spinnerStop;

    List<String> schoolList;
    List<String> portList;
    List<String> busList;
    List<String> vehicleList;
    List<String> placeList;
    List<String> routeIdList;
    List<String> routeNameList;
    List<String> stopIdList;
    List<String> stopNameList;

    ArrayAdapter<String> dataAdapter;
    ArrayAdapter<String> busAdapter;
    ArrayAdapter<String> placeAdapter;
    ArrayAdapter<String> routeNameAdapter;
    ArrayAdapter<String> stopAdapter;
    String selectedSchool;
    String selectedPlace;
    boolean flag = false;
    private ProgressDialog pgLogin;

    String URL="http://www.domainname.com/domainname/domainname.js";
    String UrlBus = "";
    String portalIdParse="";
    String vehicleIdParse="";
    String routeIdParse="";
    String urlRoute="";
    String urlStops="";
    String urlSubmit="";
    String stopId="";
    boolean isGPSEnabled = false;

    long startTime;
    long endTime;
    boolean getSchoolListResult = true;
    boolean getBuslistResult = true;
    boolean getRouteParsingResult = true;
    boolean getStopParsingResult = true;
    boolean getSubmitResult = true;

    int timeoutConnection = 4000;
    int timeoutSocket = 6000;

    Button btn_reload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gps = new GpsTracker(MainActivity.this);
        cd =new ReusableClass(getApplicationContext());

        btn_reload = (Button) findViewById(R.id.btn_reload);
        btn_reload.setVisibility(View.INVISIBLE);

        spinnerSchool = (Spinner) findViewById(R.id.spinnerSchool);
        spinnerBus = (Spinner) findViewById(R.id.spinnerBus);
        spinnerRoute = (Spinner) findViewById(R.id.spinnerRoute);
        spinnerStop= (Spinner) findViewById(R.id.spinnerStop);

        schoolList  = new ArrayList<String>();
        portList    = new ArrayList<String>();
        busList     = new ArrayList<String>();
        vehicleList = new ArrayList<String>();
        placeList   = new ArrayList<String>();
        routeIdList = new ArrayList<String>();
        routeNameList = new ArrayList<String>();
        stopIdList  = new ArrayList<String>();
        stopNameList = new ArrayList<String>();

        dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,schoolList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        busAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,busList);
        busAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        routeNameAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,routeNameList);
        routeNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        stopAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,stopNameList);
        stopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(cd.haveNetworkConnection())
        {
            new MyAsyncTask().execute();
            //--------------------------------------------
            //Progress bar
            //--------------------------------------------

            pgLogin = new ProgressDialog(MainActivity.this);
            pgLogin.setMessage("Just a moment please...");
            pgLogin.setIndeterminate(true);
            pgLogin.setCancelable(false);
            pgLogin.setCanceledOnTouchOutside(true);

            pgLogin.show();
        }
        else
        {
            btn_reload.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this,"Please check your internet !",Toast.LENGTH_LONG).show();
        }


        spinnerSchool.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               // Toast.makeText(MainActivity.this,"Selected School "+ parent.getItemAtPosition(position),Toast.LENGTH_LONG).show();
               // Toast.makeText(MainActivity.this,"Selected portList "+  portList.get(position),Toast.LENGTH_LONG).show();
                selectedSchool = ""+parent.getItemAtPosition(position);
                UrlBus = "http://domainname/domainname/domainname/API/Vehicle/GetBusNo?portalId="+portList.get(position);
                portalIdParse = portList.get(position);
                vehicleList.clear();
                busList.clear();
                busAdapter.clear();
                routeNameAdapter.clear();
                stopAdapter.clear();
                if(cd.haveNetworkConnection()) {
                    new MyAsyncTaskBusList().execute();
                    //--------------------------------------------
                    //Progress bar
                    //--------------------------------------------

                    pgLogin = new ProgressDialog(MainActivity.this);
                    pgLogin.setMessage("Just a moment please...");
                    pgLogin.setIndeterminate(true);
                    pgLogin.setCancelable(false);
                    pgLogin.setCanceledOnTouchOutside(true);

                    pgLogin.show();
                }
                else
                {
                    btn_reload.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this,"Please check your internet connection !",Toast.LENGTH_LONG).show();
                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerBus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               // Toast.makeText(MainActivity.this,"Selected place "+parent.getItemAtPosition(position),Toast.LENGTH_LONG).show();
                urlRoute = "http://domainname/domainname/domainname/API/VehicleRoute/GetRoutes?portalId=" + portalIdParse + "&vehicleId="+vehicleList.get(position);
                Calendar cal= Calendar.getInstance(TimeZone.getDefault());
                startTime = cal.getTimeInMillis();

                vehicleIdParse = vehicleList.get(position);
                routeIdList.clear();
                routeNameList.clear();
                routeNameAdapter.clear();
                stopAdapter.clear();
                if(cd.haveNetworkConnection()) {
                    new MyAsyncTaskRouteList().execute();
                    //--------------------------------------------
                    //Progress bar
                    //--------------------------------------------

                    pgLogin = new ProgressDialog(MainActivity.this);
                    pgLogin.setMessage("Just a moment please...");
                    pgLogin.setIndeterminate(true);
                    pgLogin.setCancelable(false);
                    pgLogin.setCanceledOnTouchOutside(true);

                    pgLogin.show();
                }
                else
                {
                    btn_reload.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this,"Please check your internet connection !",Toast.LENGTH_LONG).show();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                urlStops = "http://domainname/domainname/domainname/API/VehicleStops/GetStops?portalId="+ portalIdParse +"&routeId="+ routeIdList.get(position);
                routeIdParse = routeIdList.get(position);
                stopIdList.clear();
                stopNameList.clear();
                stopAdapter.clear();
                if(cd.haveNetworkConnection()) {
                    new MyAsyncTaskStopList().execute();
                    //--------------------------------------------
                    //Progress bar
                    //--------------------------------------------

                    pgLogin = new ProgressDialog(MainActivity.this);
                    pgLogin.setMessage("Just a moment please...");
                    pgLogin.setIndeterminate(true);
                    pgLogin.setCancelable(false);
                    pgLogin.setCanceledOnTouchOutside(true);

                    pgLogin.show();
                }
                else
                {
                    btn_reload.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this,"Please check your internet connection !",Toast.LENGTH_LONG).show();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                //test
            }
        });

        spinnerStop.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar cal= Calendar.getInstance(TimeZone.getDefault());
                startTime = cal.getTimeInMillis();
                stopId = stopIdList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void getTheLocation(View v)
    {
        Log.e("Tag","Button clicked");

        Calendar  cal =Calendar.getInstance(TimeZone.getDefault());
        endTime = cal.getTimeInMillis();
        long timeDiff = endTime - startTime;
        int  min = (int) ((timeDiff / (1000*60)) % 60);
        int seconds = (int) (timeDiff / 1000) % 60 ;

        String sMin = ""+min;
        String sSec = ""+seconds;

        String sMinute = sMin+"."+sSec;

       // Toast.makeText(MainActivity.this,"Minute "+ min+ "seconds " + seconds + "convert min "+sMinute,Toast.LENGTH_LONG).show();

        LocationManager locManager =(LocationManager) MainActivity.this.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled == false)
        {
           //("TAG","GPS not enabled");
            saveInPreference("latitude", "0.0", MainActivity.this);
            saveInPreference("longitude","0.0",MainActivity.this);
            gps.showSettingsAlert();
        }
        else
        {
            isGPSEnabled = true;
           //Toast.makeText(getApplicationContext(), "Your current location \n Latitude " + getFromPreference("latitude") + "\nLongitude " + getFromPreference("longitude"), Toast.LENGTH_LONG).show();

           if(stopId.isEmpty())
           {
               Toast.makeText(MainActivity.this,"Please select a stop",Toast.LENGTH_LONG).show();
           }
            else {
               if(getFromPreference("latitude").equalsIgnoreCase("0.0") ||getFromPreference("longitude").equalsIgnoreCase("0.0") )
               {
                   Toast.makeText(MainActivity.this,"GPS is not locating , \n Please try again !",Toast.LENGTH_LONG).show();
               }
               else {
                   urlSubmit = "http://domainname/domainname/domainname/API/VehicleStops/UpdateStops?stopId=" + stopId + "&Latitude=" + getFromPreference("latitude") + "&Longitude=" + getFromPreference("longitude") + "&timeInterval=" + sMinute;
                   if(cd.haveNetworkConnection()) {
                       new MyAsyncTaskSubmit().execute();
                       pgLogin = new ProgressDialog(MainActivity.this);
                       pgLogin.setMessage("Just a moment please...");
                       pgLogin.setIndeterminate(true);
                       pgLogin.setCancelable(false);
                       pgLogin.setCanceledOnTouchOutside(true);

                       pgLogin.show();
                   }
                   else
                   {
                       Toast.makeText(MainActivity.this,"Please check your intenet connection !",Toast.LENGTH_LONG).show();
                   }
                   //--------------------------------------------
                   //Progress bar
                   //--------------------------------------------


               }
           }
        }
    }



    public void reload(View v)
    {
        Intent intent= new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }

//----------------------------------------------------------------------
// Start Web parsing for SchoolList
//----------------------------------------------------------------------
    private class MyAsyncTask extends AsyncTask<String, Void, Void> {

        String replay="";

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            //String request = String.format(params[0], "12");
            Log.e("Tag " ,"Pasing Start");
            replay= getResponseByParsing(URL);


            if( replay !=null)
            {
                try {
                    getSchoolListResult = true;
                    JSONObject jsonObj = new JSONObject(replay);
                    JSONArray response = jsonObj.getJSONArray("schoolListArray");
                   // Log.e("tag ", "length "+ response.length());
                    for(int i = 0;i < response.length();i++)
                    {
                        JSONObject c = response.getJSONObject(i);
                        String schoolName = c.getString("schoolName");
                       // Log.e("Tag ","schoolName "+schoolName);
                        String schoolPort = c.getString("schoolPort");
                       // Log.e("Tag ","schoolPort "+ schoolPort);

                        schoolList.add(schoolName);
                        portList.add(schoolPort);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    getSchoolListResult = false;

                }

            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            if (pgLogin.isShowing()) {
                pgLogin.cancel();
                pgLogin.dismiss();
            }
         if (getSchoolListResult == false)
            {
                btn_reload.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this,"Connection Lost, Please check your Internet",Toast.LENGTH_LONG).show();
            }
         else {
             spinnerSchool.setAdapter(dataAdapter);
             // Log.e("tag ", "set adapter");
         }

        }
    }

    public  String getResponseByParsing(String URL)
    {
        String response = null;
        // Build the JSON object to pass parameters
        try
        {


            HttpGet httpGet = new HttpGet(URL);
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            response = EntityUtils.toString(httpResponse.getEntity());
           // Log.e("tag ", "response " + response);

        }
        catch (ConnectTimeoutException e)
        {
            getSchoolListResult = false;
            Log.e("tag", "Time Exception " + e);
        }
        catch(Exception e)
        {
            getSchoolListResult = false;
            Log.e("tag", "Exception " + e);
        }

        return response;
    }

//----------------------------------------------------------------------
// End Web parsing for SchoolList
//----------------------------------------------------------------------

//----------------------------------------------------------------------
// Start Web parsing for Buslist
//----------------------------------------------------------------------
    private class MyAsyncTaskBusList extends  AsyncTask<String, Void, Void>{

        String replayBus="";
        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            //String request = String.format(params[0], "12");
            Log.e("Tag " ,"bus parsing Start");
            replayBus = getResponseByParsingBusList(UrlBus);


            if( replayBus !=null)
            {
                getBuslistResult = true;
                try {
                    JSONObject jsonObj = new JSONObject(replayBus);
                    JSONArray data = jsonObj.getJSONArray("busListArray");

                  //  Log.e("tag ", "length "+ data.length());
                    for(int i = 0;i < data.length();i++)
                    {
                        JSONObject c = data.getJSONObject(i);
                        String VehicleId = c.getString("VehicleId");
                     //   Log.e("Tag ","VehicleId "+VehicleId);
                        String BusNo = c.getString("BusNo");
                      //  Log.e("Tag ","BusNo "+ BusNo);

                         busList.add(BusNo);
                         vehicleList.add(VehicleId);

                    }


                } catch (JSONException e) {
                    getBuslistResult = false;
                    e.printStackTrace();
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            if (pgLogin.isShowing()) {
                pgLogin.cancel();
                pgLogin.dismiss();
            }

            if(getBuslistResult == false)
            {
                btn_reload.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this,"Connection Lost ,Please check your Internet",Toast.LENGTH_LONG).show();
            }
            else {
                Log.e("tag ", "bus adapter");
                spinnerBus.setAdapter(busAdapter);
            }

        }
    }

    public  String getResponseByParsingBusList(String URL)
    {
        String response = null;
        String respond = null;
        // Build the JSON object to pass parameters
        try
        {   HttpGet httpGet = new HttpGet(URL);
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity resEntity = httpResponse.getEntity();
            response = EntityUtils.toString(resEntity);

            response = response.replace("\\","");
            StringBuilder result = new StringBuilder(response);
            result.setCharAt(0,' ');
            result.setCharAt(response.length()-1,' ');
            respond = ""+result;
        }
        catch (ConnectTimeoutException e)
        {
            getBuslistResult = false;
            Log.e("tag", "Time Exception " + e);
        }
        catch(Exception e)
        {
            getBuslistResult = false;
            Log.e("tag", "Exception " + e);
        }

        return respond;
    }

//----------------------------------------------------------------------
// End Web parsing for Buslist
//----------------------------------------------------------------------


 //----------------------------------------------------------------------
// Start Web parsing for RouteParsing
//----------------------------------------------------------------------
    private class MyAsyncTaskRouteList extends  AsyncTask<String, Void, Void>{

        String replayRoute="";
        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            //String request = String.format(params[0], "12");
            Log.e("Tag " ,"route parsing Start");
            replayRoute = getResponseByParsingRouteList(urlRoute);


            if( replayRoute !=null)
            {
                getRouteParsingResult = true;
                try {
                    JSONObject jsonObj = new JSONObject(replayRoute);
                    JSONArray data = jsonObj.getJSONArray("routes");

                  // Log.e("tag ", "length "+ data.length());
                    for(int i = 0;i < data.length();i++)
                    {
                        JSONObject c = data.getJSONObject(i);
                        String RouteId = c.getString("RouteId");
                       // Log.e("Tag ","RouteId "+RouteId);
                        String RouteName = c.getString("RouteName");
                       // Log.e("Tag ","RouteName "+ RouteName);
                        routeIdList.add(RouteId);
                        routeNameList.add(RouteName);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    getRouteParsingResult = false;
                }

            }


            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            if (pgLogin.isShowing()) {
                pgLogin.cancel();
                pgLogin.dismiss();
            }
            if(getRouteParsingResult == false)
            {
                btn_reload.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this,"Connection Lost,Please check your Interent",Toast.LENGTH_LONG).show();
            }
            else {
                Log.e("tag ", "route adapter");
                spinnerRoute.setAdapter(routeNameAdapter);
            }

        }
    }

    public  String getResponseByParsingRouteList(String URL)
    {
        String response = null;
        String respond = null;
        // Build the JSON object to pass parameters
        try
        {
            HttpGet httpGet = new HttpGet(URL);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity resEntity = httpResponse.getEntity();
            response = EntityUtils.toString(resEntity);
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 6000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            response = response.replace("\\","");
            StringBuilder result = new StringBuilder(response);
            result.setCharAt(0,' ');
            result.setCharAt(response.length()-1,' ');
            respond = ""+result;
        }
        catch (ConnectTimeoutException e)
        {
            getRouteParsingResult = false;
            Log.e("tag", "Time Exception " + e);
        }
        catch(Exception e)
        {
            getRouteParsingResult = false;
            Log.e("tag", "Exception " + e);
        }

        return respond;
    }

//----------------------------------------------------------------------
// End Web parsing for RouteParsing
//----------------------------------------------------------------------


 //----------------------------------------------------------------------
// Start Web parsing for StopParsing
//----------------------------------------------------------------------
    private class MyAsyncTaskStopList extends  AsyncTask<String, Void, Void>{

        String replayStop="";
        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            //String request = String.format(params[0], "12");
            Log.e("Tag " ,"route parsing Start");
            replayStop = getResponseByParsingStopList(urlStops);


            if( replayStop !=null)
            {
                getStopParsingResult = true;
                try {
                    JSONObject jsonObj = new JSONObject(replayStop);
                    JSONArray data = jsonObj.getJSONArray("stopsdata");

                   // Log.e("tag ", "length "+ data.length());
                    for(int i = 0;i < data.length();i++)
                    {
                        JSONObject c = data.getJSONObject(i);
                        String StopId = c.getString("StopId");
                       // Log.e("Tag ","StopId "+StopId);
                        String StopName = c.getString("StopName");
                      //  Log.e("Tag ","StopName "+ StopName);

                        stopIdList.add(StopId);
                        stopNameList.add(StopName);
                    }


                } catch (JSONException e) {
                    getStopParsingResult = false;
                    e.printStackTrace();
                }

            }


            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            if (pgLogin.isShowing()) {
                pgLogin.cancel();
                pgLogin.dismiss();
            }
            if (getStopParsingResult == false)
            {
                btn_reload.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this,"Connection loadt, Please check your internet",Toast.LENGTH_LONG).show();
            }
            else {
                Log.e("tag ", "route adapter");
                spinnerStop.setAdapter(stopAdapter);
            }

        }
    }

    public  String getResponseByParsingStopList(String URL)
    {
        String response = null;
        String respond = null;
        // Build the JSON object to pass parameters
        try
        {
            HttpGet httpGet = new HttpGet(URL);
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 6000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity resEntity = httpResponse.getEntity();
            response = EntityUtils.toString(resEntity);
            response = response.replace("\\","");
            StringBuilder result = new StringBuilder(response);
            result.setCharAt(0,' ');
            result.setCharAt(response.length()-1,' ');
            respond = ""+result;
        }
        catch (ConnectTimeoutException e)
        {
            getStopParsingResult = false;
            Log.e("tag", "Time Exception " + e);
        }
        catch(Exception e)
        {
            getStopParsingResult = false;
            Log.e("tag", "Exception " + e);
        }

        return respond;
    }

//----------------------------------------------------------------------
// End Web parsing for StopParsing
//----------------------------------------------------------------------

 //----------------------------------------------------------------------
// Start Web parsing for Submit the result
//----------------------------------------------------------------------
    private class MyAsyncTaskSubmit extends  AsyncTask<String, Void, Void>{

        String replaySubmit="";
        String Status = "";
        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            //String request = String.format(params[0], "12");
            Log.e("Tag " ,"Submit parsing Start");
            replaySubmit = getResponseByParsingsubmit(urlSubmit);


            if( replaySubmit !=null)
            {
                getSubmitResult = true;
                try {
                    JSONObject jsonObj = new JSONObject(replaySubmit);


                     Status = jsonObj.getString("Status");
                    //Log.e("Tag","Status " + Status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            if (pgLogin.isShowing()) {
                pgLogin.cancel();
                pgLogin.dismiss();
            }

            if(getSubmitResult == false)
            {
                Toast.makeText(MainActivity.this,"Connection lost,Please check your Internet",Toast.LENGTH_LONG).show();
            }
            else
            {
                {
                    if (Status.equalsIgnoreCase("updated")) {
                        Toast.makeText(MainActivity.this, "Location address succefully submited", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Location address not submited\n  PLease check again", Toast.LENGTH_LONG).show();
                    }
                    Log.e("tag ", "Submit adapter");
                }
            }

        }
    }

    public  String getResponseByParsingsubmit(String URL)
    {
        String response = null;
        String respond = null;
        // Build the JSON object to pass parameters
        try
        {
            HttpGet httpGet = new HttpGet(URL);
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity resEntity = httpResponse.getEntity();
            response = EntityUtils.toString(resEntity);
            response = response.replace("\\","");
            StringBuilder result = new StringBuilder(response);
            result.setCharAt(0,' ');
            result.setCharAt(response.length()-1,' ');
            respond = ""+result;
        }
        catch (ConnectTimeoutException e)
        {
            getSubmitResult = false;
            Log.e("tag", "Time Exception " + e);
        }
        catch(Exception e)
        {
            getSubmitResult = false;
            Log.e("tag", "Exception " + e);
        }

        return respond;
    }

//----------------------------------------------------------------------
// End Web parsing for Submit the result
//----------------------------------------------------------------------


    public String getFromPreference(String variable_name) {
        String preference_return;
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        preference_return = preferences.getString(variable_name, "");

        return preference_return;
    }

    public static void saveInPreference(String name, String content, Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, content);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        gps.stopUsingGPS();
        finish();
    }
}
