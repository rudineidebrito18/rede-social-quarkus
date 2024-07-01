package com.github.rudineidebrito18.quarkussocial.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FieldError {
    private String field;
    private String message;
}
