@echo off

set DIR=%~dp0%
set RUN_ARGS=""
set RUN_JAR=%DIR%target\dafo-dataprovider.war

rem Load global settings
call "%DIR%settings.bat"

rem Add local settings, if present
if exist "%DIR%local_settings.bat" (
    call "%DIR%local_settings.bat"
)

rem If a local_settings.properties file exists, make sure it's loaded after the application.properties
if exist "%DIR%local_settings.properties" (
    set RUN_ARGS=%RUN_ARGS% --spring.config.location="classpath:/application.properties,file:%DIR%local_settings.properties"
)

echo "Build core"
pushd %COREDIR%
   call mvnw.cmd clean install -P no-repackage
popd


echo "Build cpr"
pushd %DIR%..\plugins\CprPlugin
    call mvnw.cmd clean install
popd

echo "Build cvr"
pushd %DIR%..\plugins\CvrPlugin
    call mvnw.cmd clean install
popd

echo "Build gladdrreg"
pushd %DIR%..\plugins\GladdrregPlugin
    call mvnw.cmd clean install
popd

pushd %COREDIR%
call mvnw.cmd clean package
popd


rem Copy compiled WAR so running will not hold a lock on the compiled file destination
if not exist "%RUN_JAR%" (
    copy "%DIR%target\%COREJAR%" "%RUN_JAR%"
)

rem Run the JAR file
call "%JAVA_HOME%\bin\java.exe" -jar "%RUN_JAR%" %RUN_ARGS%
