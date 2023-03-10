package com.zerobase.order_drinks.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

public class MapDataObject {
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class address{
        private List<addressInfo> results;
        private String status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class addressInfo{
        private String formatted_address;
        private geometryInfo geometry;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class geometryInfo{
        private locationInfo location;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class locationInfo{
        private double lat;
        private double lng;
    }
}
