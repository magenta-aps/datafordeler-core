@echo off

set THISDIR=%~dp0%

set VERSION=0.0.1
set COREJAR=datafordeler-core.jar

pushd %THISDIR%..\core
set COREDIR=%CD%
popd
