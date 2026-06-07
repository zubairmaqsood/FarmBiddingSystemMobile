package com.example.farmbiddingsystem;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiAI {

    // 👉 We are now reading the hidden API key from BuildConfig safely!
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    // The official Google Gemini 1.5 Flash endpoint
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // This interface allows your activities to wait for the AI's response safely
    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void generateText(String prompt, GeminiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Build the correct JSON payload format required by Gemini
                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);

                JSONArray partsArray = new JSONArray();
                partsArray.put(textPart);

                JSONObject contentObj = new JSONObject();
                contentObj.put("parts", partsArray);

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(contentObj);

                JSONObject payload = new JSONObject();
                payload.put("contents", contentsArray);

                // Send the request over the internet
                OutputStream os = connection.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    // Extract the pure text from the JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String extractedText = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    // Send the successful text back to the main UI thread
                    mainHandler.post(() -> callback.onSuccess(extractedText.trim()));
                } else {
                    mainHandler.post(() -> callback.onError("Error Code: " + responseCode + " (Check your API Key!)"));
                }
                connection.disconnect();

            } catch (Exception e) {
                Log.e("GeminiAI", "Network/Parsing Error", e);
                mainHandler.post(() -> callback.onError("Network error. Please check your internet."));
            }
        });
    }
}