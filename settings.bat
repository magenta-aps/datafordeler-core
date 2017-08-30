@echo off

set THISDIR=%~dp0%

set VERSION=0.0.1
set COREJAR=datafordeler-core-1.0-SNAPSHOT.jar

pushd %THISDIR%..\core
set COREDIR=%CD%
popd
