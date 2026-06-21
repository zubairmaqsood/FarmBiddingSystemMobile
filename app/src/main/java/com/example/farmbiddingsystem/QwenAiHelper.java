package com.example.farmbiddingsystem; // Match your actual package name

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QwenAiHelper {

    private static final String TAG = "QwenAiHelper";

    // Credentials from your uploaded workspace CSV
    private static final String API_KEY = "sk-ws-H.IYLHDM.p7KD.MEYCIQC2__hUx58nhWhJTLGCOTdM32wursp3JBVzkBzix48YwgIhAODrgCNzRXame3RSCibq_Ukrrh2jUclJZkf1lDX4mTC5";
    private static final String BASE_URL = "https://ws-6eqpuy33whuotasz.ap-southeast-1.maas.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final OkHttpClient client;
    private final Handler mainHandler;

    public interface AiCallback {
        void onSuccess(String description, String qualityGrade, double suggestedPrice);
        void onFailure(String errorMessage);
    }

    public QwenAiHelper() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // 1. METHOD FOR IMAGE + TEXT (Quality & Description)
    public void analyzeCropImage(Bitmap cropBitmap, String itemName, AiCallback callback) {
        new Thread(() -> {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                cropBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                byte[] byteArray = outputStream.toByteArray();
                String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT).replaceAll("\\s+", "");

                String textPrompt = "You are an expert agricultural appraiser analyzing a listing for an auction app.\n" +
                        "Item Name: " + itemName + "\n\n" +
                        "Analyze the attached image and determine:\n" +
                        "1. A 2-3 sentence highly professional and appealing description for buyers.\n" +
                        "2. An estimated quality grade choosing strictly from: Premium, Good, Fair, or Poor.\n" +
                        "Provide your response strictly in the following JSON format without any markdown wrappers:\n" +
                        "{\n" +
                        "  \"description\": \"Your generated text description...\",\n" +
                        "  \"quality\": \"Premium/Good/Fair/Poor\"\n" +
                        "}";

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", "qwen-vl-max"); // Vision model

                JSONArray messagesArray = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");

                JSONArray contentArray = new JSONArray();

                JSONObject textContent = new JSONObject();
                textContent.put("type", "text");
                textContent.put("text", textPrompt);
                contentArray.put(textContent);

                JSONObject imageContent = new JSONObject();
                imageContent.put("type", "image_url");
                JSONObject imageUrlObj = new JSONObject();
                imageUrlObj.put("url", "data:image/jpeg;base64," + base64Image);
                imageContent.put("image_url", imageUrlObj);
                contentArray.put(imageContent);

                userMessage.put("content", contentArray);
                messagesArray.put(userMessage);
                jsonBody.put("messages", messagesArray);

                JSONObject responseFormat = new JSONObject();
                responseFormat.put("type", "json_object");
                jsonBody.put("response_format", responseFormat);

                makeApiCall(jsonBody, callback);

            } catch (Exception e) {
                sendFailure(callback, "Initialization error: " + e.getLocalizedMessage());
            }
        }).start();
    }

    // 2. METHOD FOR TEXT ONLY (Price Prediction based on Muns)
    public void suggestPriceText( String itemName, String quantity, String detailsText, AiCallback callback) {
        new Thread(() -> {
            try {
                String textPrompt = "You are an expert agricultural analyst in Pakistan evaluating a listing for an auction app.\n" +
                        "Item Name: " + itemName + "\n" +
                        "Quantity: " + quantity + " Muns (1 Mun = 40 KG)\n" +
                        "Quality/Details: " + detailsText + "\n\n" +
                        "Calculate a realistic, fair total starting market base price in PKR (Rs) for the entire " + quantity + " Mun quantity.\n" +
                        "Provide your response strictly in the following JSON format without any markdown wrappers:\n" +
                        "{\n" +
                        "  \"suggested_price\": 12000\n" +
                        "}";

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", "qwen-max"); // Text model

                JSONArray messagesArray = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");

                JSONArray contentArray = new JSONArray();
                JSONObject textContent = new JSONObject();
                textContent.put("type", "text");
                textContent.put("text", textPrompt);
                contentArray.put(textContent);

                userMessage.put("content", contentArray);
                messagesArray.put(userMessage);
                jsonBody.put("messages", messagesArray);

                JSONObject responseFormat = new JSONObject();
                responseFormat.put("type", "json_object");
                jsonBody.put("response_format", responseFormat);

                makeApiCall(jsonBody, callback);

            } catch (Exception e) {
                sendFailure(callback, "Initialization error.");
            }
        }).start();
    }

    // SHARED NETWORK EXECUTION LOGIC
    private void makeApiCall(JSONObject jsonBody, AiCallback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailure(callback, "Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (Response resp = response) {
                    if (!resp.isSuccessful()) {
                        sendFailure(callback, "API Server error: Code " + resp.code());
                        return;
                    }

                    String responseStr = resp.body().string();
                    JSONObject responseJson = new JSONObject(responseStr);
                    String contentResult = responseJson.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    JSONObject resultJson = new JSONObject(contentResult.trim());
                    String description = resultJson.optString("description", "");
                    String quality = resultJson.optString("quality", "");
                    double price = resultJson.optDouble("suggested_price", 0.0);

                    mainHandler.post(() -> callback.onSuccess(description, quality, price));

                } catch (Exception e) {
                    sendFailure(callback, "Parsing failed.");
                }
            }
        });
    }

    private void sendFailure(AiCallback callback, String message) {
        mainHandler.post(() -> callback.onFailure(message));
    }
}