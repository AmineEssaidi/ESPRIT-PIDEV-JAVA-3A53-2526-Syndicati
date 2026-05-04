; Syndicati - High-End Bootstrapper Engine
; This script bundles the assets, and the WPF UI handles the "Breathtaking" extraction.

#define MyAppName "Syndicati"
#define MyAppVersion "1.0.0"

[Setup]
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
UsePreviousAppDir=yes
OutputDir=..\installer-output
OutputBaseFilename=Syndicati-Setup-Core
Compression=lzma2/ultra64
SolidCompression=yes
PrivilegesRequired=admin

; HIDE EVERYTHING (Must be in [Setup] section)
WizardStyle=modern
DisableWelcomePage=yes
DisableDirPage=yes
DisableProgramGroupPage=yes
DisableReadyPage=yes
DisableFinishedPage=yes
WindowVisible=no

[UninstallDelete]
Type: filesandordirs; Name: "{app}"

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
; The main app ZIP that the WPF UI will extract
Source: "app.zip"; DestDir: "{tmp}"; Flags: ignoreversion dontcopy
; The breathtaking bootstrapper
Source: "SyndicatiInstaller.ps1"; DestDir: "{tmp}"; Flags: ignoreversion dontcopy

[Code]
function InitializeSetup: Boolean;
var
  ResultCode: Integer;
begin
  // 1. Extract the Bootstrapper and the App Archive
  ExtractTemporaryFile('SyndicatiInstaller.ps1');
  ExtractTemporaryFile('app.zip');
  
  // 2. Launch the BREATHTAKING WPF UI
  ExecAsOriginalUser('powershell.exe', '-ExecutionPolicy Bypass -WindowStyle Hidden -File "' + ExpandConstant('{tmp}\SyndicatiInstaller.ps1') + '" -ZipPath "' + ExpandConstant('{tmp}\app.zip') + '"', '', SW_SHOWNORMAL, ewWaitUntilTerminated, ResultCode);
  
  Result := False; // Exit Inno Setup because the UI is done.
end;
