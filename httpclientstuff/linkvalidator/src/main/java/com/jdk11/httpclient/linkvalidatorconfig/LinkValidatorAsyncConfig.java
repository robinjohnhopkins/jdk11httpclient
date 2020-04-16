package com.jdk11.httpclient.linkvalidatorconfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LinkValidatorAsyncConfig {

    private static HttpClient client;

    public static void main(String[] args) throws Exception {
        client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        String urlsTxtFilePath = "linkvalidator/src/main/resources/urls.txt";
        var requests =
                Files.lines(Path.of(urlsTxtFilePath))
                     .map(LinkValidatorAsyncConfig::validateLink)
                     .collect(Collectors.toList());

        requests.stream()
                .map(CompletableFuture::join)
                .forEach(System.out::println);
    }

    private static CompletableFuture<String> validateLink(String link) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                                .timeout(Duration.ofSeconds(2))
                                .GET()
                                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                     .thenApply(LinkValidatorAsyncConfig::responseToString)
                     .exceptionally(e -> String.format("%s -> %s", link, false));

    }

    private static String responseToString(HttpResponse<Void> response) {
        int status = response.statusCode();
        boolean success = status >= 200 && status <= 299;
        return String.format("%s -> %s (status: %s)", response.uri(), success, status);
    }

}