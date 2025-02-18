package it.jacopocarlini.fff.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Variant {

    private Boolean defaultVariant;
    private Object value;
    private Integer percentage;
    private String target;

}
