package com.zerobase.order_drinks.components;

import com.zerobase.order_drinks.exception.impl.ParseFailException;
import com.zerobase.order_drinks.model.MapData;
import com.zerobase.order_drinks.model.dto.StoreData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.PriorityQueue;

@Component
public class GoogleMapApi {

    @Value("${spring.googleMap.key}")
    private String secretKey;

    public ArrayList<StoreData> getLocationData(String address){
        address = address.trim().replace(" ", "%20");
        ArrayList<MapData> mapData = getLocationFromApi(address);

        ArrayList<StoreData> storeDataArrayList = new ArrayList<>();

        for(MapData data : mapData){
            StoreData storeData = new StoreData();
            storeData.setAddress(data.getAddress());
            storeData.setStoreName(data.storeName);
            storeDataArrayList.add(storeData);
        }

        return storeDataArrayList;
    }

    public ArrayList<MapData> getLocationFromApi(String address){

        String locationUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json" +
                "?query=" + address + "&key="+secretKey + "&language=ko";

        String getLocationData = getLocationString(locationUrl);
        ArrayList<MapData> addressData = parseLocation(getLocationData);


        String storeUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json" +
                "?location=" + addressData.get(0).lat + "%" + addressData.get(0).lng +
                "&query=스타벅스" +
                "&radius=1000" +
                "&key=" + secretKey + "&language=ko";

        String paredStoreData = getLocationString(storeUrl);
        ArrayList<MapData> apiData = parseLocation(paredStoreData);

        return calculate(apiData, addressData);
    }

    public ArrayList<MapData> parseLocation(String jsonString){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e){
            throw new RuntimeException(e);
        }

        JSONArray results = (JSONArray) jsonObject.get("results");

        ArrayList<MapData> list = new ArrayList<>();

        for (Object object : results){
            JSONObject jsonObject1 = (JSONObject) object;
            String formattedAddress = (String) jsonObject1.get("formatted_address");
            String name = (String) jsonObject1.get("name");
            JSONObject geo = (JSONObject) jsonObject1.get("geometry");
            JSONObject location = (JSONObject) geo.get("location");
            String lat = String.valueOf(location.get("lat"));
            String lng = String.valueOf(location.get("lng"));

            MapData mapData = new MapData();
            mapData.setAddress(formattedAddress);
            mapData.setLat(lat);
            mapData.setLng(lng);
            mapData.setStoreName(name);

            list.add(mapData);
        }

        return list;
    }

    public String getLocationString(String apiUrl){

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;

            if(responseCode == 200){
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else{
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null){
                response.append(inputLine);
            }
            br.close();

            return response.toString();
        }catch (Exception e){
            throw new ParseFailException();
        }
    }


    public ArrayList<MapData> calculate (ArrayList<MapData> apiData, ArrayList<MapData> addressData){
        PriorityQueue<MapData> pq = new PriorityQueue<>((x, y) -> (int) (x.distance - y.distance));

        MapData origin_addresses = addressData.get(0);

        for (MapData mapData : apiData){
            String distance = parseDistance(origin_addresses, mapData);
            if(distance == null)    continue;;

            distance = distance.replace(" km","");
            mapData.setDistance(Double.valueOf(distance));
            pq.add(mapData);
        }

        ArrayList<MapData> resultList = new ArrayList<>();
        resultList.add(pq.poll());
        resultList.add(pq.poll());

        return resultList;
    }

    public String parseDistance (MapData origin, MapData destination){

        String address1 = origin.address.trim().replace(" ", "%20");
        String address2 = destination.address.trim().replace(" ", "%20");

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                "?units=metric&mode=transit&destinations=" + address2 +
                "&origins=" + address1 +
                "&key=" + secretKey;

        String parseString = getLocationString(url);

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(parseString);
        } catch (ParseException e){
            throw new RuntimeException(e);
        }

        JSONArray jsonArray = (JSONArray) jsonObject.get("rows");
        JSONObject elementsObj = (JSONObject) jsonArray.get(0);
        JSONArray elementsArr = (JSONArray) elementsObj.get("elements");
        JSONObject distanceArr = (JSONObject) elementsArr.get(0);
        JSONObject distObj = (JSONObject) distanceArr.get("distance");
        String status = (String) distanceArr.get("status");

        if(status.equals("OK")){
            return (String) distObj.get("text");
        }
        return null;
    }
}
