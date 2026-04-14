Add-Type -AssemblyName System.Drawing
$images = Get-ChildItem -Path app\src\main\res\drawable\* -Include *.png,*.jpg | Where-Object { $_.Length -gt 1MB }
foreach ($img in $images) {
    try {
        $bmp = [System.Drawing.Bitmap]::FromFile($img.FullName)
        $factor = 500.0 / [Math]::Max($bmp.Width, $bmp.Height)
        if ($factor -ge 1) { $bmp.Dispose(); continue }
        $newWidth = [int][math]::Round($bmp.Width * $factor)
        $newHeight = [int][math]::Round($bmp.Height * $factor)
        $newBmp = New-Object System.Drawing.Bitmap($newWidth, $newHeight)
        $g = [System.Drawing.Graphics]::FromImage($newBmp)
        $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $g.DrawImage($bmp, 0, 0, $newWidth, $newHeight)
        $g.Dispose()
        $bmp.Dispose()
        $tempPath = $img.FullName + ".tmp"
        $newBmp.Save($tempPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $newBmp.Dispose()
        Remove-Item $img.FullName -Force
        Rename-Item $tempPath $img.Name
        Write-Output "Resized $($img.Name)"
    } catch {
        Write-Output "Error resizing $($img.Name): $_"
    }
}
