package com.example.pt.util;

import com.example.pt.domain.Circle;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class StateResolverUtils {
    private static final String XML_STATE = "src/main/resources/circle-state-lab2pt.xml";
    private static XmlMapper mapper = new XmlMapper() {{
        enable(SerializationFeature.INDENT_OUTPUT);
    }};

    public static void serializeToFileState(Circle circle) {
        try {
            File resultFile = new File(XML_STATE);
            if (!resultFile.exists()) {
                if (!resultFile.createNewFile()) {
                    throw new IOException("File not created");
                }
            }
            mapper.writeValue(resultFile, circle);
        } catch (IOException e) {
            log.error("Unable to read file " + XML_STATE);
        }
    }

    public static Circle deserializeFromFile() {
        try {
            return mapper.readValue(new File(XML_STATE), Circle.class);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    public static void clearState() throws IOException {
        boolean delete = new File(XML_STATE).delete();
        if (!delete) {
            throw new IOException(); // чёто пошло не так
        }
    }
}
