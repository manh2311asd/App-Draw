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
    
    // Using user-provided Gemini API Key.
    private static final String API_KEY = "AIzaSyAmT2SHD3gTWPfzzlO83CUXf8IxbdStyEM";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + API_KEY;
    
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

    public void gradeArtwork(String lessonTitle, String base64ImageWithPrefix, GeminiCallback callback) {
        executorService.execute(() -> {
            try {
                String cleanBase64 = base64ImageWithPrefix;
                String mimeType = "image/jpeg";
                if (cleanBase64 != null && cleanBase64.contains(",")) {
                    if (cleanBase64.startsWith("data:") && cleanBase64.contains(";")) {
                        mimeType = cleanBase64.substring(5, cleanBase64.indexOf(";"));
                    }
                    cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                }
                // Dọn rác khoảng trắng và newline (khiến Gemini trả về 400 Bad Request)
                if (cleanBase64 != null) {
                    cleanBase64 = cleanBase64.replaceAll("\\s+", "");
                }

                String prompt = "Bạn là một Giảng viên Mỹ thuật tận tâm và chuyên nghiệp. " +
                        "Dưới đây là bài thực hành của học viên cho khóa học vẽ: '" + lessonTitle + "'. " +
                        "Hãy quan sát ảnh chụp tác phẩm và đưa ra nhận xét ngắn gọn dưới định dạng JSON nguyên thủy (không có markdown box). " +
                        "Cấu trúc JSON yêu cầu bao gồm đúng 2 trường: " +
                        "\"feedback\": \"(Nhận xét ngắn về ưu điểm, biểu cảm hoặc kỹ thuật nét vẽ/tô màu)\", " +
                        "\"tip\": \"(Một mẹo thật ngắn 1 câu để giúp học viên cải thiện điểm yếu)\"";

                JSONObject payload = new JSONObject();
                JSONArray contentsArr = new JSONArray();
                JSONObject contentObj = new JSONObject();
                JSONArray partsArr = new JSONArray();

                // Text part
                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);
                partsArr.put(textPart);

                // Image part
                JSONObject inlineData = new JSONObject();
                inlineData.put("mimeType", mimeType);
                inlineData.put("data", cleanBase64);
                
                JSONObject imagePart = new JSONObject();
                imagePart.put("inlineData", inlineData);
                partsArr.put(imagePart);

                contentObj.put("parts", partsArr);
                contentsArr.put(contentObj);
                payload.put("contents", contentsArr);

                // Post request
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000); // 15 seconds
                conn.setReadTimeout(15000);

                OutputStream os = conn.getOutputStream();
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder responseString = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        responseString.append(responseLine.trim());
                    }
                    br.close();

                    JSONObject root = new JSONObject(responseString.toString());
                    JSONArray candidates = root.optJSONArray("candidates");
                    if (candidates != null && candidates.length() > 0) {
                        JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                        if (content != null) {
                            JSONArray responseParts = content.optJSONArray("parts");
                            if (responseParts != null && responseParts.length() > 0) {
                                String textResponse = responseParts.getJSONObject(0).optString("text");
                                
                                // Strip markdown json block if exists
                                textResponse = textResponse.replace("```json", "").replace("```", "").trim();
                                JSONObject resultJson = new JSONObject(textResponse);
                                
                                String feedback = resultJson.optString("feedback", "Tác phẩm rất đẹp và sáng tạo!");
                                String tip = resultJson.optString("tip", "Tiếp tục luyện tập mỗi ngày nhé.");
                                
                                mainHandler.post(() -> callback.onSuccess(feedback, tip));
                                return;
                            }
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

    public void chat(List<ChatMessage> history, ChatCallback callback) {
        executorService.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                JSONArray contentsArr = new JSONArray();

                for (ChatMessage msg : history) {
                    JSONObject contentObj = new JSONObject();
                    contentObj.put("role", msg.role);
                    
                    JSONArray partsArr = new JSONArray();
                    if (msg.text != null && !msg.text.isEmpty()) {
                        JSONObject textPart = new JSONObject();
                        textPart.put("text", msg.text);
                        partsArr.put(textPart);
                    }
                    if (msg.base64Image != null && !msg.base64Image.isEmpty()) {
                        String cleanBase64 = msg.base64Image;
                        String mime = "image/jpeg";
                        if (cleanBase64.contains(",")) {
                            if (cleanBase64.startsWith("data:") && cleanBase64.contains(";")) {
                                mime = cleanBase64.substring(5, cleanBase64.indexOf(";"));
                            }
                            cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                        }
                        cleanBase64 = cleanBase64.replaceAll("\\s+", "");
                        
                        JSONObject inlineData = new JSONObject();
                        inlineData.put("mimeType", mime);
                        inlineData.put("data", cleanBase64);
                        
                        JSONObject imagePart = new JSONObject();
                        imagePart.put("inlineData", inlineData);
                        partsArr.put(imagePart);
                    }
                    contentObj.put("parts", partsArr);
                    contentsArr.put(contentObj);
                }

                // Tiêm System Prompt để AI cư xử như trợ lý vẽ (Tùy chọn, thêm vào đầu nếu cần, nhưng Gemini Flash/Pro đôi khi bắt buộc SystemInstruction dùng param khác. Ta có thể mặc kệ hoặc add ngầm msg đầu tiên).
                payload.put("contents", contentsArr);

                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);

                OutputStream os = conn.getOutputStream();
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder responseString = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) responseString.append(responseLine.trim());
                    br.close();

                    JSONObject root = new JSONObject(responseString.toString());
                    JSONArray candidates = root.optJSONArray("candidates");
                    if (candidates != null && candidates.length() > 0) {
                        JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                        if (content != null) {
                            JSONArray responseParts = content.optJSONArray("parts");
                            if (responseParts != null && responseParts.length() > 0) {
                                String textResponse = responseParts.getJSONObject(0).optString("text");
                                mainHandler.post(() -> callback.onSuccess(textResponse));
                                return;
                            }
                        }
                    }
                    mainHandler.post(() -> callback.onError("Không có nội dung phản hồi từ AI"));
                } else {
                    String responseMsg = "";
                    try { responseMsg = conn.getResponseMessage(); } catch (Exception ignored) {}
                    final String finalMsg = responseMsg;
                    mainHandler.post(() -> callback.onError("Lỗi kết nối từ server: " + responseCode + " - " + finalMsg));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError("Lỗi xử lý: " + e.getMessage()));
            }
        });
    }
}
