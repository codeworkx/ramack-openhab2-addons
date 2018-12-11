@echo off

SETLOCAL
SET ARGC=0

FOR %%x IN (%*) DO SET /A ARGC+=1

IF %ARGC% NEQ 2 (
	echo Usage: %0 BindingIdInCamelCase Author
	exit /B 1
)

SET BindingVersion="2.4.0-SNAPSHOT" 
SET ArchetypeVersion="0.10.0-SNAPSHOT"

SET BindingIdInCamelCase=%1
SET BindingIdInLowerCase=%BindingIdInCamelCase%
SET Author=%2

call :LoCase BindingIdInLowerCase

If NOT exist "org.openhab.binding.%BindingIdInLowerCase%" (
	echo The binding with the id must exist: org.openhab.binding.%BindingIdInLowerCase%
	exit /B 1
)

call mvn -s ../archetype-settings.xml archetype:generate -N -DarchetypeGroupId=org.eclipse.smarthome.archetype -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding.test -DarchetypeVersion=%ArchetypeVersion% -DgroupId=org.openhab.binding -DartifactId=org.openhab.binding.%BindingIdInLowerCase%.test -Dpackage=org.openhab.binding.%BindingIdInLowerCase% -Dversion=%BindingVersion% -DbindingId=%BindingIdInLowerCase% -DbindingIdCamelCase=%BindingIdInCamelCase% -DvendorName=openHAB -Dnamespace=org.openhab -Dauthor="%Author%"

COPY ..\..\src\etc\about.html org.openhab.binding.%BindingIdInLowerCase%.test\

(SET BindingIdInLowerCase=)
(SET BindingIdInCamelCase=)
(SET Author=)

GOTO:EOF


:LoCase
:: Subroutine to convert a variable VALUE to all lower case.
:: The argument for this subroutine is the variable NAME.
FOR %%i IN ("A=a" "B=b" "C=c" "D=d" "E=e" "F=f" "G=g" "H=h" "I=i" "J=j" "K=k" "L=l" "M=m" "N=n" "O=o" "P=p" "Q=q" "R=r" "S=s" "T=t" "U=u" "V=v" "W=w" "X=x" "Y=y" "Z=z") DO CALL SET "%1=%%%1:%%~i%%"
GOTO:EOF

ENDLOCAL
