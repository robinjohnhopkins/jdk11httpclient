package com.jdk11.httpclient.linkvalidatorconfig;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LinkValidatorAsyncCookie {

    private static HttpClient client;
    private static CookieManager cm;

    // This sample shows how to assign a cookie manager
    // IF the same client is used again to a SECOND call to www.google.com
    // THEN the cookies set by google will be passed AUTOMAGICALLY
    public static void main(String[] args) throws Exception {
        cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

        client = HttpClient.newBuilder()
                    .cookieHandler(cm)
                    .connectTimeout(Duration.ofSeconds(3))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

        LinkValidatorAsyncCookie.showCookies("cookies before ");

        String link = "https://www.google.com";
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                                .timeout(Duration.ofSeconds(4))
                                .GET()
                                .build();

        // below is a hacky check that thenApply can be chained. obvs. println use is rubbish debug
        CompletableFuture<String> response =
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                     .thenApply(LinkValidatorAsyncCookie::responseToString)
                     .thenApply(s -> {return LinkValidatorAsyncCookie.showCookies("cookies after ");})
                     .exceptionally(e -> {String s = String.format("%s -> %s", link, false);System.out.println(s); return s;});

        var ret = response.get();  // without this, the program exits before printing cookies after...
        System.out.println(ret);
    }

    private static String responseToString(HttpResponse<String> response) {
        int status = response.statusCode();
        boolean success = status >= 200 && status <= 299;
        return String.format("%s -> %s (status: %s)", response.uri(), success, status);
    }
    private static String showCookies(String msg){
        List<HttpCookie> listCookiesBefore =  cm.getCookieStore().getCookies();
        System.out.println(msg + listCookiesBefore.toString());
        msg += "saywhat";
        return msg;
    }

}
