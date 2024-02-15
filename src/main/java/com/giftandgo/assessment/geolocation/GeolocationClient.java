package com.giftandgo.assessment.geolocation;

import com.giftandgo.assessment.model.GeolocationInfo;

public interface GeolocationClient {

    GeolocationInfo getGeolocationForIpAddress(String ipAddress);
}
