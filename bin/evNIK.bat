rem if the jre will not start, make sure, that the memory setting for the DOS box is high enough !
rem You can change that in the 'Property' setting of the Dos box.

echo "Nik's JcSelf Enviroment!"

rem First decide to run from packed or unpacked classes:
set TopUi=c:\PHS\jcself.jar
rem set TopUi=c:\PHS

rem set JRE_TOP="c:\Program Files\JavaSoft\JRE\1.1"
echo "Parameter=" %1
echo "TopUi=" %TopUi%

rem Next you can choose to run debug - mode: (-d=debug, v5 = Verbosity (1-6)
set OPTIONS="-dv1"
rem set OPTIONS=

if "%OPTIONS%" == "" goto main

jre -nojit -DHOME=%HOME% -DOPTIONS=%OPTIONS% -cp %TopUi% SOUND.CEditor
goto end

:main
jre -nojit -DHOME=%HOME% -cp %TopUi% SOUND.CEditor

:end