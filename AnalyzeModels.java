import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;

public class AnalyzeModels {
    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("models.json")));
            JSONObject root = new JSONObject(content);
            JSONArray models = root.getJSONArray("models");
            for (int i = 0; i < models.length(); i++) {
                JSONObject model = models.getJSONObject(i);
                JSONArray methods = model.optJSONArray("supportedGenerationMethods");
                if (methods != null) {
                    for (int j = 0; j < methods.length(); j++) {
                        if (methods.getString(j).equals("generateContent")) {
                            System.out.println(model.getString("name"));
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
