using System.Drawing;
using System.Drawing.Imaging;

public class ImagePatcher {
    public static void Main() {
        string inPath = "app/src/main/res/drawable/artwork_banner.jpg";
        string outPath = "app/src/main/res/drawable/artwork_banner_transparent.png";
        Bitmap bmp = new Bitmap(inPath);
        bmp.MakeTransparent(Color.White);
        bmp.Save(outPath, ImageFormat.Png);
    }
}
