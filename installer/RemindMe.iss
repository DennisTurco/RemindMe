; Inno Setup script for RemindMe
; Prerequisite: run "npm run build:electron" inside app/
;               (output in app\release\win-unpacked\)
;
; To compile:
;   - Open this file with Inno Setup Compiler
;   - Press Ctrl+F9 (Build) or use the Build > Compile menu
;   - The installer is created in installer\Output\RemindMe_Setup_....exe
;
; Note: RemindMe does not currently register itself to start with Windows
; (unlike some other apps that use the "auto-launch" npm package), so this
; installer does not create an HKCU...\Run entry either. If that changes,
; remember to also remove the entry on uninstall.

#define AppName      "RemindMe"
#define AppVersion   "1.2.3"
#define AppPublisher "Shard"
#define AppURL       "https://github.com/DennisTurco/RemindMe"
#define AppExeName   "RemindMe.exe"
#ifndef SourceDir
  #define SourceDir  "..\app\release\win-unpacked"
#endif

[Setup]
AppId={{E2C1E98A-2B38-45A8-9D98-8DB08E7094AF}
AppName={#AppName}
AppVersion={#AppVersion}
AppVerName={#AppName} {#AppVersion}
AppPublisher={#AppPublisher}
AppPublisherURL={#AppURL}
AppSupportURL={#AppURL}
AppUpdatesURL={#AppURL}
DefaultDirName={autopf}\{#AppName}
DefaultGroupName={#AppName}
AllowNoIcons=yes
; Installer output folder
OutputDir=Output
OutputBaseFilename={#AppName}_Setup_{#AppVersion}
SetupIconFile=..\app\build\icon.ico
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
; 64-bit only
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
; Requires Windows 10+
MinVersion=10.0
; No UAC needed to install under AppData (per-user install)
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
UninstallDisplayIcon={app}\{#AppExeName}
UninstallDisplayName={#AppName} {#AppVersion}

[Languages]
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; \
  Description: "{cm:CreateDesktopIcon}"; \
  GroupDescription: "{cm:AdditionalIcons}"; \
  Flags: unchecked

[Files]
; The whole packaged Electron app (electron-builder, target "dir"),
; including the bundled JRE and the Java backend JAR under resources\
Source: "{#SourceDir}\*"; \
  DestDir: "{app}"; \
  Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
; Start Menu
Name: "{group}\{#AppName}";           Filename: "{app}\{#AppExeName}"
Name: "{group}\Uninstall {#AppName}"; Filename: "{uninstallexe}"
; Desktop (optional)
Name: "{autodesktop}\{#AppName}"; \
  Filename: "{app}\{#AppExeName}"; \
  Tasks: desktopicon

[Run]
; Offers to launch the app after installation
Filename: "{app}\{#AppExeName}"; \
  Description: "{cm:LaunchProgram,{#StringChange(AppName, '&', '&&')}}"; \
  Flags: nowait postinstall skipifsilent

[UninstallRun]
; Closes the app before uninstalling (if running). "/T" also terminates the
; Java backend process tree spawned as RemindMe.exe's child (the backend
; runs as a plain "java.exe", so it can't be targeted by name alone without
; risking killing an unrelated Java process elsewhere on the system).
Filename: "taskkill.exe"; \
  Parameters: "/f /t /im {#AppExeName}"; \
  Flags: runhidden waituntilterminated; \
  RunOnceId: "KillApp"
