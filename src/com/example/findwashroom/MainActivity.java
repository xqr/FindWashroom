package com.example.findwashroom;

import java.lang.ref.WeakReference;
import java.util.List;

import com.example.findwashroom.entity.CustomSearchResultData;
import com.tencent.lbssearch.MapSearchService;

import com.tencent.lbssearch.object.Location;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.LatLng;
import com.umeng.update.UmengUpdateAgent;
import com.yhtye.findwashroom.R;
import com.yhtye.gongjiao.tools.NetUtil;
import com.yhtye.gongjiao.tools.ThreadPoolManagerFactory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements TencentLocationListener, OnItemClickListener {
    
    private TencentLocationManager mLocationManager;
    
    private int mLevel = TencentLocationRequest.REQUEST_LEVEL_NAME;
    private TencentLocation myLocation = null;
    
    private WashroomListAdapter listAdapter = null;
    private ListView washroomListView = null;
    private List<CustomSearchResultData> dataList = null; 
    
    private Intent intent=new Intent();
    private Handler handler = null;
    private ProgressDialog progressDialog = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 自动更新检查(wifi环境下触发)
        UmengUpdateAgent.update(this);
        // 全量更新
        UmengUpdateAgent.setDeltaUpdate(false);
        
        initView();
        
        progressDialog.show();
        
        mLocationManager = TencentLocationManager.getInstance(this);
        startLocation(null);
    }

    private void initView() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("正在为您搜索附近公厕……");
        
        washroomListView = (ListView) findViewById(R.id.list_washroom_line);
        handler = new ResultHandler(this);
    }
    
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        // 位置更新时的回调(无论位置是否发生变化都会定期执行)
        if (error == TencentLocation.ERROR_OK) {
            // 定位成功
            myLocation = location;
            // 检查网络
            if (!NetUtil.checkNet(MainActivity.this)) {
                Toast.makeText(MainActivity.this, R.string.network_tip, Toast.LENGTH_LONG).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                return;
            }
            ThreadPoolManagerFactory.getInstance().execute(new SearNearWcRunable(location));
        } else {
            // 定位失败
            String msg = "定位失败: " + reason;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    public void onStatusUpdate(String name, int status, String desc) {
    }
    
    @Override
    protected void onDestroy() {
        // 退出 activity 前一定要停止定位!
        stopLocation(null);
        super.onDestroy();
    }
    
    /**
     * 停止定位
     * 
     * @param view
     */
    public void stopLocation(View view) {
        mLocationManager.removeUpdates(this);
    }
    
    /**
     * 开始定位
     * 
     * @param view
     */
    public void startLocation(View view) {
        // 创建定位请求
        TencentLocationRequest request = TencentLocationRequest.create()
                .setInterval(1000 * 60) // 设置定位周期
                .setRequestLevel(mLevel); // 设置定位level

        // 开始定位
        mLocationManager.requestLocationUpdates(request, this);
    }
    
    private class SearNearWcRunable implements Runnable {
        private TencentLocation myLocation;
        
        public SearNearWcRunable(TencentLocation myLocation) {
            this.myLocation = myLocation;
        }
        
        @Override
        public void run() {
            if (myLocation == null) {
                // 定位失败
                return;
            }
            
            dataList = MapSearchService.searchNearby(new LatLng(myLocation.getLatitude(), 
                    myLocation.getLongitude()));
            Message msg=new Message();
            handler.sendMessage(msg);
        }
    }
    
    private static class ResultHandler extends Handler {
        private WeakReference<MainActivity> mActivity;
        
        public ResultHandler(MainActivity activity) {
            this.mActivity = new WeakReference<MainActivity>(activity); 
        }
        
        @Override  
        public void handleMessage(Message msg) {  
            final MainActivity  theActivity =  mActivity.get();
            theActivity.showList();
            theActivity.stopLocation(null);
        }
    }
    
    /**
     * 展示搜索结果列表
     */
    private void showList() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (dataList == null) {
            return;
        }
        if (listAdapter == null) {
            listAdapter = new WashroomListAdapter(this, dataList);
        }
        washroomListView.setAdapter(listAdapter);
        washroomListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        intent.setClass(MainActivity.this, ResultActivity.class); 
        
        // 用户地理位置：经纬度和地址
        intent.putExtra("start", String.format("%s,%s,%s", myLocation.getLatitude(), 
                myLocation.getLongitude(), myLocation.getAddress()));
        
        // 目标地理位置
        CustomSearchResultData data = dataList.get(position);
        Location destination = data.getLocation();
        intent.putExtra("destination", String.format("%s,%s,%s,%s,%s", destination.lat, 
                destination.lng, data.getTitle(), (int)data.get_distance(), data.getAddress()));
        
        startActivity(intent);
    }
}
