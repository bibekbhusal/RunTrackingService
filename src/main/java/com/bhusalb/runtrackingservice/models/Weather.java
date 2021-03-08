package com.bhusalb.runtrackingservice.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class Weather implements Serializable {
    private Double temperature;
    private Double precipitation;
    private Double humidity;
}
