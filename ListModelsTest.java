import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListModelsTest {
    public static void main(String[] args) {
        try {
            String apiKey = "YOUR_API_KEY_HERE";
            String urlStr = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            int responseCode = conn.getResponseCode();
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
