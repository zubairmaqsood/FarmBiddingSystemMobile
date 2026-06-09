package com.example.farmbiddingsystem;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiAI {

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + API_KEY;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    // 👉 NEW: Now accepts a Bitmap image along with the text prompt!
    public void generateResponse(String prompt, Bitmap image, GeminiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONArray partsArray = new JSONArray();

                // 1. Add the Text Prompt
                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);
                partsArray.put(textPart);

                // 2. Add the Image (If the farmer uploaded one)
                if (image != null) {
                    // Convert Bitmap to Base64 String
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    // Compress image slightly to ensure fast network uploads
                    image.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP);

                    // Build the Image JSON object required by Gemini
                    JSONObject inlineData = new JSONObject();
                    inlineData.put("mime_type", "image/jpeg");
                    inlineData.put("data", base64Image);

                    JSONObject imagePart = new JSONObject();
                    imagePart.put("inline_data", inlineData);

                    partsArray.put(imagePart);
                }

                JSONObject contentObj = new JSONObject();
                contentObj.put("parts", partsArray);

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(contentObj);

                JSONObject payload = new JSONObject();
                payload.put("contents", contentsArray);

                // Retain Google Search Grounding for live web browsing
                JSONObject googleSearchTool = new JSONObject();
                googleSearchTool.put("googleSearch", new JSONObject());
                JSONArray toolsArray = new JSONArray();
                toolsArray.put(googleSearchTool);
                payload.put("tools", toolsArray);

                // Send the request
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

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String extractedText = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    mainHandler.post(() -> callback.onSuccess(extractedText.trim()));
                } else if (responseCode == 429) {
                    mainHandler.post(() -> callback.onError("Server busy (429). Please wait 60 seconds."));
                } else {
                    mainHandler.post(() -> callback.onError("Error Code: " + responseCode));
                }
                connection.disconnect();

            } catch (Exception e) {
                Log.e("GeminiAI", "Network/Parsing Error", e);
                mainHandler.post(() -> callback.onError("Network error. Check internet connection."));
            }
        });
    }
}