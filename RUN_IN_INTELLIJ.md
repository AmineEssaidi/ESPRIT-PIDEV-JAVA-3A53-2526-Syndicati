# Running Dynamic Island Landing Page in IntelliJ IDEA

## Quick Setup Instructions

### 1. Open Project in IntelliJ
- Open IntelliJ IDEA
- Open the project folder: `ami_pidev_java`

### 2. Run the Application

#### Option A: Run MainApplication (Recommended)
1. Navigate to `src/main/java/com/wolfs/MainApplication.java`
2. Right-click on the file
3. Select "Run 'MainApplication.main()'"

#### Option B: Run Launcher
1. Navigate to `src/main/java/com/wolfs/Launcher.java`
2. Right-click on the file
3. Select "Run 'Launcher.main()'"

### 3. What You'll See
- **Window Size**: 1400x900 pixels
- **Dynamic Island Header**: 
  - Navigation tabs (Home, Services, About)
  - Circular profile icon on the right
  - Floating animation effect
- **Dynamic Island Footer**:
  - Copyright info
  - Quick links with icons
  - "Made with ❤️ by Amine"
- **Welcome Content**: Placeholder text in the center

### 4. Features to Test
- ✅ Click on navigation tabs (Home, Services, About)
- ✅ Hover over footer links
- ✅ Watch the floating animations
- ✅ Resize the window
- ✅ Check the dynamic island glass effects

### 5. Troubleshooting
If you get JavaFX module errors:
1. Make sure you have JavaFX SDK installed
2. Check that `pom.xml` has correct JavaFX dependencies
3. Try running with Maven: `mvn javafx:run`

## Project Structure
```
src/main/java/com/wolfs/
├── MainApplication.java      # Main JavaFX application
├── Launcher.java            # Simple launcher for IntelliJ
├── views/
│   └── LandingPageView.java # Main landing page view
├── components/
│   ├── DynamicHeader.java   # Dynamic island header
│   └── DynamicFooter.java   # Dynamic island footer
└── interfaces/
    └── ViewInterface.java   # View interface
```

## Next Steps
- Add more content to the landing page
- Implement navigation between different views
- Add more interactive elements
- Enhance animations and effects

