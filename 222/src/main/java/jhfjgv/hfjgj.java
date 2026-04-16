package jhfjgv;

public class hfjgj {
	public static void main(String [] args) {
		System.out.println("Deepsi");
	}

}
//SonarCube Installation in Ubuntu
Step-1:- Install the PostgreSQL if it's not installed on your Ubuntu 24.04 workstation.
sudo apt install -y postgresql-common postgresql -y
Step-2:- Enable the PostgreSQL database server to automatically start at boot.
sudo systemctl enable postgresql
Step-3:- Start the PostgreSQL database server.
sudo systemctl start postgresql
Step-4:- Log in to the PostgreSQL database server as the postgres user.
sudo -u postgres psql
Step-5:- Create a new sonaruser PostgreSQL role with a strong password to use with SonarQube. Replace your_password with your desired password.
CREATE ROLE sonaruser WITH LOGIN ENCRYPTED PASSWORD 'your_password';
Step-6:- Create a new sonarqube database.
CREATE DATABASE sonarqube;
Step-7:- Grant the sonaruser role full privileges to the sonarqube database.
GRANT ALL PRIVILEGES ON DATABASE sonarqube TO sonaruser;
Step-8:- Switch to the sonarqube database.
\c sonarqube
GRANT ALL PRIVILEGES ON SCHEMA public TO sonaruser;
Step-9:- Exit the PostgreSQL database console.
\q
Install SonarQube
SonarQube is not available in the default package repositories on Ubuntu 24.04 and requires OpenJDK 21 to run. Follow the steps below to download the latest SonarQube release file and install SonarQube.
Step-1:- Update the server's APT package index.
sudo apt update
Step-2:- Install OpenJDK 17.
sudo apt install openjdk-17-jdk -y
Step-3:- Install Unzip to extract files from the SonarQube archive.
sudo apt install unzip
Step-4:- Verify the installed java version.
java -version
sudo wget https://binaries.sonarsource.com/Distribution/sonarqube/sonarqube-25.2.0.102705.zip
Step-5:- Extract files from the downloaded archive using Unzip.
unzip sonarqube-25.2.0.102705.zip
Step-6:- Move the extracted files to a systemwide directory such as /opt.
sudo mv sonarqube-25.2.0.102705/ /opt/sonarqube
Step-7:- Create a dedicated sonarqube system user without login privileges and a home directory.
sudo adduser --system --no-create-home --group --disabled-login sonarqube
Step-8:- Grant the sonarqube user full privileges to the /opt/sonarqube directory.
sudo chown -R sonarqube:sonarqube /opt/sonarqube
Install SonarScanner CLI
SonarQube uses code scanners depending on the target programming language to scan and analyze code quality. SonarScanner CLI is the default scanner if no specific scanner is specified on your system. Follow the steps below to install the SonarScanner CLI to analyze code on your workstation.
Step-1:- Visit the SonarScanner CLI page and verify the latest version to download. For example, run the following command to download the SonarScanner CLI version 7.0.1
wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-7.0.1.4817-linux-x64.zip
Step-2:- Extract files from the archive depending on the downloaded version.
unzip sonar-scanner-cli-7.0.1.4817-linux-x64.zip
Step-3:- Move the extracted directory to /opt/sonarscanner.
sudo mv sonar-scanner-7.0.1.4817-linux-x64/ /opt/sonarscanner
Step-4:- Open the sonar-scanner.properties configuration file.
sudo nano /opt/sonarscanner/conf/sonar-scanner.properties
Step-5:- Find the following sonar.host.url directive and change the default https://mycompany.com/sonarqube value to 127.0.0.1.
sonar.host.url=127.0.0.1
sudo chmod +x /opt/sonarscanner/bin/sonar-scanner
Step-6:- Link the sonar-scanner binary to the /usr/local/bin directory to enable it as a system-wide command.
sudo ln -s /opt/sonarscanner/bin/sonar-scanner /usr/local/bin/sonar-scanner
Step-7:- View the installed SonarScanner version.
sonar-scanner -v
Your output should be similar to the one below.
13:33:31.946 INFO SonarScanner CLI 7.0.1.4817
13:33:31.950 INFO Java 17.0.13 Eclipse Adoptium (64-bit)
13:33:31.951 INFO Linux 6.8.0-51-generic amd64
Configure SonarQube
SonarQube requires specific configurations for optimal performance, including database connections, Java runtime options, system resource limits, and user permissions. Follow the steps below to configure SonarQube to run on your server.
Step-1:- Open the main sonar.properties Sonarqube configuration file.
sudo nano /opt/sonarqube/conf/sonar.properties
Step-2:- Add the following configurations at the end of the file. Replace sonaruser and your_password with actual PostgreSQL database user details.
sonar.jdbc.username=sonaruser
sonar.jdbc.password=your_password
sonar.jdbc.url=jdbc:postgresql://localhost:5432/sonarqube
sonar.web.javaAdditionalOpts=-server
sonar.web.host=0.0.0.0
sonar.web.port=9000
sudo nano /etc/sysctl.conf
Step-3:- Add the following directives at the end of the file.
vm.max_map_count=524288
fs.file-max=131072
sudo nano /etc/security/limits.d/99-sonarqube.conf
Step-4:- Add the following directives to increase the file descriptor and process limits for SonarQube.
sonarqube - nofile 131072
sonarqube - nproc 8192
sudo ufw allow 9000/tcp
● Run the following command to install UFW and allow SSH connections if it's unavailable.
sudo apt install ufw -y && sudo ufw allow 22/tcp
Step-5:- Reload UFW to apply the firewall configurations.
sudo ufw reload
Step-6:- View the UFW status and verify that below are the only active firewall rules.
sudo ufw status
Your output should be similar to the one below: Status: active
To Action From
-- ------ ----
22/tcp ALLOW Anywhere
9000/tcp ALLOW Anywhere
22/tcp (v6) ALLOW Anywhere (v6)
1. 9000/tcp (v6) ALLOW Anywhere (v6)
Set Up SonarQube as a System Service
Follow the steps below to set up a new system service for SonarQube to manage the application processes on your server.
Step-1:- Create a new sonarqube.service file.
sudo nano /etc/systemd/system/sonarqube.service
Step-2:- Add the following configurations to the file.
[Unit]
Description=SonarQube service
After=syslog.target network.target
[Service]
Type=forking
ExecStart=/opt/sonarqube/bin/linux-x86-64/sonar.sh start
ExecStop=/opt/sonarqube/bin/linux-x86-64/sonar.sh stop
User=sonarqube
Group=sonarqube
PermissionsStartOnly=true
Restart=always
StandardOutput=syslog
LimitNOFILE=131072
LimitNPROC=8192
TimeoutStartSec=5
SuccessExitStatus=143
[Install]
WantedBy=multi-user.target
sudo systemctl daemon-reload
Step-3:- Enable SonarQube to start at boot.
sudo systemctl enable sonarqube
Step-4:-Start the SonarQube service.
sudo systemctl start sonarqube
Step-5:- View the SonarQube service status and verify that it's running.
sudo systemctl status sonarqube
sudo reboot now

