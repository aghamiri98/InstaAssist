package com.app.instaassist.base;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class HttpRequestSpider {



    public static final String USER_AGENT = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.1.2) "
            + " Gecko/20090803 Fedora/3.5.2-2.fc11 Firefox/3.5.2";

    public static final String ANDORID_USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Mobile Safari/537.36";

    public static final String UA_1 = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
    public static int CONNECTION_TIMEOUT = 10_000;
    public static final String METHOD_GET = "GET";

    public static final String METHOD_POST = "POST";

    public enum RequestMethod {
        GET,
        POST
    }

    private static final HttpRequestSpider sInstance = new HttpRequestSpider();


    public static HttpRequestSpider getInstance() {
        return sInstance;
    }

    public String requestWithCookie(String htmlUrl, String cookie) {
        return requestWithCookie(htmlUrl, RequestMethod.GET, cookie);
    }

    public String requestWithCookie(String htmlUrl, RequestMethod method, String cookie) {
        URL url = null;
        String htmlContext = "";
        try {
            url = new URL(htmlUrl);
            HttpURLConnection conn;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent",
                        USER_AGENT);
                conn.setRequestProperty("Cookie", cookie);
                if (method == RequestMethod.GET) {
                    conn.setRequestMethod(METHOD_GET);
                } else {
                    conn.setRequestMethod(METHOD_POST);
                }


                Scanner scanner = new Scanner(conn.getInputStream());
                while (scanner.hasNextLine()) {
                    htmlContext += scanner.nextLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return htmlContext;
    }

    public String request(String htmlUrl) {
        return request(htmlUrl, RequestMethod.GET);
    }

    public String request(String htmlUrl, RequestMethod method) {
        URL url = null;
        String htmlContext = "";
        try {
            url = new URL(htmlUrl);
            HttpURLConnection conn;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent",
                        USER_AGENT);
                if (method == RequestMethod.GET) {
                    conn.setRequestMethod(METHOD_GET);
                } else {
                    conn.setRequestMethod(METHOD_POST);
                }

                Scanner scanner = new Scanner(conn.getInputStream());
                while (scanner.hasNextLine()) {
                    htmlContext += scanner.nextLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return htmlContext;
    }
}
