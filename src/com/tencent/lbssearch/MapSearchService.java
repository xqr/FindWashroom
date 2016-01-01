package com.tencent.lbssearch;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import android.text.TextUtils;
import android.util.Log;

import com.example.findwashroom.entity.CustomSearchResultData;
//import com.example.findwashroom.entity.CustomSearchResultData;
import com.tencent.mapsdk.raster.model.LatLng;
import com.yhtye.gongjiao.tools.HttpClientUtils;

public class MapSearchService {
    private static final String key = "VA4BZ-TTIRV-BNJPF-URWQ4-UKI4E-BAFAJ";
    private static final String keywords = "公厕";
    private static String apiUrl = "http://apis.map.qq.com/ws/place/v1/search";
    
    public static List<CustomSearchResultData> list = null;
    
    public static List<CustomSearchResultData> searchNearby(LatLng latlng) {
        try {
            StringBuilder url = new StringBuilder(apiUrl)
                    .append("?boundary=")
                    .append(String.format("nearby(%s,%s,1000)",
                            latlng.getLatitude(), latlng.getLongitude()))
                    .append("&keyword=")
                    .append(URLEncoder.encode(keywords, "UTF-8"))
                    .append("&key=").append(key)
                    .append("&page_size=20&page_index=1&orderby=_distance");
            
            String content = HttpClientUtils.getResponse(url.toString());
            list = parseSearchResultData(content);
            return list;
        } catch (Exception e) {
            Log.e("com.yhtye.shgongjiao.service.BaiduApiService", "getDirectionRoutes()", e);
        }
        return null;
    }
    
    private static List<CustomSearchResultData> parseSearchResultData(String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNodes = mapper.readValue(content, JsonNode.class);
            if (jsonNodes == null 
                    || jsonNodes.get("status").getIntValue() != 0
                    || jsonNodes.get("data") == null) {
                return null;
            }
            
            JsonNode nodes =  mapper.readValue(jsonNodes.get("data"), JsonNode.class);
            List<CustomSearchResultData> dataList = new ArrayList<CustomSearchResultData>();
            for (JsonNode node : nodes) {
                dataList.add(mapper.readValue(node, CustomSearchResultData.class));
            }
            return dataList;
        } catch (Exception e) {
            Log.e("com.yhtye.shgongjiao.service.BaiduApiService", "parseDirectionRoutes()", e);
        }
        
        return null;
    }
}
