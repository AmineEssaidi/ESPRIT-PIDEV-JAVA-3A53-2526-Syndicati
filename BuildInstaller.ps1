# Syndicati - Professional Installer Builder (Master Console)
# This script builds the breathtaking Syndicati installer with a bulletproof launcher.

try {
    Add-Type -AssemblyName PresentationFramework
    Add-Type -AssemblyName PresentationCore
    Add-Type -AssemblyName WindowsBase

    # Hide console for premium feel
    $Win32 = @"
    using System;
    using System.Runtime.InteropServices;
    public static class Win32Console {
        [DllImport("kernel32.dll")] public static extern IntPtr GetConsoleWindow();
        [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
    }
"@
    Add-Type $Win32
    $handle = [Win32Console]::GetConsoleWindow()
    if ($handle -ne [IntPtr]::Zero) { [Win32Console]::ShowWindow($handle, 0) | Out-Null }

    # XAML Definition
    $inputXML = @"
<Window xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml"
        Title="Syndicati Builder" Height="700" Width="1000" 
        WindowStyle="None" AllowsTransparency="True" Background="Transparent"
        WindowStartupLocation="CenterScreen">
    <Border CornerRadius="24" BorderThickness="1" BorderBrush="#1A1A2E">
        <Border.Background>
            <RadialGradientBrush Center="0.5,0" GradientOrigin="0.5,0" RadiusX="1.2" RadiusY="1.2">
                <GradientStop Color="#0F172A" Offset="0"/>
                <GradientStop Color="#050508" Offset="1"/>
            </RadialGradientBrush>
        </Border.Background>

        <Grid>
            <Grid.RowDefinitions>
                <RowDefinition Height="80"/>
                <RowDefinition Height="*"/>
                <RowDefinition Height="100"/>
            </Grid.RowDefinitions>

            <Grid Grid.Row="0" Margin="40,20,40,0">
                <StackPanel Orientation="Horizontal" VerticalAlignment="Center">
                    <TextBlock Text="SYNDICATI" FontSize="28" FontWeight="Bold" Foreground="White"/>
                    <Border Background="#6366F1" CornerRadius="4" Margin="15,0,0,0" Padding="8,4">
                        <TextBlock Text="BUILDER CORE" Foreground="White" FontSize="10" FontWeight="Bold"/>
                    </Border>
                </StackPanel>
                <Button Name="BtnClose" Content="×" FontSize="24" Foreground="#444455" Background="Transparent" BorderThickness="0" HorizontalAlignment="Right" Cursor="Hand"/>
            </Grid>

            <Grid Grid.Row="1" Margin="40,20">
                <Grid.ColumnDefinitions>
                    <ColumnDefinition Width="300"/>
                    <ColumnDefinition Width="*"/>
                </Grid.ColumnDefinitions>

                <StackPanel Grid.Column="0" Margin="0,0,30,0">
                    <TextBlock Text="BUILD CONFIGURATION" FontSize="11" FontWeight="Bold" Foreground="#6366F1" Margin="0,0,0,20"/>
                    <CheckBox Name="CheckBundleJava" Content="Bundle JDK 25 Runtime (required)" Foreground="#9999AA" IsChecked="True" IsEnabled="False" Margin="0,0,0,15"/>
                    <CheckBox Name="CheckBundlePython" Content="Include biometric + LogAI Python helpers (required)" Foreground="#9999AA" IsChecked="True" IsEnabled="False" Margin="0,0,0,15"/>
                    <CheckBox Name="CheckAIModels" Content="Include direct Gemini/Groq agent config (required)" Foreground="#9999AA" IsChecked="True" IsEnabled="False" Margin="0,0,0,15"/>
                    <Button Name="BtnBuild" Content="GENERATE INSTALLER" Height="50" Cursor="Hand" Margin="0,30,0,0">
                        <Button.Style>
                            <Style TargetType="Button">
                                <Setter Property="Background" Value="#6366F1"/>
                                <Setter Property="Foreground" Value="White"/>
                                <Setter Property="FontWeight" Value="Bold"/>
                                <Setter Property="Template">
                                    <Setter.Value>
                                        <ControlTemplate TargetType="Button">
                                            <Border Background="{TemplateBinding Background}" CornerRadius="12">
                                                <ContentPresenter HorizontalAlignment="Center" VerticalAlignment="Center"/>
                                            </Border>
                                        </ControlTemplate>
                                    </Setter.Value>
                                </Setter>
                            </Style>
                        </Button.Style>
                    </Button>
                </StackPanel>

                <Border Grid.Column="1" Background="#0A0A12" CornerRadius="16" BorderThickness="1" BorderBrush="#1A1A2E">
                    <ScrollViewer Name="LogScroll" VerticalScrollBarVisibility="Auto">
                        <TextBlock Name="TxtLogs" Text="[READY] System initialized." Foreground="#444455" FontSize="11" FontFamily="Consolas" Margin="20" TextWrapping="Wrap"/>
                    </ScrollViewer>
                </Border>
            </Grid>

            <Border Grid.Row="2" Background="#0A0A12" CornerRadius="0,0,24,24">
                <Grid Margin="40,0">
                    <StackPanel VerticalAlignment="Center">
                        <Grid Margin="0,0,0,10">
                            <TextBlock Name="TxtStep" Text="IDLE" Foreground="#6366F1" FontSize="12" FontWeight="Bold"/>
                            <TextBlock Name="TxtPercent" Text="0%" Foreground="#9999AA" FontSize="12" HorizontalAlignment="Right"/>
                        </Grid>
                        <Grid Height="6">
                            <Border Background="#1A1A2E" CornerRadius="3"/>
                            <Border Name="ProgressBar" Background="#6366F1" CornerRadius="3" HorizontalAlignment="Left" Width="0"/>
                        </Grid>
                    </StackPanel>
                </Grid>
            </Border>
        </Grid>
    </Border>
</Window>
"@

    $reader = [System.Xml.XmlReader]::Create([System.IO.StringReader] $inputXML)
    $window = [Windows.Markup.XamlReader]::Load($reader)

    # Elements
    $BtnClose = $window.FindName("BtnClose")
    $BtnBuild = $window.FindName("BtnBuild")
    $ProgressBar = $window.FindName("ProgressBar")
    $TxtStep = $window.FindName("TxtStep")
    $TxtPercent = $window.FindName("TxtPercent")
    $TxtLogs = $window.FindName("TxtLogs")
    $LogScroll = $window.FindName("LogScroll")

    $scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
    if ($scriptPath) { Set-Location $scriptPath }
    $global:buildQueue = [System.Collections.Concurrent.ConcurrentQueue[PSObject]]::new()

    $BtnClose.Add_Click({ $window.Close() })

    function Log($msg, $type="INFO") {
        $timestamp = Get-Date -Format "HH:mm:ss"
        $TxtLogs.Text += "`n[$timestamp] [$type] $msg"
        $LogScroll.ScrollToEnd()
    }

    function UpdateStatus($percent, $step) {
        $ProgressBar.Width = ($percent / 100) * 920
        $TxtPercent.Text = "$([int]$percent)%"
        if ($step) { $TxtStep.Text = $step.ToUpper() }
    }

    $Timer = New-Object System.Windows.Threading.DispatcherTimer
    $Timer.Interval = [TimeSpan]::FromMilliseconds(100)
    $Timer.Add_Tick({
        $item = $null
        while ($global:buildQueue.TryDequeue([ref]$item)) {
            if ($item.Type -eq "LOG") { Log $item.Message $item.Level }
            elseif ($item.Type -eq "PROGRESS") { UpdateStatus $item.Percent $item.Step }
            elseif ($item.Type -eq "FINISH") { $BtnBuild.IsEnabled = $true }
        }
    })
    $Timer.Start()

    $BtnBuild.Add_Click({
        $BtnBuild.IsEnabled = $false
        $TxtLogs.Text = "[START] Initiating production build..."
        $bundleJava = $true
        $includePythonHelpers = $true
        $includeAgentConfig = $true
        
        $iss = [powershell]::Create().AddScript({
            param($queue, $root, $bundleJava, $includePythonHelpers, $includeAgentConfig)
            function Push-Log($m, $l="INFO") { $queue.Enqueue(@{Type="LOG"; Message=$m; Level=$l}) }
            function Push-Progress($p, $s) { $queue.Enqueue(@{Type="PROGRESS"; Percent=$p; Step=$s}) }
            function Copy-IfExists($source, $destination) {
                if (Test-Path $source) {
                    Copy-Item $source $destination -Force
                    return $true
                }
                return $false
            }

            try {
                Set-Location $root
                [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
                New-Item -ItemType Directory -Path "$root\dist", "$root\installer", "$root\installer-output" -Force | Out-Null
                
                Push-Progress 10 "Compiling JAR"
                $mvn = "$root\tools\maven\bin\mvn.cmd"
                if (!(Test-Path $mvn)) { throw "Maven wrapper missing at $mvn" }
                $buildJdk = "$root\tools\jdk-25"
                if (!(Test-Path "$buildJdk\bin\java.exe")) {
                    $nestedBuildJdk = Get-ChildItem $buildJdk -Directory -ErrorAction SilentlyContinue |
                        Where-Object { Test-Path "$($_.FullName)\bin\java.exe" } |
                        Select-Object -First 1
                    if ($nestedBuildJdk) { $buildJdk = $nestedBuildJdk.FullName }
                }
                if (!(Test-Path "$buildJdk\bin\java.exe")) { throw "Build JDK missing at $buildJdk" }
                $env:JAVA_HOME = $buildJdk
                $env:Path = "$buildJdk\bin;$root\tools\maven\bin;$env:Path"
                Push-Log "Using build JDK: $buildJdk"

                $mavenOut = "$root\dist\maven-build.out.log"
                $mavenErr = "$root\dist\maven-build.err.log"
                if (Test-Path $mavenOut) { Remove-Item $mavenOut -Force }
                if (Test-Path $mavenErr) { Remove-Item $mavenErr -Force }
                $mvnProcess = Start-Process -FilePath $mvn -ArgumentList "clean", "package", "-DskipTests" -Wait -NoNewWindow -PassThru -RedirectStandardOutput $mavenOut -RedirectStandardError $mavenErr
                if ($mvnProcess.ExitCode -ne 0) {
                    foreach ($line in (Get-Content $mavenErr, $mavenOut -ErrorAction SilentlyContinue | Select-Object -Last 12)) {
                        if (![string]::IsNullOrWhiteSpace($line)) { Push-Log $line "ERROR" }
                    }
                    throw "Maven package failed with exit code $($mvnProcess.ExitCode). See dist\maven-build.err.log and dist\maven-build.out.log."
                }
                $jar = Get-ChildItem "$root\target" -Filter "*.jar" -File |
                    Where-Object { $_.Name -eq "syndicati.jar" -or $_.Name -notlike "original-*" } |
                    Sort-Object { if ($_.Name -eq "syndicati.jar") { 0 } else { 1 } }, LastWriteTime -Descending |
                    Select-Object -First 1
                if (!$jar) { throw "No packaged application jar was produced in target." }
                Push-Log "Packaged core jar: $($jar.Name)"
                
                Push-Progress 40 "Native Launcher"
                Push-Log "Creating native launcher for bundled runtime..."
                $csharpSource = @"
using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Windows.Forms;

public class SyndicatiLauncher {
    private static void Log(string logFile, string message) {
        try {
            File.AppendAllText(logFile, DateTime.Now.ToString("s") + " " + message + Environment.NewLine);
        } catch { }
    }

    public static void Main() {
        string root = AppDomain.CurrentDomain.BaseDirectory;
        string logDir = Path.Combine(root, "logs");
        Directory.CreateDirectory(logDir);
        string logFile = Path.Combine(logDir, "launcher.log");
        try {
            string runtimeDir = Path.Combine(root, "runtime");
            Log(logFile, "Launcher root: " + root);
            Log(logFile, "Runtime dir: " + runtimeDir);

            string javaExe = null;
            if (Directory.Exists(runtimeDir)) {
                javaExe = Directory.GetFiles(runtimeDir, "java.exe", SearchOption.AllDirectories).FirstOrDefault();
                if (string.IsNullOrEmpty(javaExe)) {
                    javaExe = Directory.GetFiles(runtimeDir, "javaw.exe", SearchOption.AllDirectories).FirstOrDefault();
                }
            }
            if (string.IsNullOrEmpty(javaExe)) {
                Log(logFile, "Java runtime missing.");
                MessageBox.Show("Critical Error: Java runtime is missing.\nExpected bundled runtime at:\n" + runtimeDir + "\n\nRebuild the installer with BuildInstaller.ps1.", "Syndicati");
                return;
            }
            Log(logFile, "Java executable: " + javaExe);

            string jarFile = Path.Combine(root, "app", "syndicati.jar");
            if (!File.Exists(jarFile)) {
                Log(logFile, "Jar missing: " + jarFile);
                MessageBox.Show("Critical Error: Application core missing at " + jarFile, "Syndicati");
                return;
            }
            Log(logFile, "Jar file: " + jarFile);

            ProcessStartInfo info = new ProcessStartInfo();
            info.FileName = javaExe;
            info.Arguments = "--enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dprism.order=d3d -Dprism.dirtyopts=false -Dprism.forceGPU=true --add-opens=java.base/java.lang=ALL-UNNAMED -Xms256m -Xmx2G -jar \"" + jarFile + "\"";
            info.WorkingDirectory = root;
            info.UseShellExecute = false;
            info.CreateNoWindow = true;
            info.RedirectStandardOutput = true;
            info.RedirectStandardError = true;
            info.EnvironmentVariables["SYNDICATI_HOME"] = root;
            Log(logFile, "Starting Java process.");
            Process process = Process.Start(info);
            process.OutputDataReceived += (sender, e) => { if (e.Data != null) Log(logFile, "[OUT] " + e.Data); };
            process.ErrorDataReceived += (sender, e) => { if (e.Data != null) Log(logFile, "[ERR] " + e.Data); };
            process.BeginOutputReadLine();
            process.BeginErrorReadLine();
            Log(logFile, "Java process started with PID " + process.Id + ".");
            process.WaitForExit();
            Log(logFile, "Java process exited with code " + process.ExitCode + ".");
        } catch (Exception ex) {
            Log(logFile, "Launcher exception: " + ex.ToString());
            MessageBox.Show("Launcher Error: " + ex.Message, "Syndicati");
        }
    }
}
"@
                $launcherPath = "$root\dist\Syndicati.exe"
                if (Test-Path $launcherPath) { Remove-Item $launcherPath -Force }
                Add-Type -TypeDefinition $csharpSource -ReferencedAssemblies "System.Windows.Forms" -OutputAssembly $launcherPath -OutputType WindowsApplication
                
                Push-Progress 60 "Bundling Assets"
                $appDir = "$root\dist\Syndicati_Bundle"
                if (Test-Path $appDir) { Remove-Item $appDir -Recurse -Force }
                New-Item -ItemType Directory -Path "$appDir\app" -Force | Out-Null
                
                Copy-Item $launcherPath "$appDir\Syndicati.exe" -Force
                Copy-Item $jar.FullName "$appDir\app\syndicati.jar" -Force
                Push-Log "Copied native launcher and application jar."

                if ($includeAgentConfig) {
                    Push-Log "Packaging direct Gemini/Groq agent configuration."
                    New-Item -ItemType Directory -Path "$appDir\config" -Force | Out-Null
                    Get-ChildItem "$root\config" -Filter "*.properties" -File -ErrorAction SilentlyContinue |
                        Copy-Item -Destination "$appDir\config" -Force
                    Copy-IfExists "$root\.env.local" "$appDir\.env.local" | Out-Null
                }

                foreach ($folder in @("certs")) {
                    $sourceFolder = Join-Path $root $folder
                    if (Test-Path $sourceFolder) {
                        Copy-Item $sourceFolder "$appDir\$folder" -Recurse -Force
                        Push-Log "Included runtime folder: $folder"
                    }
                }

                if ($includePythonHelpers) {
                    Push-Log "Including optional local biometric and LogAI helpers. Direct agent API mode does not need LangGraph."
                    Copy-IfExists "$root\face_detect_service.py" "$appDir\face_detect_service.py" | Out-Null
                    Copy-IfExists "$root\insightface_service.py" "$appDir\insightface_service.py" | Out-Null
                    Copy-IfExists "$root\mediapipe_service.py" "$appDir\mediapipe_service.py" | Out-Null
                    Copy-IfExists "$root\INSIGHTFACE_SETUP.md" "$appDir\INSIGHTFACE_SETUP.md" | Out-Null
                    Copy-IfExists "$root\requirements.txt" "$appDir\requirements.txt" | Out-Null

                    $logAiSource = "$root\workers\logai-anomaly-detection"
                    if (Test-Path $logAiSource) {
                        New-Item -ItemType Directory -Path "$appDir\workers\logai-anomaly-detection" -Force | Out-Null
                        Get-ChildItem $logAiSource -File |
                            Copy-Item -Destination "$appDir\workers\logai-anomaly-detection" -Force
                    }
                }
                
                if ($bundleJava) {
                    $jdkSource = "$root\tools\jdk-25"
                    if (!(Test-Path "$jdkSource\bin")) {
                        $nestedJdk = Get-ChildItem $jdkSource -Directory -ErrorAction SilentlyContinue |
                            Where-Object { Test-Path "$($_.FullName)\bin\javaw.exe" } |
                            Select-Object -First 1
                        if ($nestedJdk) { $jdkSource = $nestedJdk.FullName }
                    }
                    if (!(Test-Path "$jdkSource\bin\javaw.exe")) { throw "JDK runtime missing at $jdkSource" }
                    New-Item -ItemType Directory -Path "$appDir\runtime" -Force | Out-Null
                    Copy-Item "$jdkSource\*" "$appDir\runtime" -Recurse -Force
                    Push-Log "Bundled JDK runtime from $jdkSource"
                }

                $manifest = [ordered]@{
                    app = "Syndicati"
                    built_at = (Get-Date).ToString("s")
                    jar = $jar.Name
                    agent_mode = "direct_api_gemini_with_groq_fallback"
                    langgraph_worker_packaged = $false
                    bundled_jdk = [bool]$bundleJava
                    optional_python_helpers = [bool]$includePythonHelpers
                    agent_config_included = [bool]$includeAgentConfig
                } | ConvertTo-Json
                Set-Content -Path "$appDir\build-manifest.json" -Value $manifest -Encoding UTF8
                
                Push-Progress 85 "Packaging"
                if (Test-Path "$root\installer\app.zip") { Remove-Item "$root\installer\app.zip" -Force }
                Add-Type -AssemblyName System.IO.Compression.FileSystem
                [System.IO.Compression.ZipFile]::CreateFromDirectory($appDir, "$root\installer\app.zip")
                
                $iscc = "$root\tools\innosetup\ISCC.exe"
                $iss = "$root\installer\syndicati-installer.iss"
                if (!(Test-Path $iscc)) { throw "Inno Setup compiler missing at $iscc" }
                if (!(Test-Path $iss)) { throw "Inno Setup script missing at $iss" }
                $innoProcess = Start-Process -FilePath $iscc -ArgumentList "`"$iss`"" -Wait -NoNewWindow -PassThru
                if ($innoProcess.ExitCode -ne 0) { throw "Inno Setup failed with exit code $($innoProcess.ExitCode)" }
                
                Push-Progress 100 "Complete"
                Push-Log "SUCCESS: Production installer ready in installer-output." "SUCCESS"
            } catch {
                Push-Log "BUILD ERROR: $($_.Exception.Message)" "ERROR"
            } finally { $queue.Enqueue(@{Type="FINISH"}) }
        }).AddArgument($global:buildQueue).AddArgument($scriptPath).AddArgument($bundleJava).AddArgument($includePythonHelpers).AddArgument($includeAgentConfig)
        $null = $iss.BeginInvoke()
    })

    $window.ShowDialog() | Out-Null
} catch {
    Write-Host "CRITICAL ERROR: $($_.Exception.Message)" -ForegroundColor Red
    $null = [Console]::ReadKey()
}
