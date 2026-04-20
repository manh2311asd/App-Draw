import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestKey {
    public static void main(String[] args) {
        try {
            String apiKey = "AIzaSyBByaD4nOJI3knu5dhwzvecXnn_G8yqN4s";
            String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String json = "{\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"giúp tôi vẽ hình Songoku\"}]}]}";
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
