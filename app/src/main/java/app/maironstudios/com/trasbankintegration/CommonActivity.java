package app.maironstudios.com.trasbankintegration;

import android.annotation.SuppressLint;
import android.app.Activity;

import com.ingenico.pclservice.PclService;
import posintegrado.ingenico.com.mposintegrado.mposLib;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by MaironApps.
 */
abstract class CommonActivity extends Activity {
    protected PclService mPclService = null;
    protected boolean mBound = false;
    protected mposLib mposLibobj;
    protected boolean mServiceStarted;
    protected CharSequence mCurrentDevice;

    private PclServiceConnection mServiceConnection;

    private StateReceiver m_StateReceiver = null;

    abstract void onStateChanged(String state);
    abstract void onPclServiceConnected();

    // Implement ServiceConnection
    class PclServiceConnection implements ServiceConnection
    {
        public void onServiceConnected(ComponentName className, IBinder boundService )
        {
            /*We've bound to LocalService, cast the IBinder and get
            LocalService instance*/
            PclService.LocalBinder binder = (PclService.LocalBinder)
                    boundService;
            mPclService = binder.getService();
            onPclServiceConnected();
        }
        public void onServiceDisconnected(ComponentName className)
        {
            mPclService = null;
        }
    };

    // You can call this method in onCreate for instance
    protected void initService()
    {
        Log.i("MAS", "initService");
        if (!mBound)
        {
            mServiceConnection = new PclServiceConnection();
            Intent intent = new Intent(this, PclService.class);
            mBound = bindService(intent, mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }
    protected void releaseService()
    {
        if (mBound)
        {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    private static class StateReceiver extends BroadcastReceiver
    {
        private CommonActivity ViewOwner = null;
        @SuppressLint("UseValueOf")
        public void onReceive(Context context, Intent intent)
        {
            String state = intent.getStringExtra("state");
            ViewOwner.onStateChanged(state);
        }
        StateReceiver(CommonActivity receiver)
        {
            super();
            ViewOwner = receiver;
        }
    }

    private void initStateReceiver()
    {
        Log.i("MAS", "initStateReceiver");
        if(m_StateReceiver == null)
        {
            m_StateReceiver = new StateReceiver(this);
            IntentFilter intentfilter = new IntentFilter("com.ingenico.pclservice.intent.action.STATE_CHANGED" );
            registerReceiver(m_StateReceiver, intentfilter);
        }
    }
    private void releaseStateReceiver()
    {
        if(m_StateReceiver != null)
        {
            unregisterReceiver(m_StateReceiver);
            m_StateReceiver = null;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initStateReceiver();
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        releaseStateReceiver();
    }

    public boolean isCompanionConnected()
    {
        boolean bRet = false;
        if (mPclService != null)
        {
            byte[] result = new byte[1];
            {
                if (mPclService.serverStatus(result))
                {
                    if (result[0] == 0x10)
                    {
                        bRet = true;
                    }
                }
            }
        }
        return bRet;
    }
}
