$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
# Bo qua CommandHandler.java: file thu JSON cu, demo nay khong dung Gson.
$sources = @('ServerConfig.java','ServerMain.java','ControlProtocol.java','Session.java',
    'SessionManager.java','StreamManager.java','ClientHandler.java','UdpServer.java',
    'DemoConnection.java','VideoFrames.java','WebcamSender.java','WebcamViewer.java',
    'TestVideoFrames.java','InteractiveClient.java','TestClient.java','TestUdpClient.java',
    'TestRegisterClient.java','TestRelayClient.java')
$compiler = 'C:\Program Files\Java\jdk-21.0.12.1\bin\javac.exe'
if (-not (Test-Path -LiteralPath $compiler)) { $compiler = 'javac' }
& $compiler -encoding UTF-8 -cp 'lib/*' -d out @sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
Write-Host 'Build OK'
