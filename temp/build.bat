@echo off

set CORDYS_HOME=D:\Program Files\Cordys\SSD
set JAVA_HOME=D:\Program Files\Java6\jdk1.6.0_30
set BCPSDK_HOME=D:\Work\BCPSDK
set SOURCE_HOME=D:\Work\PCT-WIP

setlocal

if not defined CORDYS_HOME goto :CORDYS_HOME_isNotDefined

if not defined JAVA_HOME goto :JAVA_HOME_isNotDefined

if not defined BCPSDK_HOME goto :BCPSDK_HOMEisNotDefined

if not defined SOURCE_HOME goto :SOURCE_HOMEisNotDefined

set ANT_HOME=%BCPSDK_HOME%/common/ant

set BUILD_HOME=%CD%

set PATH=%CORDYS_HOME%/lib;%PATH%;

set ANT_CP=%JAVA_HOME%\lib\tools.jar;%ANT_CP%;
set ANT_CP=%BCPSDK_HOME%\common\junit\junit-4.8.2.jar;%ANT_CP%
set ANT_CP=%BCPSDK_HOME%\common\ant\lib\ant.jar;%ANT_CP%;
set ANT_CP=%BCPSDK_HOME%\common\ant\lib\ant-launcher.jar;%ANT_CP%;
set ANT_CP=%CORDYS_HOME%\cordyscp.jar;%ANT_CP%;
set ANT_CP=%CORDYS_HOME%\redist\log4j-1.2.15.jar;%ANT_CP%

"%JAVA_HOME%\bin\java.exe" -Xmx256M -cp "%ANT_CP%" org.apache.tools.ant.Main "-Droot.dir=%BUILD_HOME%" "-Dsdk.dir=%BCPSDK_HOME%" %*

cd "%SOURCE_HOME%\components\cwsmodelers\
call "setjunitsettings.bat"
call "buildSingleModeler.bat"
goto :end

:CORDYS_HOME_isNotDefined
echo CORDYS_HOME environment variable is not set
goto :end

:JAVA_HOME_isNotDefined
echo JAVA_HOME environment variable is not set
goto :end

:BCPSDK_HOMEisNotDefined
echo BCPSDK_HOME environment variable is not set
goto :end

:SOURCE_HOMEisNotDefined
echo SOURCE_HOME environment variable is not set
goto :end

:end
endlocal