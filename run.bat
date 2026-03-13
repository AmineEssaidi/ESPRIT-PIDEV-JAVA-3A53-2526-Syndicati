@echo off
echo Starting PiDev Liquid Glass Desktop Application...
echo.

REM Set JavaFX module path (adjust path as needed)
set JAVAFX_PATH=C:\Program Files\Java\javafx-20.0.2\lib

REM Compile the application
echo Compiling application...
javac -cp "target/classes;%JAVAFX_PATH%\*" -d target/classes src/main/java/com/wolfs/*.java src/main/java/com/wolfs/controllers/*.java src/main/java/com/wolfs/views/*.java src/main/java/com/wolfs/components/*.java src/main/java/com/wolfs/utils/*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.

REM Run the application
echo Starting application...
java -cp "target/classes;%JAVAFX_PATH%\*" --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml com.wolfs.PiDevApplication

pause
