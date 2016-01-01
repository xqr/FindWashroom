package com.example.findwashroom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.tencentmap.mapsdk.map.TencentMap.InfoWindowAdapter;
import com.yhtye.findwashroom.R;

public class WashroomInfoWindowAdapter implements InfoWindowAdapter{

    private Context context; 
    
    public WashroomInfoWindowAdapter(Context context) {
        this.context = context;
    }
    
    @Override
    public View getInfoWindow(Marker arg0) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        View convertView = mInflater.inflate(R.layout.act_marker_window, null, false);
        
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.titleTv = (TextView) convertView.findViewById(R.id.windows_title);
        viewHolder.addressTv = (TextView) convertView.findViewById(R.id.windows_address);
        
        viewHolder.titleTv.setText(String.valueOf(arg0.getTag()));
        viewHolder.addressTv.setText(arg0.getTitle());
        
        return convertView;
    }

    @Override
    public void onInfoWindowDettached(Marker arg0, View arg1) {
        // TODO 
    }
    
    private static class ViewHolder
    {
        TextView titleTv;
        TextView addressTv;
    }
}
