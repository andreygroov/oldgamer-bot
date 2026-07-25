#!/bin/bash
# Setup script for VPS deployment
# Run as root on VPS: ssh root@147.45.134.129 'bash -s' < setup-vps.sh

set -e

echo "=================================================="
echo "OLD GAMER BOT - VPS SETUP"
echo "=================================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
   echo "ERROR: This script must be run as root"
   exit 1
fi

# Variables
PROJECT_DIR="/opt/oldgamer-bot"
GITHUB_REPO="https://github.com/YOUR_USERNAME/oldgamer-bot.git"

echo "[1/5] Creating project directory..."
mkdir -p $PROJECT_DIR
cd $PROJECT_DIR

echo "[2/5] Cloning repository from GitHub..."
# If directory not empty, pull instead of clone
if [ -d .git ]; then
    echo "Repository already exists, pulling updates..."
    git pull origin main || git pull origin master
else
    echo "Cloning new repository..."
    git clone $GITHUB_REPO .
fi

echo "[3/5] Checking Docker and Docker Compose..."
docker --version || { echo "Docker not installed!"; exit 1; }
docker-compose --version || { echo "Docker Compose not installed!"; exit 1; }

echo "[4/5] Creating database if needed..."
# Schema will be created automatically by docker-compose
# But we can pre-create the DB with custom initialization if needed

echo "[5/5] Starting containers..."
docker-compose down 2>/dev/null || true
docker-compose pull
docker-compose up -d

echo ""
echo "=================================================="
echo "✅ SETUP COMPLETE!"
echo "=================================================="
echo ""
echo "Project directory: $PROJECT_DIR"
echo "Status:"
docker-compose ps
echo ""
echo "To view logs:"
echo "  cd $PROJECT_DIR"
echo "  docker-compose logs -f bot"
echo ""
echo "To stop containers:"
echo "  cd $PROJECT_DIR"
echo "  docker-compose down"
echo ""
