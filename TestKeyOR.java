import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestKeyOR {
    public static void main(String[] args) {
        try {
            String apiKey = "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";
            String urlStr = "https://openrouter.ai/api/v1/chat/completions";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HTTP-Referer", "https://github.com/appdraw");
            conn.setRequestProperty("X-Title", "AppDraw");
            conn.setDoOutput(true);
            
            String json = "{" +
                "\"model\": \"meta-llama/llama-3.2-11b-vision-instruct:free\"," +
                "\"messages\": [" +
                    "{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}," +
                    "{\"role\": \"user\", \"content\": [" +
                        "{\"type\": \"text\", \"text\": \"hi\"}" +
                    "]}" +
                "]" +
            "}";
            
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
