package com.example.pt.util;

import com.vaadin.flow.server.StreamResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VaadinUtils {

    public static StreamResource streamResource(BufferedImage image) {
        return new StreamResource("img",
                () -> {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(image, "png", bos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new ByteArrayInputStream(bos.toByteArray());
                });
    }
}
