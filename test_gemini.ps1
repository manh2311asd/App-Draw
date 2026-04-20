$apiKey = "AIzaSyAmT2SHD3gTWPfzzlO83CUXf8IxbdStyEM"
$url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
$body = '{"contents":[{"parts":[{"text":"hi"}]}]}'

try {
    $res = Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType 'application/json'
    Write-Host "SUCCESS:"
    $res | ConvertTo-Json -Depth 10
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $errorMsg = $reader.ReadToEnd()
    Write-Host "ERROR:"
    Write-Host $errorMsg
}
