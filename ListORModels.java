import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListORModels {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://openrouter.ai/api/v1/models");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            
            String[] parts = sb.toString().split("\"id\":\"");
            for(String p : parts) {
                int end = p.indexOf("\"");
                if (end > 0) {
                    String name = p.substring(0, end);
                    System.out.println("ID=" + name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
