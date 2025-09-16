package com.noncore.assessment.service.file;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class DocumentTextExtractor {
    private final Tika tika = new Tika();

    public String extractText(InputStream inputStream, String fileName, String mimeType) throws Exception {
        try (inputStream) {
            // 简易策略：优先使用 AutoDetectParser，失败时回退到 tika.parseToString
            try {
                AutoDetectParser parser = new AutoDetectParser();
                BodyContentHandler handler = new BodyContentHandler(-1);
                Metadata metadata = new Metadata();
                if (mimeType != null) metadata.set("Content-Type", mimeType);
                if (fileName != null) metadata.set("resourceName", fileName);
                parser.parse(inputStream, handler, metadata);
                return handler.toString();
            } catch (Exception e) {
                // fallback
                return tika.parseToString(inputStream);
            }
        }
    }
}


