package com.example.pt.domain;

import com.example.pt.util.PlotUtils;
import org.junit.Test;

public class CircleTest {

    @Test
    public void test() {
        Point center = new Point(0.0, 0.0);
        Circle circle = new Circle(center, 1);

        PlotUtils.plotCircle(circle);
        PlotUtils.str = "fsd";
    }

}