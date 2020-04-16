package com.jdk11.httpclient.linkvalidator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class LinkValidatorSync {

    private static HttpClient client;

    public static void main(String[] args) throws IOException {

        client = HttpClient.newHttpClient();
        String urlsTxtFilePath = "linkvalidator/src/main/resources/urls.txt";

        Files.lines(Path.of(urlsTxtFilePath))
             .map(LinkValidatorSync::validateLink)
             .forEach(System.out::println);
    }

    /**
     * Alternates:
     * HttpResponse.BodyHandlers.ofFile()
     * BodyHandler<Path> ofFile(Path file, OpenOption... openOptions)
     * @param link
     * @return
     */
    private static String validateLink(String link) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                                .GET()
                                .build();

        try {
            HttpResponse<Void> response = client.send(request,
                    HttpResponse.BodyHandlers.discarding());
            return responseToString(response);
        } catch( IOException | InterruptedException e) {
            e.printStackTrace();
            return String.format("%s -> %s", link, false);
        }
    }

    private static String responseToString(HttpResponse<Void> response) {
        int status = response.statusCode();
        boolean success = status >= 200 && status <= 299;
        return String.format("%s -> %s (status: %s)", response.uri(), success, status);
    }
}
