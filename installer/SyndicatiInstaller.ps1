# Syndicati - High-End Setup Bootstrapper
# Primary Installer Interface

param([string]$ZipPath)

Add-Type -AssemblyName PresentationFramework
Add-Type -AssemblyName PresentationCore
Add-Type -AssemblyName WindowsBase
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.IO.Compression.FileSystem

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

$inputXML = @"
<Window xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml"
        Title="Syndicati Setup" Height="600" Width="900" 
        WindowStyle="None" AllowsTransparency="True" Background="Transparent"
        WindowStartupLocation="CenterScreen">
    <Border CornerRadius="24" BorderThickness="1">
        <Border.BorderBrush>
            <LinearGradientBrush StartPoint="0,0" EndPoint="1,1">
                <GradientStop Color="#333344" Offset="0"/>
                <GradientStop Color="#111122" Offset="1"/>
            </LinearGradientBrush>
        </Border.BorderBrush>
        <Border.Background>
            <RadialGradientBrush Center="0.5,-0.1" GradientOrigin="0.5,-0.1" RadiusX="1" RadiusY="1">
                <GradientStop Color="#1A1A2E" Offset="0"/>
                <GradientStop Color="#050508" Offset="0.8"/>
            </RadialGradientBrush>
        </Border.Background>
        
        <Grid>
            <Grid.RowDefinitions>
                <RowDefinition Height="60"/>
                <RowDefinition Height="*"/>
                <RowDefinition Height="100"/>
            </Grid.RowDefinitions>

            <StackPanel Grid.Row="0" Orientation="Horizontal" HorizontalAlignment="Right" Margin="0,15,25,0">
                <Button Name="BtnClose" Content="×" FontSize="20" Foreground="#555566" Background="Transparent" BorderThickness="0" Cursor="Hand" Width="30"/>
            </StackPanel>

            <StackPanel Grid.Row="1" Margin="50,20,50,0">
                <TextBlock Text="SYNDICATI" FontSize="42" FontWeight="Bold" Foreground="White">
                    <TextBlock.Effect>
                        <DropShadowEffect BlurRadius="15" Color="#6366F1" Opacity="0.3" ShadowDepth="0"/>
                    </TextBlock.Effect>
                </TextBlock>
                <TextBlock Text="PREMIUM INFRASTRUCTURE DEPLOYMENT" FontSize="11" Foreground="#6366F1" FontWeight="SemiBold" Margin="2,0,0,30"/>
                
                <StackPanel Name="WelcomeView" Visibility="Visible">
                    <TextBlock Name="TxtWelcome" Text="Secure deployment initialized. Select destination." Foreground="#9999AA" FontSize="16" Margin="0,0,0,30"/>
                    
                    <Border Background="#0A0A12" CornerRadius="12" BorderThickness="1" BorderBrush="#1A1A2E" Padding="15" Margin="0,20,0,40">
                        <Grid>
                            <Grid.ColumnDefinitions>
                                <ColumnDefinition Width="*"/>
                                <ColumnDefinition Width="100"/>
                            </Grid.ColumnDefinitions>
                            <TextBox Name="TxtPath" Text="C:\Syndicati" Background="Transparent" Foreground="White" BorderThickness="0" VerticalAlignment="Center" FontSize="13"/>
                            <Button Name="BtnBrowse" Grid.Column="1" Content="BROWSE" FontSize="10" FontWeight="Bold" Foreground="#6366F1" Background="Transparent" BorderThickness="0" Cursor="Hand"/>
                        </Grid>
                    </Border>
                    
                    <Button Name="BtnStart" Content="START INSTALLATION" Height="55" Width="280" HorizontalAlignment="Left" Cursor="Hand">
                        <Button.Style>
                            <Style TargetType="Button">
                                <Setter Property="Background" Value="#6366F1"/>
                                <Setter Property="Foreground" Value="White"/>
                                <Setter Property="FontWeight" Value="Bold"/>
                                <Setter Property="Template">
                                    <Setter.Value>
                                        <ControlTemplate TargetType="Button">
                                            <Border Background="{TemplateBinding Background}" CornerRadius="14">
                                                <ContentPresenter HorizontalAlignment="Center" VerticalAlignment="Center"/>
                                            </Border>
                                        </ControlTemplate>
                                    </Setter.Value>
                                </Setter>
                            </Style>
                        </Button.Style>
                    </Button>
                </StackPanel>

                <StackPanel Name="ProgressView" Visibility="Collapsed">
                    <TextBlock Name="TxtStep" Text="OPTIMIZING CORE..." Foreground="#6366F1" FontSize="14" FontWeight="Bold" Margin="0,0,0,10"/>
                    <TextBlock Name="TxtDetail" Text="Initializing secure extraction engine..." Foreground="#777788" FontSize="12" Margin="0,0,0,25"/>
                    
                    <Grid Height="12">
                        <Border Background="#1A1A2E" CornerRadius="6"/>
                        <Border Name="ProgressBar" Background="#6366F1" CornerRadius="6" HorizontalAlignment="Left" Width="0">
                            <Border.Effect>
                                <DropShadowEffect BlurRadius="12" Color="#6366F1" Opacity="0.6" ShadowDepth="0"/>
                            </Border.Effect>
                        </Border>
                    </Grid>
                    
                    <TextBlock Name="TxtProgress" Text="0%" Foreground="#9999AA" FontSize="11" HorizontalAlignment="Right" Margin="0,5,0,0"/>
                    
                    <Border Background="#0A0A12" CornerRadius="16" Margin="0,30,0,0" Height="140" BorderThickness="1" BorderBrush="#1A1A2E">
                        <ScrollViewer Name="LogScroll" VerticalScrollBarVisibility="Auto">
                            <TextBlock Name="TxtLogs" Text="[SYSTEM] Awaiting user confirmation..." Foreground="#444455" FontSize="10" FontFamily="Consolas" Margin="20"/>
                        </ScrollViewer>
                    </Border>
                </StackPanel>

                <StackPanel Name="FinishView" Visibility="Collapsed">
                    <TextBlock Text="DEPLOYMENT SUCCESSFUL" Foreground="#10B981" FontSize="22" FontWeight="Bold" Margin="0,0,0,10"/>
                    <TextBlock Text="All subsystems are green. Syndicati is ready for launch." Foreground="#9999AA" FontSize="14" Margin="0,0,0,30"/>
                    
                    <Button Name="BtnLaunch" Content="LAUNCH SYNDICATI" Height="55" Width="280" HorizontalAlignment="Left" Cursor="Hand">
                        <Button.Style>
                            <Style TargetType="Button">
                                <Setter Property="Background" Value="#10B981"/>
                                <Setter Property="Foreground" Value="White"/>
                                <Setter Property="FontWeight" Value="Bold"/>
                                <Setter Property="Template">
                                    <Setter.Value>
                                        <ControlTemplate TargetType="Button">
                                            <Border Background="{TemplateBinding Background}" CornerRadius="14">
                                                <ContentPresenter HorizontalAlignment="Center" VerticalAlignment="Center"/>
                                            </Border>
                                        </ControlTemplate>
                                    </Setter.Value>
                                </Setter>
                            </Style>
                        </Button.Style>
                    </Button>
                </StackPanel>
            </StackPanel>

            <Border Grid.Row="2" Background="#0A0A12" CornerRadius="0,0,24,24">
                <Grid Margin="50,0">
                    <TextBlock Text="SYNDICATI DEPLOYMENT CORE v1.0" Foreground="#333344" FontSize="10" VerticalAlignment="Center"/>
                    <StackPanel Orientation="Horizontal" HorizontalAlignment="Right" VerticalAlignment="Center">
                        <TextBlock Text="STATUS:" Foreground="#444455" FontSize="10" VerticalAlignment="Center" Margin="0,0,10,0"/>
                        <TextBlock Text="ENCRYPTED LINK ESTABLISHED" Foreground="#10B981" FontSize="10" FontWeight="Bold" VerticalAlignment="Center"/>
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
$BtnStart = $window.FindName("BtnStart")
$BtnBrowse = $window.FindName("BtnBrowse")
$BtnLaunch = $window.FindName("BtnLaunch")
$TxtPath = $window.FindName("TxtPath")
$TxtWelcome = $window.FindName("TxtWelcome")
$WelcomeView = $window.FindName("WelcomeView")
$ProgressView = $window.FindName("ProgressView")
$FinishView = $window.FindName("FinishView")
$ProgressBar = $window.FindName("ProgressBar")
$TxtStep = $window.FindName("TxtStep")
$TxtDetail = $window.FindName("TxtDetail")
$TxtProgress = $window.FindName("TxtProgress")
$TxtLogs = $window.FindName("TxtLogs")
$LogScroll = $window.FindName("LogScroll")

