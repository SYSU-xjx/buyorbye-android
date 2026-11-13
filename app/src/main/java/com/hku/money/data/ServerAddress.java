package com.hku.money.data;

import okhttp3.HttpUrl;

/** Accept a server origin, API base, or Swagger URL and return an API base. */
public final class ServerAddress {
    private ServerAddress() {}

    public static String normalize(String value) {
        HttpUrl url = HttpUrl.parse(value.trim());
        if (url == null || !url.username().isEmpty() || !url.password().isEmpty()
                || url.query() != null || url.fragment() != null) {
            throw new IllegalArgumentException("Enter an http:// or https:// server address.");
        }
        String path = url.encodedPath();
        int swagger = path.indexOf("/swagger-ui");
        if (swagger >= 0) path = path.substring(0, swagger);
        while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        if (!path.endsWith("/api")) path += "/api";
        return url.newBuilder().encodedPath(path + "/").build().toString();
    }
}
