package com.interview.tracking_app.service;

import com.interview.tracking_app.exception.TrackingNumberGenerationException;
import com.interview.tracking_app.model.NextTrackingNumberRequest;
import com.interview.tracking_app.model.TrackingNumber;
import com.interview.tracking_app.repository.TrackingNumberRepository;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackingNumberServiceImpl implements TrackingNumberService {

    private final TrackingNumberRepository repository;

    private static final int MAX_LENGTH = 16;
    private static final int MAX_ATTEMPTS = 10;

    @Override
    public Mono<TrackingNumber> generateTrackingNumber(NextTrackingNumberRequest request)
    {
        return Flux.range(1, MAX_ATTEMPTS)
                .concatMap(attempt -> tryGenerateTrackingNumber(request)
                        .doOnError(DuplicateKeyException.class, e ->
                                log.warn("Duplicate tracking number on attempt {} for customer '{}'", attempt, request.getCustomerId())
                        )
                        .onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
                )
                .next() // emit the first successful result
                .switchIfEmpty(
                        // Fallback Logic : try a random unique tracking number
                        tryGenerateRandomTrackingNumber(request)
                                .doOnError(DuplicateKeyException.class, e ->
                                        log.error("Fallback random tracking number also caused duplicate for customer '{}'", request.getCustomerId())
                                )
                                .onErrorResume(DuplicateKeyException.class, e -> Mono.error(
                                        new TrackingNumberGenerationException(
                                                "Failed to generate unique tracking number after fallback attempt", request
                                        )
                                ))
                );
    }

    private Mono<TrackingNumber> tryGenerateTrackingNumber(NextTrackingNumberRequest request) {
        String trackingNumber = createTrackingNumber(request);
        TrackingNumber record = TrackingNumber.builder()
                .id(UUID.randomUUID().toString())
                .trackingNumber(trackingNumber)
                .createdAt(Instant.now())
                .build();
        return repository.save(record);
    }

    private String createTrackingNumber(NextTrackingNumberRequest request) {
        String base = request.getOriginCountryId() +
                request.getDestinationCountryId() +
                String.format("%03d", (int) (request.getWeight() * 100)) +
                request.getCustomerSlug().replace("-", "")
                        .substring(0, Math.min(4, request.getCustomerSlug().length())) +
                Integer.toHexString(ThreadLocalRandom.current().nextInt(0, 65536)).toUpperCase();

        return base.substring(0, Math.min(base.length(), MAX_LENGTH)).toUpperCase();
    }

    private Mono<TrackingNumber> tryGenerateRandomTrackingNumber(NextTrackingNumberRequest request) {
        String trackingNumber = generateRandomAlphanumeric(MAX_LENGTH);
        TrackingNumber record = TrackingNumber.builder()
                .id(UUID.randomUUID().toString())
                .trackingNumber(trackingNumber)
                .createdAt(Instant.now())
                .build();
        return repository.save(record);
    }

    private String generateRandomAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
