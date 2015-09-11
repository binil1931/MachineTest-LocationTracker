package locationtracker.polussoftware.com.podiumlocationtracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
public class ReusableClass {

	private static Context _context;
	
	public ReusableClass(Context context){
        this._context = context;
    }
	
	
	 public static boolean haveNetworkConnection(){
	        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
	          if (connectivity != null) 
	          {
	              NetworkInfo[] info = connectivity.getAllNetworkInfo();
	              if (info != null) 
	                  for (int i = 0; i < info.length; i++) 
	                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
	                      {
	                          return true;
	                      }
	 
	          }
	          return false;
	    }

}
