# Unified Patient Manager

---

## Section 1: How to Compile and Run the System

---

### 1. Prerequisites
---

#### Java 17 or Higher
   - **Why needed:** The backend is written in Java
   - **Check if installed:** Open terminal and run:
     ```bash
     java -version
     ```
   - **If not installed:**
     - **macOS:** `brew install java17` (if you have Homebrew)
     - **Windows:** Download from https://www.oracle.com/java/technologies/downloads/#java17
     - **Linux:** `sudo apt-get install openjdk-17-jdk` (Ubuntu/Debian)

#### Maven 3.6+
   - **Why needed:** Builds the Java backend and manages dependencies
   - **Check if installed:** Open terminal and run:
     ```bash
     mvn -version
     ```
   - **If not installed:**
     - **macOS:** `brew install maven`
     - **Windows:** Download from https://maven.apache.org/download.cgi (then add to PATH)
     - **Linux:** `sudo apt-get install maven` (Ubuntu/Debian)

#### Node.js 16+ and npm
   - **Why needed:** Runs and builds the React frontend
   - **Check if installed:** Open terminal and run:
     ```bash
     node --version
     npm --version
     ```
   - **If not installed:**
     - **macOS:** `brew install node`
     - **Windows:** Download from https://nodejs.org/ (includes npm)
     - **Linux:** `sudo apt-get install nodejs npm` (Ubuntu/Debian)

#### MySQL 8.0+
   - **Why needed:** Database for storing patient records
   - **Check if installed:** Open terminal and run:
     ```bash
     mysql --version
     ```
   - **If not installed:**
     - **macOS:** 
       ```bash
       brew install mysql
       brew services start mysql
       ```
     - **Windows:** Download from https://www.mysql.com/downloads/mysql/
     - **Linux:** `sudo apt-get install mysql-server` (Ubuntu/Debian)
   
   - **Critical: Set MySQL Root Password**
     - During installation (Windows) or after (macOS/Linux), you need to set a root password
     - **macOS/Linux:** If you didn't set one, run:
       ```bash
       mysql -u root
       ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_password_here';
       FLUSH PRIVILEGES;
       exit
       ```
     - **If you need to reset root password**
      - Start MySQL in password-reset mode:
        ```bash
        /opt/homebrew/opt/mysql/bin/mysqld_safe --skip-grant-tables --skip-networking &
        ```
      - Log in with root (no password):
        ```bash
        mysql -u root
        ```
      - Change the root password:
        ```bash
        FLUSH PRIVILEGES;
        ALTER USER 'root'@'localhost' IDENTIFIED BY 'YOUR_NEW_PASSWORD_HERE!';
        EXIT;
        ```
      - Stop the password-reset server:
        ```bash
        ps aux | grep mysqld
        ```
      - Start MySQL normally:
        ```bash
        brew services start mysql
        ```
     - **Important:** Remember this password - you'll need it later!

---

### 2. Database Setup
---

#### Quick Setup - Import Pre-built Database

##### Step 1: Navigate to Project Root
```bash
cd /path/to/egesCS411Team
```

##### Step 2: Start MySQL
**macOS/Linux:**
```bash
mysql.server start
```

**Windows:** Open Services and start MySQL80 (or open MySQL Command Line Client)

##### Step 3a: Verify MySQL is Running
```bash
mysql -u root -p
```
- Enter your MySQL root password (set during installation)
- You should see `mysql>` prompt

### Step 3b: Create the database (if it doesn’t already exist):
```bash
CREATE DATABASE EHR;
```
- You can use any name — but in this case EHR is correct
- Type `exit` to quit

##### Step 4: Import the Database
In the repository files, there should be a downloadable file named "dump-EHR-202512101432.sql". Download that. 

**macOS/Linux:**
```bash
mysql -u root -p < dump-EHR-202512101432.sql
```

**Windows (Command Prompt):**
```bash
mysql -u root -p < dump-EHR-202512101432.sql
```

##### Step 5: Verify Import
```bash
mysql -u root -p
USE EHR;
SHOW TABLES;
```

You should see tables like `user`, `medication`, `allergy`, `medical_history`, etc.


---

### 3. Configure Database Connection
---

The backend needs your MySQL password to connect. Follow these steps:

#### Step 1: Find the Configuration File
```bash
cd /path/to/egesCS411Team/UnifiedPatientManager
# Then open this file in your text editor:
src/main/resources/application.properties
```

