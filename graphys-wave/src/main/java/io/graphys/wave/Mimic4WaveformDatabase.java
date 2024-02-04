package io.graphys.wave;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Mimic4WaveformDatabase implements WaveformDatabase {
    private static final Logger logger = LogManager.getLogger();

    public List<String> getAllRecordNames() {
        var recordListFileName = "RECORDS";
        try {
            var mimicUrl = new URL("https://physionet.org/files/mimic4wdb/0.1.0/" + recordListFileName);
            var in = new Scanner(mimicUrl.openStream());
            return in.tokens().toList();
        }
        catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
            return null;
        }
    }

    protected List<String> listFileInDirectory() {
        try {
            var uri = new URI("https://physionet.org/static/published-projects/mimic4wdb/0.1.0/waves/p100/p10014354/RECORDS");
            var file = new File(uri);

            System.out.println("this is file: " + file.isFile());

            return new ArrayList<>();

            /*if (!file.isDirectory()) {
                throw new RuntimeException("Uri should point to directory.");
            }
            return Arrays.stream(file.list()).toList();*/
        }
        catch (URISyntaxException e) {
            logger.error(e);
            e.printStackTrace();
            return null;
        }
    }

    protected String getContentType(String path) {
        var template = new RestTemplate();

        var headers = template.headForHeaders(path);
        return headers.getContentType().toString();
    }
}
