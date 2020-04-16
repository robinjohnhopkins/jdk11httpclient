## JDK 10 Notes

### System Class Data Sharing

If you JVM instance 1 invoked with
java -Xshare:dump

it can dump into a shared archive resulting in a classes.ja binary file.

To use this in a different JVM instance, map in the file.
Then start up with

java -Xshare:on ...

This can have start up time savings of ~10-30%


### Application Class Data Sharing

Again two VMs.

First vm run with options:

-Xshare:dump
-XX:+UseAppCDS
-XX:SharedClassListFile=..
-XX:SharedArchiveFile=myapp.jsa


Second vm run with options:

-Xshare:on
-XX:+UseAppCDS
-XX:SharedArchiveFile=myapp.jsa

again start up time saving.

To create the class list file

-XX:DumpLoadedClassList=myapp.lst

NB this does not always work!?
in thi case check out https://github.com/simonis/cl4cds


### Improved Container Awareness

Java since jdk10 now knows if it is inside a docker container.
It has new switches
-XX:ActiveProcessorCount
-XX:InitialRAMPercentage
-XX:MaxRAMPercentage
-XX:MinRAMPercentage

It can also attach to a java process.
Implemented for Linux and docker initially.

## HttpClient JDK 11

### LinkValidatorSync

reads linkvalidator/src/main/resources/urls.txt
and determines whether each link is good (2xx range) or not

### LinkValidatorAsync

similar but sendAsync returns CompletableFuture<HttpResponse<T>>

CompletableFutures have get and join blocking methods together with options that have timeouts
They both can throw exceptions and are intended as a last resort.

It is preferable to use methods like 
.thenApply(HttpResponse::body)      - returns CompletableFuture<String> - can be chained
.thenApply(String::length)          - returns CompletableFuture<Integer> like js promises

### LinkValidatorAsyncConfig

.followRedirects(HttpClient.Redirect.NEVER)     default
.followRedirects(HttpClient.Redirect.NORMAL)    does not allow https to http
.followRedirects(HttpClient.Redirect.ALWAYS)    DON'T USE THIS 

.connectTimeout(Duration.ofSeconds(3))          default wait forever NOT WHAT YOU WANT
    don't confuse with request timeout. 


### test helper url

https://httpstat.us/500                         returns 500
https://httpstat.us/200?sleep=5000              waits 2 secs then returns 200

### HTTP_2 default - can fall back to HTTP_1_1 on handshake

```
    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(executorService)
            .version(HttpClient.Version.HTTP_2)
            .build();
```

### POST Form Parameters

https://mkyong.com/java/java-11-httpclient-examples/

com.jdk11.httpclient.post.HttpPost

Java 11 HttpClient didn’t provide API for the form data, we have to construct it manually.

### com.jdk11.httpclient.post.HttpPostJson

similar but sends json rather than form data

```
/Library/Java/JavaVirtualMachines/jdk-14+36/Contents/Home/bin/java -Didea.launcher.port=58725 "-Didea.launcher.bin.path=/Applications/IntelliJ IDEA.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath "/Users/robinjohnhopkins/workspace/jdk11httpclient/httpclientstuff/linkvalidator/target/classes:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar" com.intellij.rt.execution.application.AppMainV2 com.jdk11.httpclient.post.HttpPostJson
status response is 200, body follows
{
  "args": {}, 
  "data": "{\"name\":\"mkyong\",\"notes\":\"hello\"}", 
  "files": {}, 
  "form": {}, 
  "headers": {
    "Content-Length": "33", 
    "Content-Type": "application/json", 
    "Host": "httpbin.org", 
    "User-Agent": "Java 11 HttpClient Bot", 
    "X-Amzn-Trace-Id": "Root=1-5e9832c7-4e93a27295cc31380a2f5fe0"
  }, 
  "json": {
    "name": "mkyong", 
    "notes": "hello"
  }, 
  "origin": "80.195.138.118", 
  "url": "https://httpbin.org/post"
}
```

### CookieManager

