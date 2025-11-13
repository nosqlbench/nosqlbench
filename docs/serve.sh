#!/bin/bash
# NoSQLBench Documentation Server
# Builds and serves the documentation site locally

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "NoSQLBench Documentation Server"
echo "================================"
echo ""

# Check if Zola is installed
if ! command -v zola &> /dev/null; then
    echo -e "${RED}Error: Zola is not installed${NC}"
    echo ""
    echo "Please install Zola to build and serve the documentation:"
    echo ""
    echo -e "${YELLOW}On Linux:${NC}"
    echo "  # Download and install from GitHub releases"
    echo "  wget https://github.com/getzola/zola/releases/download/v0.19.2/zola-v0.19.2-x86_64-unknown-linux-gnu.tar.gz"
    echo "  tar xzf zola-v0.19.2-x86_64-unknown-linux-gnu.tar.gz"
    echo "  sudo mv zola /usr/local/bin/"
    echo ""
    echo "  # Or use snap"
    echo "  snap install zola --edge"
    echo ""
    echo -e "${YELLOW}On macOS:${NC}"
    echo "  # Using Homebrew"
    echo "  brew install zola"
    echo ""
    echo "  # Or download from GitHub releases"
    echo "  wget https://github.com/getzola/zola/releases/download/v0.19.2/zola-v0.19.2-x86_64-apple-darwin.tar.gz"
    echo "  tar xzf zola-v0.19.2-x86_64-apple-darwin.tar.gz"
    echo "  sudo mv zola /usr/local/bin/"
    echo ""
    echo "Visit https://www.getzola.org/documentation/getting-started/installation/"
    exit 1
fi

echo -e "${GREEN}✓${NC} Zola is installed ($(zola --version))"

# Check if Abridge theme exists
if [ ! -d "themes/abridge/templates" ]; then
    echo -e "${YELLOW}⚠${NC} Abridge theme not found or incomplete"
    echo ""

    # Check if we're in a git repository
    if [ ! -d "../.git" ]; then
        echo -e "${RED}Error: Not in a git repository${NC}"
        echo "The Abridge theme should be added as a git submodule."
        echo "Please initialize git first or run from the project root."
        exit 1
    fi

    echo "Adding Abridge theme as git submodule..."
    cd ..
    git submodule add https://github.com/Jieiku/abridge.git docs/themes/abridge 2>/dev/null || {
        echo "Updating existing submodule..."
        git submodule update --init --recursive docs/themes/abridge
    }
    cd docs

    echo -e "${GREEN}✓${NC} Abridge theme added/updated"
else
    echo -e "${GREEN}✓${NC} Abridge theme is present"
fi

# Build the site
echo ""
echo "Building site..."
zola build

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Site built successfully"
else
    echo -e "${RED}Error: Build failed${NC}"
    exit 1
fi

# Serve the site
echo ""
echo "Starting documentation server..."
echo -e "${GREEN}→${NC} Serving at http://0.0.0.0:1111"
echo -e "${GREEN}→${NC} Access from this machine: http://localhost:1111"
echo -e "${GREEN}→${NC} Access from network: http://$(hostname -I | awk '{print $1}'):1111"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

zola serve --interface 0.0.0.0 --port 1111
