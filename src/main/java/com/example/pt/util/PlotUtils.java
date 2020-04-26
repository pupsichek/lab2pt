package com.example.pt.util;

import com.example.pt.domain.Circle;
import com.example.pt.domain.Point;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;

import java.awt.image.BufferedImage;

public class PlotUtils {

    public static BufferedImage plotCircle(Circle circle) {
        double[] xData = circle.getValues().stream().mapToDouble(Point::getX).toArray();
        double[] yData = circle.getValues().stream().mapToDouble(Point::getY).toArray();
        XYChart chart = QuickChart.getChart("", "X", "Y", "circle", xData, yData);
        return BitmapEncoder.getBufferedImage(chart);
    }
}