CookieManager provides a concrete implementation of CookieHandler, 
which separates the storage of cookies from the policy surrounding 
accepting and rejecting cookies. A CookieManager is initialized with 
a CookieStore which manages storage, and a CookiePolicy object, 
which makes policy decisions on cookie acceptance/rejection.

Often not necessary. Cookies are normally created by servers and set values in headers.


#### LinkValidatorAsyncCookie

run
```
/Library/Java/JavaVirtualMachines/jdk-14+36/Contents/Home/bin/java -Didea.launcher.port=51917 "-Didea.launcher.bin.path=/Applications/IntelliJ IDEA.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath "/Users/robinjohnhopkins/workspace/jdk11httpclient/httpclientstuff/linkvalidator/target/classes:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar" com.intellij.rt.execution.application.AppMainV2 com.jdk11.httpclient.linkvalidatorconfig.LinkValidatorAsyncCookie
cookies before []
cookies after [1P_JAR=2020-04-16-12, NID=202=OpmSEOFiL5bMf16CTNJbNq0doyMAA7t2VtAibij8HR78fzHjm3IblUHxTFC0r7ynKQ05ixmm72WgiNaE9go8y7PGKwaBZgN2E76N4LF9rWgNOGN7oHMzCR88ifR0UOhf6CsRzuABkE_jzTDeZ7e6Zsd8F8dEXyNi9ZCrxXodas0]
cookies after saywhat
```

This sample shows how to assign a cookie manager
IF the same client is used again to a SECOND call to www.google.com
THEN the cookies set by google will be passed AUTOMAGICALLY


### SSLContext

this is the default SSLContext:

```
HttpClient.newBuilder()
            .sslContext(SSLContext.getDefault())
            .build()
```
You CAN supply your own SSLContext instead.


These can be used to change the default transport layer security settings as per.
```
-Djavax.net.ssl.trustStore
-Djavax.net.ssl.keyStore
```

### SSLParameters

Again often not required. If supplied - ONLY these suites and protocols will be used.
```
SSLParameters params = new SSLParameters(
   new String[] { "TLSv1.2" },
   new String[] { "TLS_AES_128_GCM_SHA256" }
);
HttpClient.newBuilder()
           .sslParameters(params)
            .build()
```

### HTTP Basic Authentication

GET /secure

if server responds 401 and includes in hdr:    WWW-Authenticate: Basic

You can make another get but add credentials:

Authorisation: Basic <credentials>
where credentials = base64(user + ":" + password);
NB only use over SSL/TLS


#### com.jdk11.httpclient.auth.AuthenticatorUse

Start a simple Spring Security WebApp provides HTTP basic authentication, 
and test it with the new Java 11 HttpClient APIs.


### Set a Proxy.

```
	private final HttpClient httpClient = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("your-company-proxy.com", 8080)))
            .build();
```

## WebSocket

Full-duplex communication Message-based protocol
Text and binary

```
CompletableFuture<WebSocket> wsFuture = HttpClient.newHttpClient()
    .newWebSocketBuilder() .buildAsync(
        URI.create("ws://server-url"),
        webSocketListener );
        
CompletableFuture<WebSocket> wsFuture = HttpClient.newHttpClient()
     .newWebSocketBuilder()
     .connectTimeout(Duration.ofSeconds(3))
     .buildAsync(URI.create("ws://echo.websocket.org"), new EchoListener(receiveLatch));
```

NB: ws://...    is INSECURE. wss://... is SECURE

ws://echo.websocket.org     is a server on the internet that echos back msgs.
EchoListener is a local class that printlns received msgs.


A web socket listener handles communication from server back to the client.

com.jdk11.httpclient.ws.WebsocketExample

This sample has no error handling - so just gives a flavour :)


## Reactive Streams

Java 9 Flow API
subscriber sends request(n) indicating how many items it will receive at a time
then publisher will call onNext(val) that number of times.

Back pressure is thus eradicated by the subscriber indicating what it can handle.
The idea is that this will be implemented by libraries like spring, rsjava.


## HTTP/2 Server Push

This is here but he just describes it with no concrete example.

