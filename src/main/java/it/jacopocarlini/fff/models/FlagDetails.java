package it.jacopocarlini.fff.models;

import it.jacopocarlini.fff.entity.Target;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
