package com.hezb.hplayer.clingupnp.dms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import com.hezb.clingupnp.dms.MediaServer;
import com.hezb.hplayer.R;
import com.hezb.hplayer.base.BaseActivity;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.ValidationException;

import java.net.UnknownHostException;

/**
 * DMS页
 * Created by hezb on 2016/1/29.
 */
public class DMSActivity extends BaseActivity {

    private TextView mAddress;

    private MediaServer mMediaServer;

    private boolean hadBindService = false;
    private AndroidUpnpService mUpnpService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mUpnpService = (AndroidUpnpService) service;
            try {
                mMediaServer = new MediaServer(mContext);
                mUpnpService.getRegistry().addDevice(mMediaServer.getDevice());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAddress.setText(mMediaServer.getAddress());
                    }
                });
            } catch (ValidationException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
        }
    };

    @Override
    protected int getContentViewId() {
        return R.layout.activity_dms;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddress = (TextView) findViewById(R.id.address);

        hadBindService = bindService(new Intent(mContext, com.hezb.clingupnp.dms.UpnpService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hadBindService) {
            unbindService(mServiceConnection);
        }
        if (mMediaServer != null) {
            mMediaServer.release();
        }
    }
}
