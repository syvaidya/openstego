;---------------------------------------------------------
; Steganography utility to hide messages into cover files
; Author: Samir Vaidya (mailto:syvaidya@gmail.com)
; Copyright (c) 2007-2009 Samir Vaidya
;---------------------------------------------------------
!include MUI2.nsh

!macro DETERMINE_CONTEXT
  UserInfo::getAccountType
  Pop $0
  StrCmp $0 "Admin" +3
  SetShellVarContext current
  Goto +2
  SetShellVarContext all
!macroend

;--------------------------------
;General

  ;Name and file
  Name "${AppName}"
  OutFile "${AppDir}/../Setup-${AppName}-${AppVersion}.exe"

  SetCompressor /SOLID LZMA

  ;Default installation folder
  InstallDir "$PROGRAMFILES\${AppName}"

  ;Request application privileges for Windows Vista
  RequestExecutionLevel highest

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

 ;!insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "LICENSE"
 ;!insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
 ;!insertmacro MUI_PAGE_FINISH

 ;!insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
 ;!insertmacro MUI_UNPAGE_FINISH

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "${AppName} (required)" InstSec

  !insertmacro DETERMINE_CONTEXT

  SetOutPath "$INSTDIR"
  File /r ${AppDir}\doc
  File /r ${AppDir}\lib
  File ${AppDir}\openstego.bat
  File ${AppDir}\openstego.ico
  File ${AppDir}\README
  File ${AppDir}\LICENSE

  ;Write the uninstall keys for Windows
  WriteRegStr SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "DisplayName" "${AppName} v${AppVersion}"
  WriteRegStr SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "NoModify" 1
  WriteRegDWORD SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "NoRepair" 1

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

;--------------------------------
; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts" SMShort

  !insertmacro DETERMINE_CONTEXT

  CreateDirectory "$SMPROGRAMS\${AppName}"
  CreateShortCut "$SMPROGRAMS\${AppName}\Uninstall OpenStego.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\${AppName}\Run OpenStego.lnk" "javaw.exe" "-Xmx512M -jar .\lib\openstego.jar" "$INSTDIR\openstego.ico" 0

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_InstSec ${LANG_ENGLISH} "Install OpenStego application"
  LangString DESC_SMShort ${LANG_ENGLISH} "Create Start Menu shortcuts"

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${InstSec} $(DESC_InstSec)
    !insertmacro MUI_DESCRIPTION_TEXT ${SMShort} $(DESC_SMShort)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  !insertmacro DETERMINE_CONTEXT

  DeleteRegKey SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}"

  RMDir /r "$INSTDIR\doc"
  RMDir /r "$INSTDIR\lib"
  Delete "$INSTDIR\openstego.bat"
  Delete "$INSTDIR\openstego.ico"
  Delete "$INSTDIR\README"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\Uninstall.exe"

  RMDir "$INSTDIR"

  ;Remove shortcuts, if any
  RMDir /r "$SMPROGRAMS\${AppName}"

SectionEnd
