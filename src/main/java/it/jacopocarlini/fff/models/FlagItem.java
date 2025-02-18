package it.jacopocarlini.fff.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Data
@Builder
@AllArgsConstructor
public class FlagItem {
    private String flagKey;
    private Boolean enabled;
    private Integer variants;
    private LocalDateTime timeWindowStart;
    private LocalDateTime timeWindowEnd;

    private Boolean target;
    private Boolean rollout;


}
