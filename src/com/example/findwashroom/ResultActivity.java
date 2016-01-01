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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends MapActivity implements OnMarkerDraggedListener, 
        OnMarkerClickListener {

    private MapView mapView;
    private TencentMap tencentMap;
    private MapPoint[] locations;
    private WashroomInfoWindowAdapter adapter;
    private Polyline polyLine;
    
    private List<WalkingResultObject.Route> walkRoutes;
    private List<CustomSearchResultData> dataList = null; 
    
    private TextView myaddress;
    private TextView mytitile;
    private TextView mydistance;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
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
        
        if (locations == null) {
            locations = getCoords();
        }
        // 设置起点和终点
        MapPoint start = locations[0];
        MapPoint destination = locations[1];
        showMyMap(start, destination);
        getWalkPlan(start.changeToLocation(), destination.changeToLocation());
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
        
        tencentMap.addMarker(new MarkerOptions()
                .position(start.getLatlng())
                .title(start.getAddress()) // 点击时会显示
                .tag(start)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.navi1))
                .draggable(true)
                .anchor(0.5f, 0.5f));
        
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
            }
        }
    };
    
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
    
    protected MapPoint[] getCoords() {
        Intent intent = getIntent();
        String startStr = intent.getStringExtra("start");
        String destinationStr = intent.getStringExtra("destination");
        MapPoint start = null;
        if (!TextUtils.isEmpty(startStr)) {
            String[] items = startStr.split(",", 3);
            if (items.length == 3) {
                start = new MapPoint(Float.valueOf(items[0]),Float.valueOf(items[1]), items[2]);
                start.setName("我的位置");
            }
        }
        MapPoint destination = null;
        if (!TextUtils.isEmpty(destinationStr)) {
            String[] items = destinationStr.split(",", 5);
            if (items.length == 5) {
                destination = new MapPoint(Float.valueOf(items[0]),Float.valueOf(items[1]), items[4]);
                destination.setName(items[2]);
                destination.setDistance(Integer.parseInt(items[3]));
            }
        }
        MapPoint[] locations = {start, destination};
        return locations;
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
        ResultActivity.this.finish();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // TODO Auto-generated method stub 
        Object obj = marker.getTag();
        if (obj == null) {
            return false;
        }
        if (obj instanceof CustomSearchResultData) {
            CustomSearchResultData data = (CustomSearchResultData) obj;
            mytitile.setText(data.getTitle());
            myaddress.setText(data.getAddress());
            mydistance.setText((int)data.get_distance() + "米");
            
            getWalkPlan(locations[0].changeToLocation(), data.getLocation());
        } else if (obj instanceof MapPoint) {
            // 我的位置
            MapPoint data = (MapPoint)obj;
            mytitile.setText(data.getName());
            myaddress.setText(data.getAddress());
        } 
        return false;
    }
}
