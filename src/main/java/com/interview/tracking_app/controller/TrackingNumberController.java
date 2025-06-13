package com.interview.tracking_app.controller;

import com.interview.tracking_app.model.NextTrackingNumberRequest;
import com.interview.tracking_app.model.TrackingNumber;
import com.interview.tracking_app.service.TrackingNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/next-tracking-number")
@Validated
public class TrackingNumberController {

    private final TrackingNumberService trackingNumberService;

    public TrackingNumberController(TrackingNumberService trackingNumberService) {
        this.trackingNumberService = trackingNumberService;
    }

    @GetMapping
    @Operation(
            summary = "Generate tracking number",
            description = "Creates a unique tracking number based on shipment details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tracking number generated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public Mono<TrackingNumber> getTrackingNumber(@Valid NextTrackingNumberRequest request) {
        return trackingNumberService.generateTrackingNumber(request);
    }
}
