; =========================================
; RemindMe - Inno Setup Installer
; =========================================

[Setup]
AppName=RemindMe
AppVersion=1.2.1
AppPublisher=Shard
AppPublisherURL=https://www.shardpc.it/
DefaultDirName={userdocs}\Shard\RemindMe
DisableDirPage=yes
DisableProgramGroupPage=no
PrivilegesRequired=lowest
OutputBaseFilename=RemindMe_v1.2.1_Setup
SetupIconFile=src\main\resources\res\img\logo.ico
SetupLogging=yes
Compression=lzma
SolidCompression=yes
WizardStyle=modern
UninstallDisplayName=Uninstall Remind Me
UninstallDisplayIcon={app}\RemindMe.exe

; immagini wizard
WizardImageFile=src\main\resources\res\img\shard.png
WizardSmallImageFile=src\main\resources\res\img\logo.png

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

; =========================================
; FILE INSTALLATI
; =========================================
[Files]
Source: "RemindMe.exe"; DestDir: "{app}"
Source: "README.md"; DestDir: "{app}"
Source: "config.enc"; DestDir: "{app}"

Source: "jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs
Source: "src\main\resources\*"; DestDir: "{app}\src\main\resources"; Flags: recursesubdirs
Source: "docs\*"; DestDir: "{app}\docs"; Flags: recursesubdirs

; =========================================
; AVVIO AUTOMATICO (PER-UTENTE)
; =========================================
[Registry]
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; \
  ValueType: string; ValueName: "RemindMe"; \
  ValueData: """{app}\RemindMe.exe"" --background"; \
  Flags: uninsdeletevalue

; =========================================
; POST-INSTALL
; =========================================
[Run]
Filename: "{app}\RemindMe.exe"; Parameters: "--background"; Flags: nowait postinstall

; =========================================
; COLLEGAMENTI
; =========================================
[Icons]
Name: "{userdesktop}\RemindMe"; Filename: "{app}\RemindMe.exe"
Name: "{userprograms}\RemindMe\RemindMe"; Filename: "{app}\RemindMe.exe"

; =========================================
; CODICE
; =========================================
[Code]
function InitializeSetup(): Boolean;
begin
  Result := True;
end;
