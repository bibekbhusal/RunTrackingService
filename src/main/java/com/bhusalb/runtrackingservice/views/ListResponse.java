package com.bhusalb.runtrackingservice.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListResponse<T> {
    private List<T> items;
}
