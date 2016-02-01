package com.hezb.hplayer.clingupnp.dmp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.hezb.clingupnp.DlnaDeviceInfo;
import com.hezb.clingupnp.DlnaSearch;
import com.hezb.clingupnp.UpnpServiceType;
import com.hezb.clingupnp.dmp.Constants;
import com.hezb.clingupnp.dmp.ContentBrowseActionCallback;
import com.hezb.clingupnp.dmp.ContentItem;
import com.hezb.hplayer.R;
import com.hezb.hplayer.base.BaseActivity;
import com.hezb.hplayer.constant.ConstantKey;
import com.hezb.hplayer.ui.activity.PlayerActivity;
import com.hezb.hplayer.util.Log;
import com.hezb.hplayer.util.Utility;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.model.container.Container;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * DLNA设备页
 * Created by hezb on 2016/1/27.
 */
public class DevicesActivity extends BaseActivity implements ContentBrowseActionCallback.OnReceiveListener {

    private DlnaSearch mDlnaSearch;

    private Spinner mDevicesSpinner;
    private ListView mContentList;

    private ArrayAdapter<String> mDevicesSpinnerAdapter;
    private List<String> deviceNameList;
    private List<Device> deviceList;

    private Service currentService;
    private List<String> containerIdList;// 文件夹对应ID列表

    @Override
    protected int getContentViewId() {
        return R.layout.activity_devices;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initAllMember();

        mDlnaSearch = new DlnaSearch(this);
        mDlnaSearch.startSearchDMP();

    }

    private void initAllMember() {
        mDevicesSpinner = (Spinner) findViewById(R.id.devices_spinner);
        mContentList = (ListView) findViewById(R.id.content_list);

        deviceNameList = new ArrayList<>();
        mDevicesSpinnerAdapter = new ArrayAdapter<>(mContext,
                R.layout.devices_spinner_item, deviceNameList);
        mDevicesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDevicesSpinner.setAdapter(mDevicesSpinnerAdapter);
        mDevicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Device device = deviceList.get(position);
                currentService = device.findService(
                        new UDAServiceType(UpnpServiceType.CONTENT_DIRECTORY));
                if (currentService != null) {
                    mDlnaSearch.getUpnpService().getControlPoint().execute(
                            new ContentBrowseActionCallback(currentService,
                                    createRootContainer(currentService),
                                    DevicesActivity.this));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        deviceList = new ArrayList<>();

        mContentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContentItem contentItem = (ContentItem) parent.getItemAtPosition(position);
                if (contentItem.isContainer()) {
                    Service service = contentItem.getService();
                    mDlnaSearch.getUpnpService().getControlPoint().execute(
                            new ContentBrowseActionCallback(service, contentItem.getContainer(),
                                    DevicesActivity.this));
                    containerIdList.add(contentItem.getId());
                } else {
                    String format = contentItem.getFormat();
                    if (format != null) {
                        if (format.contains(Constants.FORMAT_VIDEO)
                                || format.contains(Constants.FORMAT_AUDIO)) {
                            Intent intent = new Intent(mContext, PlayerActivity.class);
                            intent.putExtra(ConstantKey.PLAY_URL, contentItem.getUrl());
                            intent.putExtra(ConstantKey.NAME, contentItem.getTitle());
                            startActivity(intent);
                        }
                    }
                }
            }
        });
        mContentList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ContentItem contentItem = (ContentItem) parent.getItemAtPosition(position);
                if (!contentItem.isContainer()) {
                    Utility.showToast(mContext, contentItem.getUrl());
                }
                return true;
            }
        });

        EventBus.getDefault().register(this);
    }

    private Container createRootContainer(Service service) {
        Container rootContainer = new Container();
        rootContainer.setId("0");
        rootContainer.setTitle("Content Directory on "
                + service.getDevice().getDisplayString());
        containerIdList = new ArrayList<>();
        containerIdList.add("0");
        return rootContainer;
    }

    public void onEventMainThread(DlnaDeviceInfo dlnaDeviceInfo) {
        Log.d("onEventMainThread" + dlnaDeviceInfo.isAdd());
        if (dlnaDeviceInfo.isAdd()) {
            deviceList.add(dlnaDeviceInfo.getDevice());
            deviceNameList.add(dlnaDeviceInfo.getDevice().getDetails().getFriendlyName());
        } else {
            String removeIdentifier = dlnaDeviceInfo.getDevice().getIdentity()
                    .getUdn().getIdentifierString();
            String identifier = null;
            for (int i = 0; i < deviceList.size(); i++) {
                identifier = deviceList.get(i).getIdentity().getUdn().getIdentifierString();
                if (identifier.equals(removeIdentifier)) {
                    deviceList.remove(i);
                    deviceNameList.remove(i);
                    break;
                }
            }
        }
        if (mDevicesSpinnerAdapter != null) {
            mDevicesSpinnerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentService != null && containerIdList != null && containerIdList.size() != 1) {
            containerIdList.remove(containerIdList.size() - 1);// 移除当前id
            String tagId = containerIdList.get(containerIdList.size() - 1);
            mDlnaSearch.getUpnpService().getControlPoint().execute(
                    new ContentBrowseActionCallback(currentService, tagId, this));
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDlnaSearch.resumeSearch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDlnaSearch.pauseSearch();
    }

    @Override
    protected void onDestroy() {
        mDlnaSearch.unBindService();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void received(final List<ContentItem> contentItemList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ContentListAdapter adapter = new ContentListAdapter(mContext, contentItemList);
                mContentList.setAdapter(adapter);
            }
        });
    }

    @Override
    public void failure(String msg) {
        Utility.showToast(mContext, "获取远程目录失败！");
    }
}
