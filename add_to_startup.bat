@echo off
set "EXE_PATH=%~dp0RemindMe.exe --background"
reg add "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" /v "RemindMe" /t REG_SZ /d "%EXE_PATH%" /f
