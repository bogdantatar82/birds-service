package com.hunus.birdsservice.utils;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> sightings;
    private int page;
    private int size;
    private Long total;
}
