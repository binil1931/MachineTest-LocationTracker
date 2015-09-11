package locationtracker.polussoftware.com.podiumlocationtracker;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by binil on 9/1/2015.
 */


public class GpsTracker extends Service implements LocationListener {

    private static Context mContext;
    boolean isGPSEnabled = false;
    boolean canGetLocation = false;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    protected LocationManager locationManager;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES =0; // 1 minute

    public GpsTracker(Context context)
    {

        this.mContext = context;
        getLocation();
    }

    public Location getLocation()
    {
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGPSEnabled)
        {
            this.canGetLocation = true;
            if (location == null)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_TIME_BW_UPDATES, this);
                Log.d("GPS Enabled", "GPS Enabled");

            }

        }
        else
        {
            saveInPreference("latitude","0.0",mContext);
            saveInPreference("longitude","0.0",mContext);
        }

        return location;
    }
    @Override
    public void onLocationChanged(Location location) {
        Log.e("Tag", "Location changed");
       latitude = location.getLatitude();
       longitude = location.getLongitude();

       // String lat = String.valueOf(latitude);
       // String lon = String.valueOf(longitude);

        saveInPreference("latitude",""+latitude,mContext);
        saveInPreference("longitude",""+longitude,mContext);
      //  saveInPreference("longitude","longitude",GpsTracker.this);


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public double getLatitude()
    {

       // latitude = location.getLatitude();
      //  Log.e("Tag" , "latitude "+ latitude);
        return latitude;
    }

    public double getLongitude()
    {
       // longitude = location.getLongitude();
       // Log.e("Tag" , "longitude "+ longitude);
        return longitude;
    }

    public static void saveInPreference(String name, String content, Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, content);
        editor.commit();
    }
    public void stopUsingGPS()
    {
          Log.e("tag","stop gps");
            locationManager.removeUpdates(GpsTracker.this);

    }

}
