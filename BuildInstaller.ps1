Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

# Set security protocol for downloads
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$ProgressPreference = 'SilentlyContinue'

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# Create main form
$form = New-Object System.Windows.Forms.Form
$form.Text = "PiDev Dynamic Island - Installer Builder"
$form.Size = New-Object System.Drawing.Size(500, 350)
$form.StartPosition = "CenterScreen"
$form.FormBorderStyle = "FixedDialog"
$form.MaximizeBox = $false
$form.BackColor = [System.Drawing.Color]::FromArgb(30, 30, 35)

# Title label
$titleLabel = New-Object System.Windows.Forms.Label
$titleLabel.Text = "PiDev Dynamic Island"
$titleLabel.Font = New-Object System.Drawing.Font("Segoe UI", 18, [System.Drawing.FontStyle]::Bold)
$titleLabel.ForeColor = [System.Drawing.Color]::White
$titleLabel.AutoSize = $true
$titleLabel.Location = New-Object System.Drawing.Point(120, 20)
$form.Controls.Add($titleLabel)

# Subtitle
$subtitleLabel = New-Object System.Windows.Forms.Label
$subtitleLabel.Text = "Professional Installer Builder"
$subtitleLabel.Font = New-Object System.Drawing.Font("Segoe UI", 10)
$subtitleLabel.ForeColor = [System.Drawing.Color]::FromArgb(150, 150, 150)
$subtitleLabel.AutoSize = $true
$subtitleLabel.Location = New-Object System.Drawing.Point(160, 55)
$form.Controls.Add($subtitleLabel)

# Status label
$statusLabel = New-Object System.Windows.Forms.Label
$statusLabel.Text = "Ready to build installer"
$statusLabel.Font = New-Object System.Drawing.Font("Segoe UI", 9)
$statusLabel.ForeColor = [System.Drawing.Color]::FromArgb(100, 200, 100)
$statusLabel.AutoSize = $true
$statusLabel.Location = New-Object System.Drawing.Point(30, 100)
$form.Controls.Add($statusLabel)

# Progress bar
$progressBar = New-Object System.Windows.Forms.ProgressBar
$progressBar.Location = New-Object System.Drawing.Point(30, 130)
$progressBar.Size = New-Object System.Drawing.Size(420, 25)
$progressBar.Style = "Continuous"
$form.Controls.Add($progressBar)

# Details textbox
$detailsBox = New-Object System.Windows.Forms.TextBox
$detailsBox.Location = New-Object System.Drawing.Point(30, 170)
$detailsBox.Size = New-Object System.Drawing.Size(420, 80)
$detailsBox.Multiline = $true
$detailsBox.ScrollBars = "Vertical"
$detailsBox.ReadOnly = $true
$detailsBox.BackColor = [System.Drawing.Color]::FromArgb(20, 20, 25)
$detailsBox.ForeColor = [System.Drawing.Color]::FromArgb(200, 200, 200)
$detailsBox.Font = New-Object System.Drawing.Font("Consolas", 8)
$form.Controls.Add($detailsBox)

# Build button
$buildButton = New-Object System.Windows.Forms.Button
$buildButton.Text = "Build Installer"
$buildButton.Location = New-Object System.Drawing.Point(150, 265)
$buildButton.Size = New-Object System.Drawing.Size(180, 35)
$buildButton.BackColor = [System.Drawing.Color]::FromArgb(220, 50, 50)
$buildButton.ForeColor = [System.Drawing.Color]::White
$buildButton.FlatStyle = "Flat"
$buildButton.Font = New-Object System.Drawing.Font("Segoe UI", 10, [System.Drawing.FontStyle]::Bold)
$form.Controls.Add($buildButton)

function Log($message) {
    $detailsBox.AppendText("$message`r`n")
    $detailsBox.SelectionStart = $detailsBox.Text.Length
    $detailsBox.ScrollToCaret()
    [System.Windows.Forms.Application]::DoEvents()
}

function UpdateStatus($message, $progress) {
    $statusLabel.Text = $message
    $progressBar.Value = $progress
    [System.Windows.Forms.Application]::DoEvents()
}

