;----------------------------------------------------------------------------------------
; Steganography utility to hide messages into cover files
; Copyright (c) Samir Vaidya (mailto:syvaidya@gmail.com)
;----------------------------------------------------------------------------------------

Unicode true

!define MULTIUSER_EXECUTIONLEVEL Highest
!define MULTIUSER_INSTALLMODE_INSTDIR "${AppName}"
!define MULTIUSER_INSTALLMODE_FUNCTION onMultiUserModeChanged
!define MULTIUSER_INSTALLMODE_COMMANDLINE
!define MULTIUSER_INSTALLMODEPAGE_SHOWUSERNAME
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\win-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\win-uninstall.ico"
!define MULTIUSER_MUI
!include MultiUser.nsh
!include MUI2.nsh
!include "LogicLib.nsh"
!include "FileFunc.nsh"

;----------------------------------------------------------------------------------------
;General

  Name "${AppName}"
  OutFile "${DistDir}/Setup-${AppName}-${AppVersion}.exe"

;----------------------------------------------------------------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;----------------------------------------------------------------------------------------
;Pages

 ;!insertmacro MUI_PAGE_WELCOME
  !insertmacro MULTIUSER_PAGE_INSTALLMODE
  !insertmacro MUI_PAGE_LICENSE "LICENSE"
 ;!insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
 ;!insertmacro MUI_PAGE_FINISH

 ;!insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
 ;!insertmacro MUI_UNPAGE_FINISH

;----------------------------------------------------------------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English"

;----------------------------------------------------------------------------------------
;Installer Sections

Section "${AppName}"

  SetOutPath "$INSTDIR"

  File ${AppDir}\openstego.bat
  File ${AppDir}\openstego.ico
  File ${AppDir}\README
  File ${AppDir}\LICENSE
  File /r ${AppDir}\lib

  ;Write the uninstall keys for Windows
  WriteRegStr SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "DisplayName" "${AppName}"
  WriteRegStr SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "DisplayVersion" "${AppVersion}"
  WriteRegStr SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "DisplayIcon" "$INSTDIR\openstego.ico"
  WriteRegStr SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegDWORD SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "NoModify" 1
  WriteRegDWORD SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "NoRepair" 1

  ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
  IntFmt $0 "0x%08X" $0
  WriteRegDWORD SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "EstimatedSize" "$0"

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Start Menu Shortcuts"

  CreateShortCut "$SMPROGRAMS\${AppName}.lnk" "$INSTDIR\openstego.bat" "" "$INSTDIR\openstego.ico" 0

SectionEnd

;----------------------------------------------------------------------------------------
;Uninstaller Section

Section "Uninstall"

  DeleteRegKey SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}"

  Delete "$INSTDIR\openstego.bat"
  Delete "$INSTDIR\openstego.ico"
  Delete "$INSTDIR\README"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir /r "$INSTDIR\lib"

  RMDir "$INSTDIR"

  ;Remove shortcuts, if any
  Delete "$SMPROGRAMS\${AppName}.lnk"

SectionEnd

Function .onInit
  !insertmacro MULTIUSER_INIT
FunctionEnd

Function un.onInit
  !insertmacro MULTIUSER_UNINIT
FunctionEnd

Function onMultiUserModeChanged
  ${If} $MultiUser.InstallMode == "CurrentUser"
    StrCpy $InstDir "$LocalAppdata\Programs\${MULTIUSER_INSTALLMODE_INSTDIR}"
  ${EndIf}
FunctionEnd
