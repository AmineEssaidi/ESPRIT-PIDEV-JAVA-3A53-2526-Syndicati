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

    # XAML Definition (Omitted for brevity in this scratchpad, but same as before)
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
                    <CheckBox Name="CheckBundleJava" Content="Bundle JDK 25 Runtime" Foreground="#9999AA" IsChecked="True" Margin="0,0,0,15"/>
                    <CheckBox Name="CheckBundlePython" Content="Bundle Portable Python 3.10" Foreground="#9999AA" IsChecked="True" Margin="0,0,0,15"/>
                    <CheckBox Name="CheckAIModels" Content="Include Neural Models" Foreground="#9999AA" IsChecked="True" Margin="0,0,0,15"/>
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
        $TxtLogs.Text = "[START] Initiating Bulletproof Build..."
        
        $iss = [powershell]::Create().AddScript({
            param($queue, $root)
            function Push-Log($m, $l="INFO") { $queue.Enqueue(@{Type="LOG"; Message=$m; Level=$l}) }
            function Push-Progress($p, $s) { $queue.Enqueue(@{Type="PROGRESS"; Percent=$p; Step=$s}) }

            try {
                Set-Location $root
                [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
                
                Push-Progress 10 "Compiling JAR"
                $mvn = "$root\tools\maven\bin\mvn.cmd"
                Start-Process -FilePath $mvn -ArgumentList "clean", "package", "-DskipTests" -Wait -NoNewWindow
                
                Push-Progress 40 "Native Launcher"
                Push-Log "Creating Self-Correcting C# Launcher..."
                $csharpSource = @"
using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Windows.Forms;

public class SyndicatiLauncher {
    public static void Main() {
        try {
            string root = AppDomain.CurrentDomain.BaseDirectory;
            string runtimeDir = Path.Combine(root, "runtime");
            
            if (!Directory.Exists(runtimeDir)) {
                MessageBox.Show("Critical Error: Runtime folder missing at " + runtimeDir, "Syndicati");
                return;
            }

            // SELF-CORRECTING SEARCH: Find javaw.exe anywhere in runtime
            string javaExe = Directory.GetFiles(runtimeDir, "javaw.exe", SearchOption.AllDirectories).FirstOrDefault();
            
            if (string.IsNullOrEmpty(javaExe)) {
                MessageBox.Show("Critical Error: Could not find javaw.exe inside " + runtimeDir, "Syndicati");
                return;
            }

            string jarFile = Path.Combine(root, "app", "syndicati.jar");
            if (!File.Exists(jarFile)) {
                MessageBox.Show("Critical Error: Application core missing at " + jarFile, "Syndicati");
                return;
            }

            ProcessStartInfo info = new ProcessStartInfo();
            info.FileName = javaExe;
            info.Arguments = "--enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow -Dprism.order=d3d -Dprism.dirtyopts=false -Dprism.forceGPU=true --add-opens=java.base/java.lang=ALL-UNNAMED -Xmx2G -jar \"" + jarFile + "\"";
            info.WorkingDirectory = root;
            info.UseShellExecute = false;
            info.CreateNoWindow = true;
            
            Process.Start(info);
        } catch (Exception ex) {
            MessageBox.Show("Launcher Error: " + ex.Message, "Syndicati");
        }
    }
}
"@
                Add-Type -TypeDefinition $csharpSource -ReferencedAssemblies "System.Windows.Forms" -OutputAssembly "$root\dist\Syndicati.exe" -OutputType ConsoleApplication
                
                Push-Progress 60 "Bundling Assets"
                $appDir = "$root\dist\Syndicati_Bundle"
                if (Test-Path $appDir) { Remove-Item $appDir -Recurse -Force }
                New-Item -ItemType Directory -Path "$appDir\app" -Force | Out-Null
                
                # Copy Files
                Copy-Item "$root\dist\Syndicati.exe" "$appDir\Syndicati.exe" -Force
                Copy-Item "$root\target\syndicati.jar" "$appDir\app\syndicati.jar" -Force
                
                # Smart JDK Copy: Handle the nested folder inside the tools\jdk-25
                $jdkSource = "$root\tools\jdk-25"
                $subDir = Get-ChildItem $jdkSource -Directory | Select-Object -First 1
                if ($subDir -and (Test-Path "$($subDir.FullName)\bin")) {
                    Log "Detected nested JDK folder: $($subDir.Name). Copying contents..."
                    Copy-Item "$($subDir.FullName)\*" "$appDir\runtime" -Recurse -Force
                } else {
                    Copy-Item "$jdkSource\*" "$appDir\runtime" -Recurse -Force
                }
                
                Push-Progress 85 "Packaging"
                if (Test-Path "$root\installer\app.zip") { Remove-Item "$root\installer\app.zip" -Force }
                Add-Type -AssemblyName System.IO.Compression.FileSystem
                [System.IO.Compression.ZipFile]::CreateFromDirectory($appDir, "$root\installer\app.zip")
                
                # Final Inno Compilation
                $iscc = "$root\tools\innosetup\ISCC.exe"
                $iss = "$root\installer\syndicati-installer.iss"
                Start-Process -FilePath $iscc -ArgumentList "`"$iss`"" -Wait -NoNewWindow
                
                Push-Progress 100 "Complete"
                Push-Log "SUCCESS: Bulletproof Installer Ready." "SUCCESS"
            } catch {
                Push-Log "BUILD ERROR: $($_.Exception.Message)" "ERROR"
            } finally { $queue.Enqueue(@{Type="FINISH"}) }
        }).AddArgument($global:buildQueue).AddArgument($scriptPath)
        $null = $iss.BeginInvoke()
    })

    $window.ShowDialog() | Out-Null
} catch {
    Write-Host "CRITICAL ERROR: $($_.Exception.Message)" -ForegroundColor Red
    $null = [Console]::ReadKey()
}
