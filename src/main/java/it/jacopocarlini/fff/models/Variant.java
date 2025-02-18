package it.jacopocarlini.fff.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Variant {

    private Boolean defaultVariant;
    private String name;
    private Object value;
    private Integer percentage;
    private String target;

}
