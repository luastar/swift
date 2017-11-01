package com.luastar.swift.http.route.condition;


import com.luastar.swift.http.server.HttpRequest;

public interface RequestCondition<T> {

    T combine(T other);

    T getMatchingCondition(HttpRequest request);

    int compareTo(T other, HttpRequest request);

}
