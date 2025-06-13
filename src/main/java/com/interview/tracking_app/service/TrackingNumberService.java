package com.interview.tracking_app.service;

import com.interview.tracking_app.model.NextTrackingNumberRequest;
import com.interview.tracking_app.model.TrackingNumber;
import reactor.core.publisher.Mono;

public interface TrackingNumberService {
    public Mono<TrackingNumber> generateTrackingNumber(NextTrackingNumberRequest request);
}