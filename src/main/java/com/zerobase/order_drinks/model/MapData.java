package com.zerobase.order_drinks.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MapData {
    public String address;
    public String storeName;
    public String lat;
    public String lng;
    public double distance;
}
