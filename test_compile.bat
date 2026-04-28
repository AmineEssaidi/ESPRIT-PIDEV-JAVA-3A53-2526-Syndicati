@echo off
cd /d "c:\Users\amine\OneDrive\Desktop\ESPRIT-PIDEV-JAVA-3A53-2526-Syndicati"
set JAVA_HOME=c:\Users\amine\OneDrive\Desktop\ESPRIT-PIDEV-JAVA-3A53-2526-Syndicati\tools\jdk-25
set PATH=c:\Users\amine\OneDrive\Desktop\ESPRIT-PIDEV-JAVA-3A53-2526-Syndicati\tools\jdk-25\bin;c:\Users\amine\OneDrive\Desktop\ESPRIT-PIDEV-JAVA-3A53-2526-Syndicati\tools\maven\bin;%PATH%
echo JAVA_HOME=%JAVA_HOME%
echo PATH=%PATH%
java -version
mvn -q -DskipTests compile
