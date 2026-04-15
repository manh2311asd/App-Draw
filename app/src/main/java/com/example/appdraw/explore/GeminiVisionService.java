package com.example.appdraw.explore;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiVisionService {

    // Using Custom API Key from .env
    private static final String API_KEY = com.example.appdraw.BuildConfig.AI_API_KEY;
    private final String API_URL = "https://chat.trollllm.xyz/v1/chat/completions";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface GeminiCallback {
        void onSuccess(String feedback, String tip);

        void onError(String error);
    }

    public interface ChatCallback {
        void onSuccess(String reply);

        void onError(String error);
    }

    public static class ChatMessage {
        public String role; // "user" or "model"
        public String text;
        public String base64Image; // only for user, optional

        public ChatMessage(String role, String text, String base64Image) {
            this.role = role;
            this.text = text;
            this.base64Image = base64Image;
        }
    }

    // Call API for grading artwork
    public void gradeArtwork(String lessonTitle, String base64ImageWithPrefix, GeminiCallback callback) {
        executorService.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("HTTP-Referer", "https://github.com/artcraft");
                conn.setRequestProperty("X-Title", "ArtCraft");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000); // 30s
                conn.setReadTimeout(60000);    // 60s

                JSONObject payload = new JSONObject();
                payload.put("model", "claude-haiku-4.5");
                payload.put("max_tokens", 4096);

                JSONArray messages = new JSONArray();

                // User Request
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");

                JSONArray contentParts = new JSONArray();

                // Text part
                JSONObject textPart = new JSONObject();
                textPart.put("type", "text");
                textPart.put("text", "[SYSTEM INSTRUCTION: Bạn là một Giảng viên Mỹ thuật AI tận tâm và chuyên nghiệp.]\n\n" +
                        "Dưới đây là bài thực hành của học viên cho khóa học vẽ: '" + lessonTitle + "'. " +
                        "Hãy quan sát ảnh chụp tác phẩm và đưa ra nhận xét ngắn gọn dưới định dạng JSON nguyên thủy (không có markdown box). " +
                        "Cấu trúc JSON yêu cầu bao gồm đúng 2 trường:\n" +
                        "\"feedback\": \"(Nhận xét ngắn về ưu điểm, biểu cảm hoặc kỹ thuật nét vẽ/tô màu)\",\n" +
                        "\"tip\": \"(Một mẹo thật ngắn 1 câu để giúp học viên cải thiện điểm yếu)\"");
                contentParts.put(textPart);

                // Image part
                String cleanBase64 = base64ImageWithPrefix;
                if (cleanBase64.startsWith("data:")) {
                    cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                }
                cleanBase64 = cleanBase64.replaceAll("\\s+", "");

                JSONObject imagePart = new JSONObject();
                imagePart.put("type", "image_url");
                JSONObject imageUrlObj = new JSONObject();
                imageUrlObj.put("url", "data:image/jpeg;base64," + cleanBase64);
                imagePart.put("image_url", imageUrlObj);
                contentParts.put(imagePart);

                userMessage.put("content", contentParts);
                messages.put(userMessage);

                payload.put("messages", messages);

                String jsonInputString = payload.toString();

                OutputStream os = conn.getOutputStream();
                os.write(jsonInputString.getBytes("utf-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode >= 200 && responseCode < 300) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder responseString = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) responseString.append(responseLine.trim());
                    br.close();

                    JSONObject root = new JSONObject(responseString.toString());
                    JSONArray choices = root.optJSONArray("choices");
                    if (choices != null && choices.length() > 0) {
                        JSONObject msg = choices.getJSONObject(0).optJSONObject("message");
                        if (msg != null) {
                            String textResponse = msg.optString("content");

                            // Strip markdown json block if exists
                            textResponse = textResponse.replace("```json", "").replace("```", "").trim();
                            // Fix potential bad parsing
                            if (textResponse.contains("{") && textResponse.contains("}")) {
                                if (!textResponse.startsWith("{")) {
                                    textResponse = textResponse.substring(textResponse.indexOf("{"));
                                }
                                if (!textResponse.endsWith("}")) {
                                    textResponse = textResponse.substring(0, textResponse.lastIndexOf("}")+1);
                                }
                            }

                            JSONObject resultJson;
                            try {
                                resultJson = new JSONObject(textResponse);
                            } catch (Exception e) {
                                // Fallback if AI didn't follow JSON format correctly
                                resultJson = new JSONObject();
                                resultJson.put("feedback", "Tác phẩm rất đẹp và đầy sáng tạo!");
                                resultJson.put("tip", "Tiếp tục phát huy và luyện tập thật nhiều nhé.");
                            }

                            String feedback = resultJson.optString("feedback", "Tác phẩm rất đẹp và sáng tạo!");
                            String tip = resultJson.optString("tip", "Tiếp tục luyện tập mỗi ngày nhé.");

                            mainHandler.post(() -> callback.onSuccess(feedback, tip));
                            return;
                        }
                    }
                    mainHandler.post(() -> callback.onError("Không có nội dung phản hồi từ AI"));
                } else {
                    String responseMsg = "";
                    try { responseMsg = conn.getResponseMessage(); } catch (Exception ignored) {}
                    
                    String errorBody = "";
                    try {
                        java.io.InputStream errorStream = conn.getErrorStream();
                        if (errorStream != null) {
                            BufferedReader brError = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                            StringBuilder errorStr = new StringBuilder();
                            String errorLine;
                            while ((errorLine = brError.readLine()) != null) errorStr.append(errorLine.trim());
                            brError.close();
                            errorBody = errorStr.toString();
                        }
                    } catch (Exception ignored) {}
                    
                    final String finalMsg = responseMsg + " | " + errorBody;
                    mainHandler.post(() -> callback.onError("Lỗi kết nối từ server: " + responseCode + " - " + finalMsg));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError("Lỗi xử lý: " + e.getMessage()));
            }
        });
    }

    // Call API for Chatting
    public void chat(List<ChatMessage> history, ChatCallback callback) {
        executorService.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("HTTP-Referer", "https://github.com/artcraft");
                conn.setRequestProperty("X-Title", "ArtCraft");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);

                boolean hasImage = false;
                for (ChatMessage msg : history) {
                    if (msg.base64Image != null && !msg.base64Image.isEmpty()) {
                        hasImage = true;
                        break;
                    }
                }

                JSONObject payload = new JSONObject();
                payload.put("model", "claude-haiku-4.5");
                payload.put("max_tokens", 4096);

                JSONArray messages = new JSONArray();

                boolean isFirstUserMessage = true;

                for (ChatMessage msg : history) {
                    JSONObject msgObj = new JSONObject();
                    msgObj.put("role", msg.role.equals("model") ? "assistant" : "user");
                    
                    JSONArray contentParts = new JSONArray();

                    if (msg.text != null && !msg.text.isEmpty()) {
                        JSONObject textPart = new JSONObject();
                        textPart.put("type", "text");
                        String textContent = msg.text;
                        if (isFirstUserMessage && msg.role.equals("user")) {
                            textContent = "[SYSTEM: Bạn là Trợ lý Mỹ thuật AI (tên Phong AI). LUÔN LUÔN trả lời bằng TIẾNG VIỆT 100%. Tuyệt đối KHÔNG hiển thị thẻ <thought> hay quá trình suy nghĩ! BẠN CHỈ LÀ TRỢ LÝ VĂN BẢN, bạn KHÔNG THỂ tạo hay vẽ ra hình ảnh (chỉ hướng dẫn bằng lời). Tuyệt đối KHÔNG sinh ra các ký hiệu như <start_of_image>, <image> hay giả vờ đã vẽ xong.] " + textContent;
                            isFirstUserMessage = false;
                        }
                        textPart.put("text", textContent);
                        contentParts.put(textPart);
                    }

                    if (msg.base64Image != null && !msg.base64Image.isEmpty()) {
                        String cleanBase64 = msg.base64Image;
                        if (cleanBase64.startsWith("data:")) {
                            cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                        }
                        cleanBase64 = cleanBase64.replaceAll("\\s+", "");

                        JSONObject imagePart = new JSONObject();
                        imagePart.put("type", "image_url");
                        JSONObject imgUrl = new JSONObject();
                        imgUrl.put("url", "data:image/jpeg;base64," + cleanBase64);
                        imagePart.put("image_url", imgUrl);
                        contentParts.put(imagePart);
                    }
                    
                    if (contentParts.length() > 0) {
                        msgObj.put("content", contentParts);
                        messages.put(msgObj);
                    }
                }

                payload.put("messages", messages);

                String jsonInputString = payload.toString();

                OutputStream os = conn.getOutputStream();
                os.write(jsonInputString.getBytes("utf-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode >= 200 && responseCode < 300) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder responseString = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) responseString.append(responseLine.trim());
                    br.close();

                    JSONObject root = new JSONObject(responseString.toString());
                    JSONArray choices = root.optJSONArray("choices");
                    if (choices != null && choices.length() > 0) {
                        JSONObject msg = choices.getJSONObject(0).optJSONObject("message");
                        if (msg != null) {
                            String textResponse = msg.optString("content");

                            if (textResponse.contains("<thought>")) {
                                textResponse = textResponse.replaceAll("(?s)<thought>.*?</thought>", "").trim();
                            }
                            if (textResponse.contains("<think>")) {
                                textResponse = textResponse.replaceAll("(?s)<think>.*?</think>", "").trim();
                            }
                            textResponse = textResponse.replaceAll("<thought>", "").replaceAll("</thought>", "");
                            textResponse = textResponse.replaceAll("<think>", "").replaceAll("</think>", "");

                            final String finalResponse = textResponse;
                            mainHandler.post(() -> callback.onSuccess(finalResponse));
                            return;
                        }
                    }
                    mainHandler.post(() -> callback.onError("Không có nội dung phản hồi từ AI"));
                } else {
                    String responseMsg = "";
                    try { responseMsg = conn.getResponseMessage(); } catch (Exception ignored) {}
                    
                    String errorBody = "";
                    try {
                        java.io.InputStream errorStream = conn.getErrorStream();
                        if (errorStream != null) {
                            BufferedReader brError = new BufferedReader(new InputStreamReader(errorStream, "utf-8"));
                            StringBuilder errorStr = new StringBuilder();
                            String errorLine;
                            while ((errorLine = brError.readLine()) != null) errorStr.append(errorLine.trim());
                            brError.close();
                            errorBody = errorStr.toString();
                        }
                    } catch (Exception ignored) {}
                    
                    final String finalMsg = responseMsg + " | " + errorBody;
                    mainHandler.post(() -> callback.onError("Lỗi kết nối từ server: " + responseCode + " - " + finalMsg));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError("Lỗi xử lý: " + e.getMessage()));
            }
        });
    }
}
