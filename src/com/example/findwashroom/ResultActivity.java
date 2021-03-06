package com.example.findwashroom;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import com.example.findwashroom.entity.CustomSearchResultData;
import com.example.findwashroom.entity.MapPoint;
import com.tencent.lbssearch.MapSearchService;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.Location;
import com.tencent.lbssearch.object.param.WalkingParam;
import com.tencent.lbssearch.object.result.WalkingResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.mapsdk.raster.model.Polyline;
import com.tencent.mapsdk.raster.model.PolylineOptions;
import com.tencent.tencentmap.mapsdk.map.MapActivity;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMarkerClickListener;
import com.tencent.tencentmap.mapsdk.map.TencentMap.OnMarkerDraggedListener;
import com.umeng.analytics.MobclickAgent;
import com.yhtye.findwashroom.R;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends MapActivity implements OnMarkerDraggedListener, 
        OnMarkerClickListener, TencentLocationListener {

    private MapView mapView;
    private TencentMap tencentMap;
    private WashroomInfoWindowAdapter adapter;
    // 路线
    private Polyline polyLine;
    
    private MapPoint startPoint;
    private MapPoint destinationPoint;
    private Marker startMarker;
    
    private TencentLocationManager mLocationManager;
    private int mLevel = TencentLocationRequest.REQUEST_LEVEL_NAME;
    
    private List<WalkingResultObject.Route> walkRoutes;
    private List<CustomSearchResultData> dataList = null; 
    
    private TextView myaddress;
    private TextView mytitile;
    private TextView mydistance;
    
    private ListView roadStepsListView = null;
    private RoadStepsListAdapter listAdapter = null;
    
    private SensorManager mSensorManager;
    private Sensor accelerometer; // 加速度传感器
    private Sensor magnetic; // 地磁场传感器
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
     // 实例化传感器管理者
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 初始化加速度传感器
        accelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        init();
    }
    
    private void init() {
        dataList = MapSearchService.list;
        
        mapView = (MapView)findViewById(R.id.map);
        tencentMap = mapView.getMap();
        tencentMap.setOnMarkerClickListener(this);
        
        myaddress = (TextView)findViewById(R.id.myaddress);
        mytitile = (TextView)findViewById(R.id.mytitile);
        mydistance = (TextView)findViewById(R.id.mydistance1);
        roadStepsListView = (ListView) findViewById(R.id.list_road_steps);
        
        mLocationManager = TencentLocationManager.getInstance(this);
        
        // 解析起点和终点
        getCoords();
        
        showMyMap(startPoint, destinationPoint);
        getWalkPlan(startPoint.changeToLocation(), destinationPoint.changeToLocation());
    }
    
    protected void showMyMap(MapPoint start, MapPoint destination) {
        mytitile.setText(destination.getName());
        myaddress.setText(destination.getAddress());
        mydistance.setText(destination.getDistance() + "米");
        
        if (adapter == null) {
            adapter = new WashroomInfoWindowAdapter(this);
        }
        tencentMap.setInfoWindowAdapter(adapter);
        tencentMap.setCenter(start.getLatlng()); // 先设置Center会减少卡顿
        
        addUserPoint(start);
        
        tencentMap.zoomToSpan(start.getLatlng(), destination.getLatlng());
        if (destination.getDistance() > 700) {
            tencentMap.setZoom(16);
        } else if (destination.getDistance() < 300) {
            tencentMap.setZoom(18);
        } else {
            tencentMap.setZoom(17); //值越大，地图越被放大
        }
        
        addWcMarker(dataList);
    }
    
    /**
     * 步行规划，只能设置起点和终点
     */
    protected void getWalkPlan(Location start, Location destination) {
        TencentSearch tencentSearch = new TencentSearch(this);
        WalkingParam walkingParam = new WalkingParam();
        walkingParam.from(start);
        walkingParam.to(destination);
        tencentSearch.getDirection(walkingParam, directionResponseListener);
    }
    
    HttpResponseListener directionResponseListener = new HttpResponseListener() {
        @Override
        public void onFailure(int arg0, Header[] arg1, String arg2,
                Throwable arg3) {
            Toast.makeText(ResultActivity.this, "路线规划失败，请检查手机网络", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSuccess(int arg0, Header[] arg1, BaseObject arg2) {
            if (arg2 == null) {
                return;
            }
            WalkingResultObject walkObj = (WalkingResultObject)arg2;
            if (walkObj.isStatusOk()) {
                walkRoutes = walkObj.result.routes;
                if (walkRoutes == null || walkRoutes.size() == 0) {
                    return;
                }
                int index = 0;
                drawSolidLine(walkRoutes.get(index).polyline);
                // 显示路线
                if (listAdapter == null) {
                    listAdapter = new RoadStepsListAdapter(ResultActivity.this,
                            walkRoutes.get(index).steps);
                    roadStepsListView.setAdapter(listAdapter);
                } else {
                    listAdapter.setSteps(walkRoutes.get(index).steps);
                    listAdapter.notifyDataSetChanged();
                }
            }
            startLocation(null);
        }
    };
    
    private void addUserPoint(MapPoint start) {
        if (startMarker != null) {
            startMarker.remove();
        }
        startMarker = tencentMap.addMarker(new MarkerOptions()
                .position(start.getLatlng())
                .title(start.getAddress())
                // 点击时会显示
                .tag(start)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.navi1))
                .draggable(true).anchor(0.5f, 0.5f));
        // 旋转坐标
        calculateOrientation();
    }
    
    private void addWcMarker(List<CustomSearchResultData> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        for (CustomSearchResultData data  : list) {
            tencentMap.addMarker(new MarkerOptions()
            .position(new LatLng(data.getLocation().lat, data.getLocation().lng))
            .title(data.getAddress()) // 点击时会显示
            .tag(data)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.wc_90))
            .draggable(true)
            .anchor(0.5f, 0.5f));
        }
    }
    
    /**
     * 将路线以实线画到地图上
     * @param locations
     */
    protected void drawSolidLine(List<Location> locations) {
        if (polyLine != null) {
            polyLine.remove();
        }
        polyLine = tencentMap.addPolyline(new PolylineOptions()
                                            .addAll(getLatLngs(locations))
                                            .color(0xff2200ff));
    }
    
    protected List<LatLng> getLatLngs(List<Location> locations) {
        List<LatLng> latLngs = new ArrayList<LatLng>();
        for (Location location : locations) {
            latLngs.add(new LatLng(location.lat, location.lng));
        }
        return latLngs;
    }
    
    protected void getCoords() {
        Intent intent = getIntent();
        String startStr = intent.getStringExtra("start");
        String destinationStr = intent.getStringExtra("destination");
        if (!TextUtils.isEmpty(startStr)) {
            String[] items = startStr.split(",", 3);
            if (items.length == 3) {
                startPoint = new MapPoint(Float.valueOf(items[0]),Float.valueOf(items[1]), items[2]);
                startPoint.setName("我的位置");
            }
        }
        if (!TextUtils.isEmpty(destinationStr)) {
            String[] items = destinationStr.split(",", 5);
            if (items.length == 5) {
                destinationPoint = new MapPoint(Float.valueOf(items[0]),Float.valueOf(items[1]), items[4]);
                destinationPoint.setName(items[2]);
                destinationPoint.setDistance(Integer.parseInt(items[3]));
            }
        }
    }

    @Override
    public void onMarkerDrag(Marker arg0) {
    }

    @Override
    public void onMarkerDragEnd(Marker arg0) {
        arg0.setSnippet(arg0.getPosition().toString());
        arg0.showInfoWindow();
    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
    }
    
    /**
     * 关闭当前页面
     * 
     * @param v
     */
    public void backPrePageClick(View v) {
        tencentMap = null;
        mapView = null;
        stopLocation(null);
        ResultActivity.this.finish();
    }
    
    @Override
    public void onResume() {
        startLocation(null);
        
        // 注册监听
        mSensorManager.registerListener(new MySensorEventListener(),
                accelerometer, Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(new MySensorEventListener(), magnetic,
                Sensor.TYPE_MAGNETIC_FIELD);
        
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        stopLocation(null);
        // 解除注册
        mSensorManager.unregisterListener(new MySensorEventListener());
        
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object obj = marker.getTag();
        if (obj == null) {
            return false;
        }
        if (obj instanceof CustomSearchResultData) {
            CustomSearchResultData data = (CustomSearchResultData) obj;
            mytitile.setText(data.getTitle());
            myaddress.setText(data.getAddress());
            mydistance.setText((int)data.get_distance() + "米");
            
            getWalkPlan(startPoint.changeToLocation(), data.getLocation());
        } else if (obj instanceof MapPoint) {
            // 我的位置
            MapPoint data = (MapPoint)obj;
            mytitile.setText(data.getName());
            myaddress.setText(data.getAddress());
        } 
        return false;
    }

    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
     // 位置更新时的回调(无论位置是否发生变化都会定期执行)
        if (error == TencentLocation.ERROR_OK) {
            // 定位成功
            startPoint = new MapPoint(location.getLatitude(), location.getLongitude(), location.getAddress());
            startPoint.setName("我的位置");
            addUserPoint(startPoint);
        } else {
        }
    }

    @Override
    public void onStatusUpdate(String arg0, int arg1, String arg2) {
        
    }
    
    @Override
    protected void onDestroy() {
        // 退出 activity 前一定要停止定位!
        stopLocation(null);
        // 解除注册
        mSensorManager.unregisterListener(new MySensorEventListener());
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
                .setInterval(1000 * 12) // 设置定位周期
                .setRequestLevel(mLevel); // 设置定位level

        // 开始定位
        mLocationManager.requestLocationUpdates(request, this);
    }
    
    @Override
    protected void onStop() {
        // 解除注册
        mSensorManager.unregisterListener(new MySensorEventListener());
        super.onStop();
    }
    
    class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO 太频繁
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // 加速度传感器
                accelerometerValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // 磁场传感器
                magneticFieldValues = event.values;
            }
            calculateOrientation();
        }
 
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            
        }
    }
    private float lastOrientation = 0;
    /**
     *  计算方向
     */
    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues,
                magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        
        if (Math.abs(lastOrientation - values[0]) < 20) {
            return;
        }
        
        Animation rotateAnimation=new RotateAnimation(lastOrientation, values[0], 
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.8f);
        rotateAnimation.setDuration(100);//设置动画持续时间
        rotateAnimation.setFillAfter(true);//设置动画结束后保持当前的位置（即不返回到动画开始前的位置）
        startMarker.getMarkerView().startAnimation(rotateAnimation);
        
        lastOrientation = values[0];
    }
}