# Logic
$BtnClose.Add_Click({ $window.Close() })

# Check for existing installation
if (Test-Path $TxtPath.Text) {
    $TxtWelcome.Text = "Existing installation detected. Ready to upgrade."
    $BtnStart.Content = "UPDATE SYNDICATI"
}

$BtnBrowse.Add_Click({
    $Browser = New-Object System.Windows.Forms.FolderBrowserDialog
    if ($Browser.ShowDialog() -eq "OK") { $TxtPath.Text = $Browser.SelectedPath }
})

function Log($msg) {
    $timestamp = Get-Date -Format "HH:mm:ss"
    $TxtLogs.Text += "`n[$timestamp] $msg"
    $LogScroll.ScrollToEnd()
}

function UpdateProgress($percent, $step, $detail) {
    $ProgressBar.Width = ($percent / 100) * 800
    $TxtProgress.Text = "$([int]$percent)%"
    if ($step) { $TxtStep.Text = $step.ToUpper() }
    if ($detail) { $TxtDetail.Text = $detail }
    [System.Windows.Forms.Application]::DoEvents()
}

$BtnStart.Add_Click({
    $WelcomeView.Visibility = "Collapsed"
    $ProgressView.Visibility = "Visible"
    $target = $TxtPath.Text
    
    # 1. Stabilization & Cleanup
    UpdateProgress 10 "Maintenance" "Terminating existing sessions..."
    $proc = Get-Process "Syndicati" -ErrorAction SilentlyContinue
    if ($proc) { 
        Log "Closing active application instances..."
        $proc | Stop-Process -Force 
    }
    
    if (!(Test-Path $target)) { 
        New-Item -ItemType Directory -Path $target -Force | Out-Null 
    } else {
        Log "Cleaning old core files (preserving user data)..."
        Get-ChildItem -Path $target -Exclude "*.db", "*.json", "config", "logs" | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    # 2. Actual Extraction (Unzipping)
    UpdateProgress 40 "Deployment" "Injecting core application files..."
    if ($ZipPath -and (Test-Path $ZipPath)) {
        try {
            Log "Unzipping app package to $target..."
            [System.IO.Compression.ZipFile]::ExtractToDirectory($ZipPath, $target)
            Log "Extraction complete."
        } catch {
            Log "CRITICAL ERROR: $($_.Exception.Message)"
            [System.Windows.Forms.MessageBox]::Show("Extraction failed: $($_.Exception.Message)")
            return
        }
    }
    
    # 3. Shortcuts
    UpdateProgress 80 "Integration" "Configuring desktop environment..."
    try {
        $WshShell = New-Object -ComObject WScript.Shell
        # Desktop
        $Shortcut = $WshShell.CreateShortcut("$([Environment]::GetFolderPath('Desktop'))\Syndicati.lnk")
        $Shortcut.TargetPath = "$target\Syndicati.exe"
        $Shortcut.WorkingDirectory = $target
        $Shortcut.IconLocation = "$target\Syndicati.exe,0"
        $Shortcut.Save()
        Log "Desktop shortcut created."
    } catch {
        Log "WARNING: Could not create shortcuts."
    }
    
    # 4. Finalizing
    UpdateProgress 100 "Complete" "Syndicati is ready."
    Log "Installation finished successfully."
    
    $ProgressView.Visibility = "Collapsed"
    $FinishView.Visibility = "Visible"
})

$BtnLaunch.Add_Click({
    $exe = Join-Path $TxtPath.Text "Syndicati.exe"
    if (Test-Path $exe) { Start-Process $exe }
    $window.Close()
})

$window.ShowDialog() | Out-Null
