import android.graphics.BitmapFactory;
import java.io.File;

public class ImageInfo {
    public static void main(String[] args) {
        String[] files = {"app/src/main/res/drawable/artwork_banner.jpg", "app/src/main/res/drawable/banner_watercolor.png", "app/src/main/res/mipmap-xxhdpi/ic_launcher.webp"};
        for(String f : files) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f, options);
            System.out.println(f + " : " + options.outWidth + "x" + options.outHeight);
        }
    }
}
