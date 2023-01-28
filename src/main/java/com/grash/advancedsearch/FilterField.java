package com.grash.advancedsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterField {
    private String field;
    private Object value;
    private String operation;
    private List<Object> values = new ArrayList<>();
}