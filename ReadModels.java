import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadModels {
    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("models.json")));
            Pattern p = Pattern.compile("\"name\":\\s*\"(models/gemini-.*?)\"");
            Matcher m = p.matcher(content);
            while(m.find()) {
                System.out.println(m.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
