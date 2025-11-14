# Prerequisites Installation Guide

Complete installation guide for all required tools on macOS, Windows, and Ubuntu.

## Required Software

- Docker Desktop 24.0+
- Java 21 (JDK)
- Maven 3.9+
- Git

---

## macOS Installation

### 1. Install Homebrew (if not installed)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### 2. Install Docker Desktop

**Option A: Using Homebrew**
```bash
brew install --cask docker
```

**Option B: Manual Download**
1. Download from https://www.docker.com/products/docker-desktop/
2. Open the `.dmg` file
3. Drag Docker to Applications
4. Launch Docker from Applications
5. Wait for Docker to start (whale icon in menu bar)

### 3. Install Java 21

```bash
# Install OpenJDK 21
brew install openjdk@21

# Link it
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# Set JAVA_HOME (add to ~/.zshrc or ~/.bash_profile)
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"' >> ~/.zshrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc

# Reload shell
source ~/.zshrc
```

### 4. Install Maven

```bash
brew install maven
```

### 5. Install Git

```bash
brew install git
```

### 6. Verify Installation

```bash
docker --version          # Should show 24.0+
docker-compose --version  # Should show 2.20+
java -version            # Should show 21.x
mvn -version             # Should show 3.9+
git --version            # Should show 2.x+
```

---

## Windows Installation

### 1. Install Docker Desktop

1. **Download Docker Desktop**
   - Visit https://www.docker.com/products/docker-desktop/
   - Click "Download for Windows"

2. **Run Installer**
   - Double-click `Docker Desktop Installer.exe`
   - Follow installation wizard
   - Enable WSL 2 when prompted
   - Restart computer when prompted

3. **Verify Docker**
   - Launch Docker Desktop from Start Menu
   - Wait for Docker Engine to start
   - Open PowerShell and run:
   ```powershell
   docker --version
   docker-compose --version
   ```

### 2. Install Java 21

1. **Download Java 21**
   - Visit https://adoptium.net/
   - Select "Temurin 21 (LTS)"
   - Choose Windows x64 installer (.msi)
   - Download the file

2. **Install Java**
   - Run the downloaded `.msi` file
   - Check "Add to PATH" option
   - Check "Set JAVA_HOME variable" option
   - Complete installation

3. **Verify Java**
   ```powershell
   java -version
   echo %JAVA_HOME%
   ```

### 3. Install Maven

1. **Download Maven**
   - Visit https://maven.apache.org/download.cgi
   - Download `apache-maven-3.9.x-bin.zip`

2. **Extract Maven**
   - Extract to `C:\Program Files\Maven`
   - Final path should be `C:\Program Files\Maven\apache-maven-3.9.x`

3. **Set Environment Variables**
   - Open "Environment Variables" (Search in Start Menu)
   - Under "System Variables", click "New"
   - Variable name: `MAVEN_HOME`
   - Variable value: `C:\Program Files\Maven\apache-maven-3.9.x`
   - Edit "Path" variable
   - Add new entry: `%MAVEN_HOME%\bin`
   - Click OK

4. **Verify Maven**
   ```powershell
   mvn -version
   ```

### 4. Install Git

1. **Download Git**
   - Visit https://git-scm.com/download/win
   - Download the installer

2. **Install Git**
   - Run the installer
   - Use default options
   - Complete installation

3. **Verify Git**
   ```powershell
   git --version
   ```

### 5. Verify All Tools

Open PowerShell and run:
```powershell
docker --version
docker-compose --version
java -version
mvn -version
git --version
```

---

## Ubuntu/Debian Installation

### 1. Update System

```bash
sudo apt-get update
sudo apt-get upgrade -y
```

### 2. Install Docker

```bash
# Install dependencies
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# Add Docker's official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker $USER

# Apply group changes
newgrp docker

# Enable Docker to start on boot
sudo systemctl enable docker
sudo systemctl start docker
```

### 3. Install Java 21

```bash
# Install OpenJDK 21
sudo apt-get install -y openjdk-21-jdk

# Set JAVA_HOME (add to ~/.bashrc)
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc

# Reload shell
source ~/.bashrc
```

### 4. Install Maven

```bash
sudo apt-get install -y maven
```

### 5. Install Git

```bash
sudo apt-get install -y git
```

### 6. Verify Installation

```bash
docker --version          # Should show 24.0+
docker compose version    # Should show 2.20+
java -version            # Should show 21.x
mvn -version             # Should show 3.9+
git --version            # Should show 2.x+
```

---

## Post-Installation

### Configure Docker Resources

**macOS/Windows:**
1. Open Docker Desktop
2. Go to Settings → Resources
3. Set Memory to at least 8GB
4. Set CPUs to at least 4
5. Apply & Restart

**Ubuntu:**
Docker uses system resources by default. Ensure your system has:
- At least 8GB RAM
- At least 20GB free disk space

### Test Docker

```bash
# Test Docker
docker run hello-world

# Test Docker Compose
docker compose version
```

---

## Troubleshooting

### macOS

**Docker not starting?**
```bash
# Reset Docker
rm -rf ~/Library/Containers/com.docker.docker
# Restart Docker Desktop
```

**Java not found?**
```bash
# Check Java installation
/usr/libexec/java_home -V

# Set JAVA_HOME manually
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Windows

**Docker requires WSL 2?**
1. Open PowerShell as Administrator
2. Run: `wsl --install`
3. Restart computer
4. Reinstall Docker Desktop

**Maven not found?**
- Verify PATH includes `%MAVEN_HOME%\bin`
- Restart PowerShell after setting environment variables

### Ubuntu

**Permission denied for Docker?**
```bash
# Add user to docker group
sudo usermod -aG docker $USER

# Log out and log back in, or run:
newgrp docker
```

**Java version conflict?**
```bash
# List installed Java versions
sudo update-alternatives --config java

# Select Java 21
```

---

## Verification Checklist

Run these commands to verify everything is installed:

```bash
# Docker
docker --version
docker compose version
docker run hello-world

# Java
java -version
echo $JAVA_HOME  # macOS/Linux
echo %JAVA_HOME% # Windows

# Maven
mvn -version

# Git
git --version
```

Expected output:
- Docker: 24.0 or higher
- Docker Compose: 2.20 or higher
- Java: 21.x
- Maven: 3.9 or higher
- Git: 2.x or higher

---

## Next Steps

Once all prerequisites are installed:

1. Clone the repository
2. Navigate to `local-deploy` folder
3. Follow [QUICKSTART.md](QUICKSTART.md) for 5-minute setup
4. Or see [README.md](README.md) for complete documentation

---

**All set! Ready to deploy! 🚀**
