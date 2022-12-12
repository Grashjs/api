package com.grash.dto;

import com.grash.model.File;
import com.grash.model.MeterCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class MeterShowDTO {
    private Long id;

    private String name;


    private String unit;

    private int updateFrequency;

    private MeterCategory meterCategory;

    private File image;

    private List<UserMiniDTO> users = new ArrayList<>();

    private LocationMiniDTO location;

    private AssetMiniDTO asset;
}
