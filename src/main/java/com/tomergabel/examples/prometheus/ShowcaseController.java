package com.tomergabel.examples.prometheus;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.*;
import org.springframework.web.bind.annotation.*;


@RestController("/showcase")
@ResponseBody
public class ShowcaseController {
    private final Meter meter;
    private final LongHistogram histogram;
    private final LongCounter counter;
    private final LongGauge gauge;

    public ShowcaseController(OpenTelemetry openTelemetry) {
        this.meter = openTelemetry.getMeter("showcase");
        this.histogram = meter
                .histogramBuilder("my_duration")
                .setDescription("Event duration")
                .setUnit("ms")
                .ofLongs()
                .build();
        this.counter = meter
                .counterBuilder("my_count")
                .setDescription("Event count")
                .build();
        this.gauge = meter
                .gaugeBuilder("my_value")
                .setDescription("Some stateful value")
                .ofLongs()
                .build();
    }

    @GetMapping(path="/count/{label}")
    public DemoResponse count(@PathVariable String label) {
        this.counter.add(1, Attributes.of(AttributeKey.stringKey("my_label"), label));
        return DemoResponse.SUCCESS;
    }

    @GetMapping(path="/duration/{lengthMS}")
    public DemoResponse duration(@PathVariable int lengthMS) {
        this.histogram.record(lengthMS);
        return DemoResponse.SUCCESS;
    }

    @PutMapping(path="/gauge")
    public DemoResponse gauge(@RequestBody GaugeData data) {
        this.gauge.set(data.getValue());
        return DemoResponse.SUCCESS;
    }
}
