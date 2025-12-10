# Unified Patient Manager

---

## Section 1: How to Compile and Run the System

---

### Prerequisites
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
     - **Important:** Remember this password - you'll need it later!

---

### Database Setup
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

##### Step 3: Verify MySQL is Running
```bash
mysql -u root -p
```
- Enter your MySQL root password (set during installation)
- You should see `mysql>` prompt
- Type `exit` to quit

##### Step 4: Import the Database

**macOS/Linux:**
```bash
mysql -u root -p < database.sql
```

**Windows (Command Prompt):**
```bash
mysql -u root -p < database.sql
```

##### Step 5: Verify Import
```bash
mysql -u root -p
USE EHR;
SHOW TABLES;
```

You should see tables like `user`, `medication`, `allergy`, `medical_history`, etc.


---

### Configure Database Connection
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

### Compilation of the System
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

**For macOS/Linux:**
```bash
./mvnw clean compile
```

**For Windows:**
```bash
mvnw.cmd clean compile
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

### Running the System
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

**For macOS/Linux:**
```bash
./mvnw spring-boot:run
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

(Instructions to be added)

