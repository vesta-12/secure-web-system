package org.example.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TemplateUtils {

    public static String loadHtml(String fileName) {
        try {
            InputStream inputStream = TemplateUtils.class
                    .getClassLoader()
                    .getResourceAsStream("static/" + fileName);

            if (inputStream == null) {
                return "<h1>Template not found: " + fileName + "</h1>";
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            return "<h1>Error loading template</h1>";
        }
    }
}