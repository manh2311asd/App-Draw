using System;
using System.Drawing;
using System.Drawing.Imaging;

public class ImagePatcher2 {
    public static void Main() {
        string inPath = "app/src/main/res/drawable/artwork_banner.jpg";
        string outPath = "app/src/main/res/drawable/artwork_banner_transparent.png";
        Bitmap bmp = new Bitmap(inPath);
        
        // JPEG artifact removal tolerance
        int tolerance = 240; 
        
        for(int y = 0; y < bmp.Height; y++) {
            for(int x = 0; x < bmp.Width; x++) {
                Color c = bmp.GetPixel(x, y);
                if (c.R > tolerance && c.G > tolerance && c.B > tolerance) {
                    bmp.SetPixel(x, y, Color.Transparent);
                }
            }
        }
        
        bmp.Save(outPath, ImageFormat.Png);
    }
}