function Build-Installer {
    $buildButton.Enabled = $false
    $detailsBox.Clear()
    
    try {
        # Create directories
        if (!(Test-Path "tools")) { New-Item -ItemType Directory -Path "tools" -Force | Out-Null }
        if (!(Test-Path "installer")) { New-Item -ItemType Directory -Path "installer" -Force | Out-Null }
        if (!(Test-Path "dist\app")) { New-Item -ItemType Directory -Path "dist\app" -Force | Out-Null }
        if (!(Test-Path "installer-output")) { New-Item -ItemType Directory -Path "installer-output" -Force | Out-Null }
        
        # Step 1: Java
        UpdateStatus "Checking Java..." 5
        $javaDir = "$scriptPath\tools\jdk-21"
        $javaExe = "$javaDir\bin\java.exe"
        
        if (!(Test-Path $javaExe)) {
            Log "Downloading Java 21... (this may take a few minutes)"
            $javaUrl = "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jdk_x64_windows_hotspot_21.0.5_11.zip"
            Invoke-WebRequest -Uri $javaUrl -OutFile "$scriptPath\tools\jdk.zip" -UseBasicParsing
            
            Log "Extracting Java..."
            Expand-Archive -Path "$scriptPath\tools\jdk.zip" -DestinationPath "$scriptPath\tools\jdk-temp" -Force
            
            Get-ChildItem "$scriptPath\tools\jdk-temp" -Directory | ForEach-Object {
                if (Test-Path $javaDir) { Remove-Item $javaDir -Recurse -Force }
                Move-Item $_.FullName $javaDir
            }
            
            Remove-Item "$scriptPath\tools\jdk-temp" -Recurse -Force -ErrorAction SilentlyContinue
            Remove-Item "$scriptPath\tools\jdk.zip" -Force -ErrorAction SilentlyContinue
        }
        Log "[OK] Java ready"
        UpdateStatus "Java ready" 15
        
        $env:JAVA_HOME = $javaDir
        $env:PATH = "$javaDir\bin;$env:PATH"
        
        # Step 2: Maven
        UpdateStatus "Checking Maven..." 20
        $mavenDir = "$scriptPath\tools\maven"
        $mvn = "$mavenDir\bin\mvn.cmd"
        
        if (!(Test-Path $mvn)) {
            Log "Downloading Maven..."
            $mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
            Invoke-WebRequest -Uri $mavenUrl -OutFile "$scriptPath\tools\maven.zip" -UseBasicParsing
            
            Log "Extracting Maven..."
            Expand-Archive -Path "$scriptPath\tools\maven.zip" -DestinationPath "$scriptPath\tools\maven-temp" -Force
            
            Get-ChildItem "$scriptPath\tools\maven-temp" -Directory | ForEach-Object {
                if (Test-Path $mavenDir) { Remove-Item $mavenDir -Recurse -Force }
                Move-Item $_.FullName $mavenDir
            }
            
            Remove-Item "$scriptPath\tools\maven-temp" -Recurse -Force -ErrorAction SilentlyContinue
            Remove-Item "$scriptPath\tools\maven.zip" -Force -ErrorAction SilentlyContinue
        }
        Log "[OK] Maven ready"
        UpdateStatus "Maven ready" 30
        
        # Step 3: Build JAR
        UpdateStatus "Building application..." 35
        Log "Building application with Maven..."
        
        $process = Start-Process -FilePath $mvn -ArgumentList "clean", "package", "-DskipTests", "-q" -Wait -NoNewWindow -PassThru
        
        if ($process.ExitCode -ne 0) {
            throw "Maven build failed!"
        }
        Log "[OK] Application built"
        UpdateStatus "Application built" 50
        
        # Step 4: Prepare distribution
        UpdateStatus "Preparing distribution..." 55
        Log "Preparing app for distribution..."
        
        if (Test-Path "dist") { Remove-Item "dist" -Recurse -Force }
        New-Item -ItemType Directory -Path "dist\app" -Force | Out-Null
        New-Item -ItemType Directory -Path "dist\jre" -Force | Out-Null
        
        # Copy JAR
        Copy-Item "target\wolfs_pidev_3a6-1.0-SNAPSHOT.jar" "dist\app\PiDev.jar" -Force
        Log "[OK] JAR copied"
        
        # Copy JRE (minimal)
        Log "Bundling JRE... (this may take a moment)"
        UpdateStatus "Bundling JRE..." 60
        Copy-Item "$javaDir\*" "dist\jre\" -Recurse -Force
        
        # Create launcher script
        $launcherContent = @"
@echo off
cd /d "%~dp0"
start "" "jre\bin\javaw.exe" -jar "app\PiDev.jar"
"@
        Set-Content -Path "dist\PiDev.bat" -Value $launcherContent
        
        Log "[OK] Distribution prepared"
        UpdateStatus "Distribution prepared" 70
        
        # Step 5: Inno Setup
        UpdateStatus "Checking Inno Setup..." 75
        $innoDir = "$scriptPath\tools\innosetup"
        $iscc = "$innoDir\ISCC.exe"
        
        if (!(Test-Path $iscc)) {
            Log "Downloading Inno Setup..."
            $innoInstaller = "$scriptPath\tools\innosetup-installer.exe"
            Invoke-WebRequest -Uri "https://files.jrsoftware.org/is/6/innosetup-6.2.2.exe" -OutFile $innoInstaller -UseBasicParsing
            
            Log "Installing Inno Setup (please wait)..."
            # Run installer with explicit path
            $innoArgs = "/VERYSILENT /SUPPRESSMSGBOXES /NORESTART /DIR=`"$innoDir`""
            $process = Start-Process -FilePath $innoInstaller -ArgumentList $innoArgs -Wait -PassThru
            
            # Wait for installation to complete
            Start-Sleep -Seconds 10
            
            # Check if installation succeeded
            if (!(Test-Path $iscc)) {
                # Try system-wide location
                $systemInno = "${env:ProgramFiles(x86)}\Inno Setup 6\ISCC.exe"
                if (Test-Path $systemInno) {
                    $iscc = $systemInno
                    Log "Using system Inno Setup installation"
                } else {
                    throw "Inno Setup installation failed. Please install manually from https://jrsoftware.org/isdl.php"
                }
            }
            
            Remove-Item $innoInstaller -Force -ErrorAction SilentlyContinue
        }
        Log "[OK] Inno Setup ready"
        UpdateStatus "Inno Setup ready" 85
        
        # Step 6: Create icon if needed
        if (!(Test-Path "installer\app-icon.ico")) {
            Log "Note: No custom icon found. Using default."
        }
        
        # Step 7: Build installer
        UpdateStatus "Building installer..." 90
        Log "Creating installer..."
        
        # Build full path to ISS file
        $issFile = "$scriptPath\installer\pidev-installer.iss"
        Log "Using: $iscc"
        Log "Script: $issFile"
        
        $process = Start-Process -FilePath $iscc -ArgumentList "`"$issFile`"" -Wait -NoNewWindow -PassThru -RedirectStandardOutput "$scriptPath\inno-out.txt" -RedirectStandardError "$scriptPath\inno-err.txt"
        
        if (Test-Path "$scriptPath\inno-err.txt") {
            $errContent = Get-Content "$scriptPath\inno-err.txt" -Raw
            if ($errContent) { Log "Inno Error: $errContent" }
        }
        
        if ($process.ExitCode -ne 0) {
            if (Test-Path "$scriptPath\inno-out.txt") {
                $outContent = Get-Content "$scriptPath\inno-out.txt" -Raw
                if ($outContent) { Log $outContent }
            }
            throw "Installer creation failed with exit code $($process.ExitCode)!"
        }
        
        Remove-Item "$scriptPath\inno-out.txt" -Force -ErrorAction SilentlyContinue
        Remove-Item "$scriptPath\inno-err.txt" -Force -ErrorAction SilentlyContinue
        
        UpdateStatus "Installer created successfully!" 100
        Log ""
        Log "========================================="
        Log "SUCCESS! Installer created!"
        Log "========================================="
        Log "Location: installer-output\PiDev-Setup-1.0.0.exe"
        
        $statusLabel.ForeColor = [System.Drawing.Color]::FromArgb(100, 255, 100)
        
        # Open output folder
        Start-Process "explorer.exe" -ArgumentList "installer-output"
        
    } catch {
        $statusLabel.Text = "Error: $($_.Exception.Message)"
        $statusLabel.ForeColor = [System.Drawing.Color]::FromArgb(255, 100, 100)
        Log "ERROR: $($_.Exception.Message)"
    }
    
    $buildButton.Enabled = $true
    $buildButton.Text = "Build Again"
}

$buildButton.Add_Click({ Build-Installer })

$form.ShowDialog()
