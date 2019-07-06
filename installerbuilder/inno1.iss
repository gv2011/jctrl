[Setup]
AppName=jctrl
AppVersion=0.1
AppCopyright=Viinz 2019
AppId={{2C1D6C06-1231-4A4E-B7FA-B212D9667B31}
DefaultDirName={pf}\jctrl-setup
TimeStampsInUTC=True
AllowRootDirectory=True
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

[Files]
Source: "bin\*"; DestDir: "{app}\bin"
Source: "bin\server\*"; DestDir: "{app}\bin\server"
Source: "conf\*"; DestDir: "{app}\conf"
Source: "conf\security\*"; DestDir: "{app}\conf\security"
Source: "conf\security\policy\*"; DestDir: "{app}\conf\security\policy"
Source: "conf\security\policy\limited\*"; DestDir: "{app}\conf\security\policy\limited"
Source: "conf\security\policy\unlimited\*"; DestDir: "{app}\conf\security\policy\unlimited"
Source: "include\*"; DestDir: "{app}\include"
Source: "include\win32\*"; DestDir: "{app}\include\win32"
Source: "legal\java.base\*"; DestDir: "{app}\legal\java.base"
Source: "legal\java.compiler\*"; DestDir: "{app}\legal\java.compiler"
Source: "legal\java.datatransfer\*"; DestDir: "{app}\legal\java.datatransfer"
Source: "legal\java.desktop\*"; DestDir: "{app}\legal\java.desktop"
Source: "legal\java.logging\*"; DestDir: "{app}\legal\java.logging"
Source: "legal\java.management\*"; DestDir: "{app}\legal\java.management"
Source: "legal\java.naming\*"; DestDir: "{app}\legal\java.naming"
Source: "legal\java.prefs\*"; DestDir: "{app}\legal\java.prefs"
Source: "legal\java.security.sasl\*"; DestDir: "{app}\legal\java.security.sasl"
Source: "legal\java.sql\*"; DestDir: "{app}\legal\java.sql"
Source: "legal\java.transaction.xa\*"; DestDir: "{app}\legal\java.transaction.xa"
Source: "legal\java.xml\*"; DestDir: "{app}\legal\java.xml"
Source: "lib\*"; DestDir: "{app}\lib"
Source: "lib\security\*"; DestDir: "{app}\lib\security"
Source: "lib\server\*"; DestDir: "{app}\lib\server"
Source: "release"; DestDir: "{app}"
Source: "jctrl.ico"; DestDir: "{app}"

[Icons]
Name: "{group}\Jctrl Installer"; Filename: "{app}\bin\javaw.exe"; WorkingDir: "{app}"; IconFilename: "{app}\jctrl.ico"; Parameters: "-m com.github.gv2011.jctrl.installer/com.github.gv2011.jctrl.installer.Main"
