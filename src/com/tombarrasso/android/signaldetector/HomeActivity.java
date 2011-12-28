package com.tombarrasso.android.signaldetector;

// Android Packages
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Button;
import android.telephony.SignalStrength;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.CellLocation;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;
import android.util.Log;

public final class HomeActivity extends Activity
{
	public static final String TAG = HomeActivity.class.getSimpleName();
	
	public static final String EMAIL = "tbarrasso@sevenplusandroid.org";
	
	private CellLocation mCellLocation;
	private SignalStrength mSignalStrength;
	private boolean mDone = false;
	private TextView mText = null;
	private String mTextStr;
	private Button mSubmit, mCancel;
	private TelephonyManager mManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
    	mText = (TextView) findViewById(R.id.text);
    	mSubmit = (Button) findViewById(R.id.submit);
    	mCancel = (Button) findViewById(R.id.cancel);
    	
    	// Prevent button press.
    	mSubmit.setEnabled(false);
    	
    	// Handle click events.
    	mSubmit.setOnClickListener(new OnClickListener()
    	{
    		@Override
    		public void onClick(View mView)
    		{
    			sendResults();
    			finish();
    		}
    	});
    	mCancel.setOnClickListener(new OnClickListener()
    	{
    		@Override
    		public void onClick(View mView)
    		{
    			finish();
    		}
    	});
    	
    	// Register the listener with the telephony manager
    	mManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
    		PhoneStateListener.LISTEN_CELL_LOCATION);
    }
    
    // Listener for signal strength.
    final PhoneStateListener mListener = new PhoneStateListener()
    {
    	@Override
    	public void onCellLocationChanged(CellLocation mLocation)
    	{
    		if (mDone) return;
    		
    		Log.d(TAG, "Cell location obtained.");
    	
    		mCellLocation = mLocation;
    		
    		update();
    	}
    	
    	@Override
    	public void onSignalStrengthsChanged(SignalStrength sStrength)
    	{
    		if (mDone) return;
    		
    		Log.d(TAG, "Signal strength obtained.");
    		
    		mSignalStrength = sStrength;
    		
    		update();
    	}
    };
    
    // AsyncTask to avoid an ANR.
    private class ReflectionTask extends AsyncTask<Void, Void, Void>
    {
		protected Void doInBackground(Void... mVoid)
		{
			mTextStr = 
    			("DEVICE INFO\n\n" + "SDK: `" + Build.VERSION.SDK_INT + "`\nCODENAME: `" +
    			Build.VERSION.CODENAME + "`\nRELEASE: `" + Build.VERSION.RELEASE +
    			"`\nDevice: `" + Build.DEVICE + "`\nHARDWARE: `" + Build.HARDWARE +
    			"`\nMANUFACTURER: `" + Build.MANUFACTURER + "`\nMODEL: `" + Build.MODEL +
    			"`\nPRODUCT: `" + Build.PRODUCT + ((getRadio() == null) ? "" : ("`\nRADIO: `" + getRadio())) +
    			"`\nBRAND: `" + Build.BRAND + ((Build.VERSION.SDK_INT >= 8) ? ("`\nBOOTLOADER: `" + Build.BOOTLOADER) : "") +
    			"`\nBOARD: `" + Build.BOARD + "`\nID: `"+ Build.ID + "`\n\n" +
    			ReflectionUtils.dumpClass(SignalStrength.class, mSignalStrength) + "\n" +
    			ReflectionUtils.dumpClass(mCellLocation.getClass(), mCellLocation) + "\n" + getWimaxDump() +
    			ReflectionUtils.dumpClass(TelephonyManager.class, mManager) +
    			ReflectionUtils.dumpStaticFields(Context.class, getApplicationContext()));
    			
    		return null;
		}
		
		protected void onProgressUpdate(Void... progress)
		{
			// Do nothing...
		}
		
		protected void onPostExecute(Void result)
		{
			complete();
		}
	}

    private final void complete()
    {
    	mDone = true;
    	
    	try
    	{
    		mText.setText(mTextStr);
    	
			// Stop listening.
			mManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
			Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_SHORT).show();
			
			mSubmit.setEnabled(true);
		}
		catch (Exception e)
		{
			Log.e(TAG, "ERROR!!!", e);
		}
    }
    
    private final void update()
    {
    	if (mSignalStrength == null || mCellLocation == null) return;
    	
    	final ReflectionTask mTask = new ReflectionTask();
    	mTask.execute();
    }
    
    /**
     * @return The Radio of the {@link Build} if available.
     */
    public static final String getRadio()
    {
    	if (Build.VERSION.SDK_INT >= 8 && Build.VERSION.SDK_INT < 14)
    		return Build.RADIO;
    	else if (Build.VERSION.SDK_INT >= 14)
    		return Build.getRadioVersion();
    	else
    		return null;
    }
    
    private static final String[] mServices =
	{
		"WiMax", "wimax", "wimax", "WIMAX", "WiMAX"
	};
    
    /**
     * @return A String containing a dump of any/ all WiMax
     * classes/ services loaded via {@link Context}.
     */
    public final String getWimaxDump()
    {
    	String mStr = "";
    	
    	for (final String mService : mServices)
    	{
    		final Object mServiceObj = getApplicationContext()
    			.getSystemService(mService);
    		if (mServiceObj != null)
    		{
    			mStr += "getSystemService(" + mService + ")\n\n";
    			mStr += ReflectionUtils.dumpClass(mServiceObj.getClass(), mServiceObj);
			}
    	}
    	
    	return mStr;
    }
    
    /**
     * Start an {@link Intent} chooser for the user to submit the results.
     */
    public final void sendResults()
    {
    	final Intent mIntent = new Intent(Intent.ACTION_SEND);
		mIntent.setType("plain/text");
		mIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { EMAIL });
		mIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.results));
		mIntent.putExtra(Intent.EXTRA_TEXT, mTextStr);
		HomeActivity.this.startActivity(Intent.createChooser(mIntent, "Send results."));
    }
}
