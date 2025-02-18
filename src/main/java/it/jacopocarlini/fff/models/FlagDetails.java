package it.jacopocarlini.fff.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class FlagDetails {

    private String flagKey;
    private Boolean enabled;

    private List<Variant> variants;

    private LocalDateTime timeWindowStart;
    private LocalDateTime timeWindowEnd;


}
