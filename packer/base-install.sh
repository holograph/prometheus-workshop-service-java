#!/bin/bash -e

cloud-init status --wait

export DEBIAN_FRONTEND=noninteractive
export DEBCONF_NONINTERACTIVE_SEEN=true

echo '--- Installing base system ---'

echo '- Installing base components'
sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install -y                           \
  apt-transport-https software-properties-common  \
  wget net-tools jq curl zip unzip

echo '- Setting up Linux desktop'
sudo apt-get install -y ubuntu-desktop-minimal xrdp
sudo sed -i 's/^port=3389/port=tcp:\/\/:3389/' /etc/xrdp/xrdp.ini
sudo systemctl restart xrdp.service
cat <<"EOF" | sudo tee /etc/dconf/profile/user
user-db:user
system-db:local
EOF
sudo mkdir /etc/dconf/db/local.d
cat <<"EOF" | sudo tee /etc/dconf/db/local.d/00-no-overview
[org/gnome/shell/extensions/dash-to-dock]
disable-overview-on-startup=true
EOF
sudo dconf update

echo '- Setting up lab user'
sudo useradd -g sudo -m -s /bin/bash -p $(echo "student" | openssl passwd -1 -stdin) student
sudo mkdir -p /home/student/.ssh
sudo mkdir -p /home/student/.config/autostart
echo 'ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIEz4jal99UkJ8EOL/oTQQRvlRZa+gF8PXI1PeEl/+y35 lab@example.com' \
  | sudo tee /home/student/.ssh/authorized_keys
echo 'yes' | sudo tee /home/student/.config/gnome-initial-setup-done
sudo chown -R student /home/student
./conky-install.sh

echo '- Disabling autoupdate dialogs'
cat <<"EOF" | sudo tee /etc/apt/apt.conf.d/20auto-upgrade
APT::Periodic::Update-Package-Lists "0";
APT::Periodic::Unattended-Upgrade "0";
EOF
sudo sed -i 's/Prompt=lts/Prompt=never/' /etc/update-manager/release-upgrades

echo '- Re-enabling SSH'
sudo systemctl enable ssh.service

echo '--- Installing lab components ---'
./service-install.sh
./otel-collector-install.sh
./prometheus-install.sh
./grafana-install.sh