#### Step 2: Add Your MySQL Password

**Find this section:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/EHR?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

**Add your password on a new line after `spring.datasource.username=root`:**
```properties
spring.datasource.password=your_mysql_password_here
```

**Example (if your password is "myPassword123"):**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/EHR?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=myPassword123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

#### Step 3: Save the File

---

### 4. Compilation of the System
---

#### Step 1: Navigate to Project Directory

Open your terminal and navigate to the project root:

```bash
cd /path/to/egesCS411Team/UnifiedPatientManager
```

Replace `/path/to/` with your actual path. Example:
- **macOS/Linux:** `cd ~/Downloads/egesCS411Team/UnifiedPatientManager`
- **Windows:** `cd C:\Users\YourName\Downloads\egesCS411Team\UnifiedPatientManager`

#### Step 2: Compile Backend with Maven

**For macOS:**
```bash
mvn clean install
```

**For Windows:**
```bash
mvnw.cmd clean install
```

**What this does:**
- `clean` - Removes old build files
- `compile` - Compiles Java source code and downloads dependencies (first time may take 2-3 minutes)

**Expected output:**
```
[INFO] BUILD SUCCESS
```

#### Step 3: Install Frontend Dependencies

Open a **new terminal window** (keep the old one open) and navigate to the frontend directory:

```bash
cd /path/to/egesCS411Team/UnifiedPatientManager/frontend
```

Install npm packages:
```bash
npm install
```

**What this does:**
- Downloads and installs all React dependencies (may take 1-2 minutes)
- Creates a `node_modules/` folder

**Expected output:**
```
added XXX packages
```

---

### 5. Running the System
---

#### Important: Start Services in This Order

You need to run **three services simultaneously** in separate terminal windows. Open three terminal windows and follow these steps in order.

---

##### Terminal 1: Start MySQL Database

Run this command first to ensure the database is ready:

```bash
mysql.server start
```

**Expected output:**
```
Starting MySQL
.
Success!
```
---

##### Terminal 2: Start Backend (Java Spring Boot)

In the first terminal (or a new one), navigate to the backend directory:

```bash
cd /path/to/egesCS411Team/UnifiedPatientManager
```

**Start the Spring Boot server:**

**For macOS:**
```bash
mvn spring-boot:run
```

**For Windows:**
```bash
mvnw.cmd spring-boot:run
```

**What to expect:**
- The application will start and print logs
- Wait for this message to appear (usually takes 10-20 seconds):
  ```
  [INFO] o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port(s): 8080
  [INFO] c.E.U.UnifiedPatientManagerApplication   : Started UnifiedPatientManagerApplication in X.XXX seconds
  ```

** Backend is ready when you see these messages. Do NOT close this terminal.**

---

##### Terminal 3: Start Frontend (React)

Open a **third terminal window** and navigate to the frontend directory:

```bash
cd /path/to/egesCS411Team/UnifiedPatientManager/frontend
```

Start the development server:

```bash
npm run dev
```

**What to expect:**
- The frontend will compile and print:
  ```
  VITE v7.2.2  ready in XXX ms
  
  ➜  Local:   http://localhost:5173/
  ➜  press h to show help
  ```

**Frontend is ready when you see this message. Do NOT close this terminal.**

---

#### Step 4: Access the Application

Open your web browser and go to:

```
http://localhost:5173
```

**You should see the UPM login page.**

---
---

## Section 2: How to Run Tests and Get Coverage Report

### Run All Tests

```bash
# Navigate to project root
cd /path/to/UnifiedPatientManager

# Run all tests using Maven
mvn test
```

## Understanding Test Results

### What You'll See
NOTICE: The full tests take at a minimum 5 minutes to run completely. This is because the test to see if our One Time Password has does robustness testing on the max +. The full test suite should realistically take no more than 5 minutes and 15 seconds to run. 

When tests complete successfully, you'll see output like this:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.Eges411Team.UnifiedPatientManager.serviceUnitTests.loginTests.LoginAttemptWindowTest
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.064 s
...
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
### Viewing Coverage Reports
After running tests, a coverage report is automatically generated:

The completion prompt includes coverage statistics:

```
INSTR: X  # Instruction coverage
BR:    X     # Branch coverage
LINE:  X   # Line coverage
```


