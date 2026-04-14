using System;
using System.Drawing;
using System.Drawing.Imaging;

public class ImagePatcher3 {
    public static void Main() {
        string inPath = "app/src/main/res/drawable/artwork_banner.jpg";
        string outPath = "app/src/main/res/drawable/artwork_banner_transparent.png";
        
        using (Bitmap src = new Bitmap(inPath))
        using (Bitmap dest = new Bitmap(src.Width, src.Height, PixelFormat.Format32bppArgb))
        {
            for(int y = 0; y < src.Height; y++) {
                for(int x = 0; x < src.Width; x++) {
                    Color c = src.GetPixel(x, y);
                    
                    // Calculate distance to pure white
                    double d = Math.Sqrt(Math.Pow(255 - c.R, 2) + Math.Pow(255 - c.G, 2) + Math.Pow(255 - c.B, 2));
                    
                    int a = 255;
                    if (d < 15) {
                        a = 0;
                    } else if (d < 120) {
                        // Smooth feathering
                        a = (int)((d - 15) / 105.0 * 255.0);
                        
                        // Optional: Un-premultiply the white background to restore original color 
                        // c.R = (c.R - 255 * (1 - a / 255.0)) / (a / 255.0)
                    }
                    
                    dest.SetPixel(x, y, Color.FromArgb(a, c.R, c.G, c.B));
                }
            }
            // To ensure we get rid of white bleeding from the original white background, we could also floodfill.
            // But this alpha feathering usually looks infinitely better than a hard threshold!
            dest.Save(outPath, ImageFormat.Png);
        }
    }
}
