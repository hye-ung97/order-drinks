package com.zerobase.order_drinks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.order_drinks.exception.impl.ParseFailException;
import com.zerobase.order_drinks.model.dto.MapDataObject;
import com.zerobase.order_drinks.model.dto.StoreData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleMapService {
    private final RestTemplate restTemplate;

    @Value("${spring.googleMap.key}")
    private String secretKey;

    private String baseUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json";

    public List<StoreData> findStoreFromApi(String address) {
        String currentLocationString = currentLocation(address);
        MapDataObject.address addressData = null;

        addressData = apiParse(currentLocationString);
        MapDataObject.addressInfo addressInfo = addressData.getResults().get(0);
        return storeLocation(addressInfo);

    }

    public String currentLocation(String address){
        address = address.replace(" ", "+").trim();

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("query", address)
                .queryParam("key", secretKey)
                .queryParam("language", "ko").encode().build();

        return apitoString(builder);
    }

    public List<StoreData> storeLocation(MapDataObject.addressInfo current) {
        double lat = current.getGeometry().getLocation().getLat();
        double lng = current.getGeometry().getLocation().getLng();

        UriComponents builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("location", lat + "%" + lng)
                .queryParam("query", "????????????")
                .queryParam("radius", "1000")
                .queryParam("key", secretKey)
                .queryParam("language", "ko").build();

        String jsonString = apitoString(builder);
        MapDataObject.address addressData = null;

        addressData = apiParse(jsonString);

        List<MapDataObject.addressInfo> addressList = addressData.getResults();

        PriorityQueue<StoreData> pq = new PriorityQueue<>(new Comparator<StoreData>() {
            @Override
            public int compare(StoreData o1, StoreData o2) {
                double a = Double.valueOf(o1.getDistance().replace(" km",""));
                double b = Double.valueOf(o2.getDistance().replace(" km",""));

                if(a <= b){
                    return -1;
                }
                else{
                    return 1;
                }
            }
        });

        for(MapDataObject.addressInfo data : addressList){
            double storeLat = data.getGeometry().getLocation().getLat();
            double storeLng = data.getGeometry().getLocation().getLng();
            double dist = distance(lat, lng, storeLat, storeLng);

            StoreData storeData = new StoreData();
            storeData.setStoreName(data.getName());
            storeData.setAddress(data.getFormatted_address());
            storeData.setDistance(String.format("%.3f km", dist));
            pq.add(storeData);
        }

        List<StoreData> result = new ArrayList<>();
        result.add(pq.poll());
        result.add(pq.poll());

        return result;
    }

    public String apitoString(UriComponents builder) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> response = restTemplate.exchange(builder.toUri(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        return response.getBody();
    }

    public MapDataObject.address apiParse(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonString, MapDataObject.address.class);
        } catch (JsonProcessingException e) {
            throw new ParseFailException();
        }
    }

    // ???????????? ?????? ??????
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1609.344;

        return dist / 1000;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
