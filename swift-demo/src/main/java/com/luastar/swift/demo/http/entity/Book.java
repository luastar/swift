package com.luastar.swift.demo.http.entity;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by zhuminghua on 2016/12/22.
 */
public class Book {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    @Min(10)
    private Integer size;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", size=" + size +
                '}';

    }
}
