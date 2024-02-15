package com.giftandgo.assessment.model;

import java.io.Serializable;

public record GeolocationInfo(String ipAddress, String status, String country, String countryCode, String region, String regionName, String city,
                              String zip, String lat, String lon, String timezone, String isp, String org, String as) {
}
