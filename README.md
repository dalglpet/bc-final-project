# 🌡️ Gas & Temperature Data Analysis (Java)

![Java](https://img.shields.io/badge/Java-17-orange?logo=java&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)

A Java-based application for analyzing and visualizing datasets of gas emissions and temperature records. The program loads CSV files, computes statistics, and provides insights into trends through a simple console workflow.

---

## ✨ Features
• Load gas emission and temperature data from CSV files  
• Compute descriptive statistics (averages, min/max, correlations)  
• Run multiple analysis sessions with reusable dataset loaders  
• Display results in text and visualizations (powered by XChart)  

---

## 🛠️ Technologies
• Java (OOP and modular design)  
• CSV dataset parsing  
• Custom `Stats` class for descriptive analysis  
• `Visualization` class for charts using **XChart**  

---

## 🚀 How to Run

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/gas-temp-analysis.git
cd gas-temp-analysis
```

### 2. Verify You Have Java Installed
This project requires **Java 17 or later**.  
Check your version:
```bash
java -version
```
If you don’t have Java installed, download it from [Adoptium](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/).

### 3. Download the XChart Library
This project uses **XChart** for visualization.  
Download the JAR from: [https://knowm.org/open-source/xchart/](https://knowm.org/open-source/xchart/)  

Place the JAR file (e.g., `xchart-X.X.X.jar`) in the same directory as your `.java` files, or add it to your classpath.

### 4. Compile the Project
From the root of the project directory, compile all `.java` files with XChart included in the classpath:
```bash
javac -cp .:xchart-X.X.X.jar *.java
```

### 5. Prepare the Datasets
Make sure the included CSV files are present in the same directory as your `.java` files:  
• `gases.csv` – gas dataset  
• `temps.csv` – temperature dataset  

### 6. Run the Program
Run with the XChart JAR in the classpath:
```bash
java -cp .:xchart-X.X.X.jar Main
```

### 7. Example Workflow
```
=== Gas & Temperature Data Analysis ===
1) Load gas dataset
2) Load temperature dataset
3) Compute statistics
4) Visualize results
0) Exit
```
1. Select **1** to load the gas dataset.  
2. Select **2** to load the temperature dataset.  
3. Select **3** to compute statistics like averages, min, max, and correlations.  
4. Select **4** to view visualization results (charts generated with XChart).  
5. Select **0** to exit the program.  

---

## 📂 Project Structure
```
├── Main.java             # Entry point for running analysis sessions
├── DatasetLoader.java    # Loads CSV files (gases.csv, temps.csv)
├── GasRecord.java        # Represents a single gas emission entry
├── TempRecord.java       # Represents a single temperature entry
├── AnalysisSession.java  # Encapsulates one analysis run
├── Stats.java            # Computes statistical metrics
├── Visualization.java    # Generates charts using XChart
├── gases.csv             # Gas dataset
└── temps.csv             # Temperature dataset
```

---

📌 Future Improvements  
	• Enhance visualizations with charts and graphs  
	• Add support for more dataset formats (JSON, XML)  
	• Implement advanced correlation/regression analysis  
	• Interactive console menu for selecting datasets and stats  

⸻

📜 License  

This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.  

⸻

👤 Developed by [Your Name]  
💡 Contributions, issues, and suggestions are welcome!  
