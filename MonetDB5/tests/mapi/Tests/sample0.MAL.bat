@echo off

set PATH=%CLIENTS_PREFIX%\lib\MonetDB\Tests;%PATH%

call Mlog.bat -x sample0.exe %HOST% %MAPIPORT% mal
