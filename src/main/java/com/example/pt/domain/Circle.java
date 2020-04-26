package com.example.pt.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.beans.ConstructorProperties;
import java.util.LinkedList;
import java.util.List;

/**
 * Окружность
 */
public class Circle {
    private static double COUNT = 10000;
    private static double STEP = (2 * Math.PI) / COUNT;
    @Getter
    private Point center;
    @Getter
    @JsonProperty(value = "radius")
    private Double r;
    @JsonIgnore
    @Getter
    private List<Point> values;

    @ConstructorProperties({"center", "radius"})
    public Circle(Point center, double radius) {
        if (center == null) {
            throw new IllegalArgumentException(); //todo нужно сообщение
        }
        if (Double.isNaN(center.getX()) || Double.isNaN(center.getY())) {
            throw new IllegalArgumentException(); // todo нужно сообщение
        }
        if (radius <= 0) {
            throw new IllegalArgumentException(); //todo нужно сообщение
        }
        this.center = center;
        this.r = radius;
        values = new LinkedList<>();
        generateValues();
    }

    private void generateValues() {
        for (double val = -Math.PI; val < Math.PI; val += STEP) {
            values.add(new Point(r * Math.cos(val) + center.getX(), r * Math.sin(val) + center.getY()));
        }
    }
}
