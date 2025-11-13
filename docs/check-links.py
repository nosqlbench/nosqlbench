#!/usr/bin/env python3
"""
Simple link checker for the built documentation site.
Checks all internal links in the public/ directory.
"""

import os
import re
from pathlib import Path
from urllib.parse import urljoin, urlparse
from collections import defaultdict

def extract_links(html_file):
    """Extract all href links from an HTML file."""
    with open(html_file, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()

    # Find all href attributes
    links = re.findall(r'href="([^"]*)"', content)
    return links

def is_internal_link(link):
    """Check if link is internal (not external URL)."""
    parsed = urlparse(link)
    # Internal if no scheme or if localhost/docs.nosqlbench.io
    if not parsed.scheme:
        return True
    if 'localhost' in parsed.netloc or 'docs.nosqlbench.io' in parsed.netloc or '127.0.0.1' in parsed.netloc:
        return True
    return False

def normalize_link(link, base_path):
    """Normalize a link for checking."""
    parsed = urlparse(link)

    # Remove fragment
    path = parsed.path

    # Skip empty, external, javascript, mailto
    if not path or path.startswith('#') or link.startswith('javascript:') or link.startswith('mailto:'):
        return None

    # Handle absolute paths from site root
    if path.startswith('/'):
        return path

    # Handle relative paths
    if not is_internal_link(link):
        return None

    # Relative to current file
    current_dir = os.path.dirname(base_path)
    normalized = os.path.normpath(os.path.join(current_dir, path))
    return normalized

def check_link_exists(link, public_dir):
    """Check if a link target exists in the public directory."""
    # Remove leading slash for file path
    path = link.lstrip('/')

    # Try direct file
    full_path = os.path.join(public_dir, path)
    if os.path.isfile(full_path):
        return True

    # Try as directory with index.html
    if os.path.isdir(full_path):
        index_path = os.path.join(full_path, 'index.html')
        if os.path.isfile(index_path):
            return True

    # Try appending index.html
    if not path.endswith('.html'):
        index_path = os.path.join(public_dir, path, 'index.html')
        if os.path.isfile(index_path):
            return True

    return False

def main():
    public_dir = Path('public')
    if not public_dir.exists():
        print("Error: public/ directory not found. Run 'zola build' first.")
        return 1

    broken_links = defaultdict(list)
    total_links = 0
    broken_count = 0

    print("Checking all HTML files in public/...")
    print()

    # Check all HTML files
    for html_file in public_dir.rglob('*.html'):
        rel_path = html_file.relative_to(public_dir)
        links = extract_links(html_file)

        for link in links:
            if not is_internal_link(link):
                continue

            # Get path without fragment
            parsed = urlparse(link)
            link_path = parsed.path

            if not link_path or link_path.startswith('#'):
                continue

            total_links += 1

            # Check if target exists
            if not check_link_exists(link_path, str(public_dir)):
                broken_links[str(rel_path)].append(link)
                broken_count += 1

    # Report results
    print(f"Total internal links checked: {total_links}")
    print(f"Broken links found: {broken_count}")
    print()

    if broken_links:
        print("BROKEN LINKS BY FILE:")
        print("=" * 80)
        for source_file, links in sorted(broken_links.items())[:20]:  # Show first 20
            print(f"\n{source_file}:")
            for link in links[:5]:  # Show first 5 broken links per file
                print(f"  → {link}")
            if len(links) > 5:
                print(f"  ... and {len(links) - 5} more")

        if len(broken_links) > 20:
            print(f"\n... and {len(broken_links) - 20} more files with broken links")

        return 1
    else:
        print("✅ All internal links are valid!")
        return 0

if __name__ == '__main__':
    exit(main())
