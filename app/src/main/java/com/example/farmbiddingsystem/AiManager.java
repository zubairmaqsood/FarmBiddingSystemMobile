package com.example.farmbiddingsystem;

import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class AiManager {

    // 🔑 Your integrated API Key
    private static final String API_KEY = "AQ.Ab8RN6J-GleOIa7EIFXfe3Uh0S_AMYCKRkeFrGisFd_mrY9QOg";

    // ⚠️ IMPORTANT: Remember to change this URL to match your actual AI provider's endpoint!
    private static final String API_URL = "https://api.your-ai-provider.com/v1/chat/completions";

    public interface AiCallback {
        void onResponse(String aiAnswer);
        void onError(String errorMessage);
    }

    public static void askAI(String userPrompt, AiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Build the JSON payload to send to the AI
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-4o-mini"); // Or your provider's specific model name

            JSONArray messages = new JSONArray();
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", "user");
            messageObject.put("content", userPrompt);
            messages.put(messageObject);

            jsonBody.put("messages", messages);

            // Set up the network request
            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            // Execute the network call asynchronously (in the background)
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseString = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseString);

                            // Parse out the text answer from the AI's JSON response
                            String aiAnswer = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            callback.onResponse(aiAnswer);
                        } catch (Exception e) {
                            callback.onError("Parsing error: " + e.getMessage());
                        }
                    } else {
                        callback.onError("Server Error: " + response.code() + " - " + response.message());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}