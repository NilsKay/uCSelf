rem call %HOME%\guistart.bat
echo "Niks CEditor developers guienv !"
set TopUi=l:\PHS
set JRE_TOP="d:\Program Files\JavaSoft\JRE\1.1"
set Print="ON";
set CLASSPATH=%TopUi%
set JAVAPAR=-nojit -DTopUi=%TopUi% -DHOME=%HOME% -DTOP=%TOP% -DPRINT=%Print% -cp %CLASSPATH%
rem echo "JAVATOP="%JAVAPAR%
jre %JAVAPAR% SOUND.CEditor
