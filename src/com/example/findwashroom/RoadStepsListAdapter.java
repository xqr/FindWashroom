package com.example.findwashroom;

import java.util.List;

import com.tencent.lbssearch.object.result.RoutePlanningObject.Step;
import com.yhtye.findwashroom.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RoadStepsListAdapter extends BaseAdapter {
    private Context context; 
    private List<Step> steps = null;
    public List<Step> getSteps() {
        return steps;
    }
    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }
    
    public RoadStepsListAdapter(Context context, List<Step> steps) {
        this.context = context;
        this.steps = steps;
    }
    
    @Override
    public int getCount() {
        if (steps == null) {
            return 0;
        }
        return steps.size();
    }

    @Override
    public Object getItem(int position) {
        if (steps == null) {
            return null;
        }
        if (position < steps.size()) {
            return steps.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView != null) {
            viewHolder = (ViewHolder)convertView.getTag();
        } else { 
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
            convertView = mInflater.inflate(R.layout.act_roadsteps_list, parent, false);
            
            viewHolder = new ViewHolder();
            viewHolder.stepTv = (TextView) convertView.findViewById(R.id.stepTv);
            
            convertView.setTag(viewHolder);
        }
        
        // 设置数据
        Step step = steps.get(position);
        String text = String.format("%s. %s", (position+1), step.instruction);
        viewHolder.stepTv.setText(text);
        
        return convertView;
    }
    
    private static class ViewHolder
    {
        TextView stepTv;
    }
}
