package com.interview.tracking_app.repository;

import com.interview.tracking_app.model.TrackingNumber;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.Optional;

public interface TrackingNumberRepository extends ReactiveMongoRepository<TrackingNumber, String> {
    Optional<TrackingNumber> findByTrackingNumber(String trackingNumber);
}
