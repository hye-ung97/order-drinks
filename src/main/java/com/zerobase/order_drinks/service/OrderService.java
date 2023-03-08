package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.model.MenuEntity;
import com.zerobase.order_drinks.model.StoreData;
import com.zerobase.order_drinks.repository.ListOrderRepository;
import com.zerobase.order_drinks.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.type.StringNVarcharType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final MenuRepository menuRepository;
    private final ListOrderRepository listOrderRepository;


    @Value("${spring.googleMap.key}")
    private String secretKey;

    public Page<MenuEntity> menuList(Pageable pageable) {
        return this.menuRepository.findAll(pageable);
    }

    public StoreData getLocationData(String address){
        address = address.trim();
        Map<String, String> data = getLocationFromApi(address);
        StoreData storeData = new StoreData();
        storeData.setAddress(data.get("address"));
        storeData.setStoreName(data.get("name"));

        return storeData;
    }

    public Map<String, String> getLocationFromApi(String address){

        String locationUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json" +
                "?query=" + address + "&key="+secretKey;

        String getLocationData = getLocationString(locationUrl);
        Map<String, String> parsedLocation = parseLocation(getLocationData);

        String storeUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json" +
                "?location=" + parsedLocation.get("lat") + "%" + parsedLocation.get("lng") +
                "&query=스타벅스" +
                "&radius=1000" +
                "&key=" + secretKey + "&language=ko";

        String paredStoreData = getLocationString(storeUrl);
        Map<String, String> storeData = parseLocation(paredStoreData);

        return storeData;
    }

    public Map<String, String> parseLocation(String jsonString){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e){
            throw new RuntimeException(e);
        }

        Map<String, String> resultMap = new HashMap<>();
        JSONArray results = (JSONArray) jsonObject.get("results");
        JSONObject resultObj = (JSONObject) results.get(0);
        JSONObject geo = (JSONObject) resultObj.get("geometry");
        String formattedAddress = (String) resultObj.get("formatted_address");
        String name = (String) resultObj.get("name");
        JSONObject location = (JSONObject) geo.get("location");

        resultMap.put("lat", String.valueOf(location.get("lat")));
        resultMap.put("lng", String.valueOf(location.get("lng")));
        resultMap.put("address", formattedAddress);
        resultMap.put("name", name);

        return resultMap;
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
            return "failed to get response";
        }
    }
}
