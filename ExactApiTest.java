import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExactApiTest {
    public static void main(String[] args) {
        try {
            String apiKey = "AIzaSyCvZcoKYoaD08Di9xg-wy7v9e016fA7r8M";
            String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // Exact JSON sent from ChatActivity for "giúp tôi vẽ hình Songoku"
            // Note: chatHistory has 2 messages: 
            // 1. the prompt: giúp tôi vẽ hình Songoku
            
            String json = "{\"systemInstruction\":{\"parts\":[{\"text\":\"Bạn là Trợ lý Mỹ thuật AI...\"}]},\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"giúp tôi vẽ hình Songoku\"}]}]}";
            System.out.println("Payload: " + json);
            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("utf-8"));
            os.flush();
            os.close();
            
            int responseCode = conn.getResponseCode();
            System.out.println("Code: " + responseCode);
            java.io.InputStream is = (responseCode >= 400) ? conn.getErrorStream() : conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
