package com.hku.money.data;

import android.content.Context;
import com.google.gson.JsonObject;
import java.util.UUID;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import okhttp3.*;

public final class LiveApi {
    private static final java.util.concurrent.ExecutorService WORK = Executors.newSingleThreadExecutor();
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .callTimeout(30, java.util.concurrent.TimeUnit.SECONDS).build();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private final android.content.SharedPreferences preferences;
    private final Context context;
    public LiveApi(Context context) {
        preferences = context.getSharedPreferences("api_session", Context.MODE_PRIVATE);
        this.context = context.getApplicationContext();
    }
    public String baseUrl() {
        return preferences.getString("base_url", com.hku.money.BuildConfig.API_BASE_URL);
    }

    public void saveBaseUrl(String value) {
        String normalized = ServerAddress.normalize(value);
        if (!normalized.equals(baseUrl())) {
            preferences.edit().putString("base_url", normalized).remove("token").remove("preview").apply();
        }
    }

    public void testConnection(String value, Callback callback) {
        final String normalized;
        try {
            normalized = ServerAddress.normalize(value);
        } catch (IllegalArgumentException error) {
            callback.done(null, error.getMessage());
            return;
        }
        String root = normalized.substring(0, normalized.length() - "api/".length());
        WORK.execute(() -> {
            com.google.gson.JsonElement data = null;
            String error = null;
            OkHttpClient probe = CLIENT.newBuilder().callTimeout(8, java.util.concurrent.TimeUnit.SECONDS).build();
            try (Response response = probe.newCall(new Request.Builder().url(root + "v3/api-docs").build()).execute()) {
                if (!response.isSuccessful()) {
                    error = "API docs returned HTTP " + response.code();
                } else {
                    JsonObject spec = new com.google.gson.Gson().fromJson(response.body().string(), JsonObject.class);
                    JsonObject paths = spec.getAsJsonObject("paths");
                    boolean auth = paths != null && paths.has("/api/auth/login") && paths.has("/api/auth/register");
                    boolean publish = paths != null && paths.has("/api/purchase-proposals")
                            && paths.has("/api/purchase-proposals/{id}/publish");
                    if (!auth || !publish) error = "Server reached. Login or publish endpoints are missing.";
                    else {
                        JsonObject summary = new JsonObject();
                        summary.addProperty("paths", paths.size());
                        data = summary;
                    }
                }
            } catch (java.io.IOException failure) {
                error = "Cannot reach this address. Check the server and Wi-Fi.";
            } catch (RuntimeException failure) {
                error = "Server replied, but its API document is invalid.";
            }
            com.google.gson.JsonElement result = data;
            String message = error;
            MAIN.post(() -> callback.done(result, message));
        });
    }

    public boolean signedIn() { return preferences.contains("token") || preview(); }
    public boolean preview() { return preferences.getBoolean("preview", false); }
    public void startPreview() { clear(); preferences.edit().putBoolean("preview", true).apply(); }
    public void clear() { preferences.edit().remove("token").remove("preview").apply(); }
    public void saveToken(String token) { preferences.edit().putString("token", token).putBoolean("preview", false).apply(); }
    public String deviceId() {
        String id = preferences.getString("device", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            preferences.edit().putString("device", id).apply();
        }
        return id;
    }
    public interface Callback { void done(com.google.gson.JsonElement data, String error); }
    public void request(String method, String path, JsonObject payload, Callback callback) {
        String token = preferences.getString("token", "");
        String server = baseUrl();
        WORK.execute(() -> {
            com.google.gson.JsonElement data = null;
            String error = null;
            try {
                Request.Builder request = new Request.Builder()
                        .url(server + path);
                if (!token.isEmpty()) request.header("Authorization", "Bearer " + token);
                RequestBody body = payload == null ? null : RequestBody.create(
                        MediaType.parse("application/json"), payload.toString());
                if (!method.equals("GET") && body == null) {
                    body = RequestBody.create(MediaType.parse("application/json"), "{}");
                }
                request.method(method, body);
                try (Response response = CLIENT.newCall(request.build()).execute()) {
                    JsonObject result = new com.google.gson.Gson().fromJson(response.body().string(), JsonObject.class);
                    if (response.code() == 401 && !path.startsWith("auth/")) {
                        clear();
                        MAIN.post(() -> context.startActivity(new android.content.Intent(context,
                                com.hku.money.LoginActivity.class).addFlags(
                                android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)));
                    }
                    if (!response.isSuccessful()) {
                        error = result.has("message") ? result.get("message").getAsString() : "Request failed (" + response.code() + ")";
                    } else {
                        data = result.get("data");
                    }
                }
            } catch (Exception failure) {
                error = "Cannot reach the server. Check your connection and try again.";
            }
            com.google.gson.JsonElement value = data;
            String message = error;
            MAIN.post(() -> callback.done(value, message));
        });
    }
}
