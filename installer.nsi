;----------------------------------------------------------------------------------------
; Steganography utility to hide messages into cover files
; Author: Samir Vaidya (mailto:syvaidya@gmail.com)
; Copyright (c) 2007-2014 Samir Vaidya
;----------------------------------------------------------------------------------------

!define JRE_DOWNLOAD_URL "http://www.java.com/getjava/"
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\win-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\win-uninstall.ico"

!include MUI2.nsh

;Macro to determine whether installation should be for All Users or Current User
!macro DETERMINE_CONTEXT
  UserInfo::getAccountType
  Pop $0
  StrCmp $0 "Admin" +3
  SetShellVarContext current
  Goto +2
  SetShellVarContext all
!macroend

;----------------------------------------------------------------------------------------
;General

  ;Name and file
  Name "${AppName}"
  OutFile "${AppDir}/../Setup-${AppName}-${AppVersion}.exe"

  SetCompressor /SOLID LZMA

  ;Default installation folder
  InstallDir "$PROGRAMFILES\${AppName}"

  ;Request application privileges for Windows Vista
  RequestExecutionLevel highest

;----------------------------------------------------------------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;----------------------------------------------------------------------------------------
;Pages

 ;!insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "LICENSE"
  Page custom CheckInstalledJRE
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

  !insertmacro DETERMINE_CONTEXT

  SetOutPath "$INSTDIR"
  File /r ${AppDir}\lib
  File ${AppDir}\openstego.bat
  File ${AppDir}\openstego.ico
  File ${AppDir}\README
  File ${AppDir}\LICENSE

  ;Write the uninstall keys for Windows
  WriteRegStr SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "DisplayName" "${AppName} v${AppVersion}"
  WriteRegStr SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegDWORD SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "NoModify" 1
  WriteRegDWORD SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}" "NoRepair" 1

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Start Menu Shortcuts"

  !insertmacro DETERMINE_CONTEXT

  CreateDirectory "$SMPROGRAMS\${AppName}"
  CreateShortCut "$SMPROGRAMS\${AppName}\Run OpenStego.lnk" "javaw.exe" "-Xmx512M -jar .\lib\openstego.jar" "$INSTDIR\openstego.ico" 0
  CreateShortCut "$SMPROGRAMS\${AppName}\Uninstall OpenStego.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0

SectionEnd

;----------------------------------------------------------------------------------------
;Language strings

  LangString JRECheckFailed ${LANG_ENGLISH} "${AppName} needs Java Runtime Environment (JRE) version ${RequiredJREVersion} or above to run properly.$\n$\nWould you like to download it now? (Please restart this installer after installing Java)."
  LangString InstallWithoutJRE ${LANG_ENGLISH} "${AppName} will install even though you do not have required version of Java installed. Please don't complain later if it doesn't work."

;----------------------------------------------------------------------------------------
;Uninstaller Section

Section "Uninstall"

  !insertmacro DETERMINE_CONTEXT

  DeleteRegKey SHELL_CONTEXT "Software\Microsoft\Windows\CurrentVersion\Uninstall\${AppName}"

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

;----------------------------------------------------------------------------------------
;Helper functions to check installed JRE version

Function CheckInstalledJRE
  Push "${RequiredJREVersion}"
  Call DetectJRE
  Exch $0  ; Get return value from stack
  StrCmp $0 "0" AbortInstall
  StrCmp $0 "-1" AbortInstall
  Goto JREAlreadyInstalled

  AbortInstall:
    MessageBox MB_ICONEXCLAMATION|MB_YESNO $(JRECheckFailed) IDNO +3
    ExecShell open "${JRE_DOWNLOAD_URL}"
    Quit
    MessageBox MB_ICONEXCLAMATION $(InstallWithoutJRE)

  JREAlreadyInstalled:
    Pop $0    ; Restore $0
    Return

FunctionEnd

; Returns: 0 - JRE not found. -1 - JRE found but too old. Otherwise - Path to JAVA EXE
; DetectJRE. Version requested is on the stack.
; Returns (on stack)  "0" on failure (java too old or not installed), otherwise path to java interpreter
; Stack value will be overwritten!
Function DetectJRE
  Exch $0  ; Get version requested
      ; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1  ; $1 = Java version string
  Push $2  ; $2 = Javahome
  Push $3  ; $3 and $4 are used for checking the major/minor version of java
  Push $4
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
  StrCmp $2 "" DetectTry2
  Goto GetJRE

  DetectTry2:
    ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
    StrCmp $1 "" NoFound
    ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
    StrCmp $2 "" NoFound

  GetJRE:
  ; $0 = version requested. $1 = version found. $2 = javaHome
    IfFileExists "$2\bin\java.exe" 0 NoFound
    StrCpy $3 $0 1      ; Get major version. Example: $1 = 1.5.0, now $3 = 1
    StrCpy $4 $1 1      ; $3 = major version requested, $4 = major version found
    IntCmp $4 $3 0 FoundOld FoundNew
    StrCpy $3 $0 1 2
    StrCpy $4 $1 1 2      ; Same as above. $3 is minor version requested, $4 is minor version installed
    IntCmp $4 $3 FoundNew FoundOld FoundNew

  NoFound:
    Push "0"
    Goto DetectJREEnd

  FoundOld:
    Push "-1"
    Goto DetectJREEnd

  FoundNew:
    Push "$2\bin\java.exe"
    Goto DetectJREEnd

  DetectJREEnd:
    ; Top of stack is return value, then r4,r3,r2,r1
    Exch  ; => r4,rv,r3,r2,r1,r0
    Pop $4  ; => rv,r3,r2,r1r,r0
    Exch  ; => r3,rv,r2,r1,r0
    Pop $3  ; => rv,r2,r1,r0
    Exch   ; => r2,rv,r1,r0
    Pop $2  ; => rv,r1,r0
    Exch  ; => r1,rv,r0
    Pop $1  ; => rv,r0
    Exch  ; => r0,rv
    Pop $0  ; => rv

FunctionEnd
