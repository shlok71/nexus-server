#!/usr/bin/env python3
"""
Script to push NexusBlock Network to GitHub via API
"""
import os
import base64
import requests
import json
from pathlib import Path

GITHUB_TOKEN = "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"
REPO_OWNER = "shlok71"
REPO_NAME = "nexus-server"
BRANCH = "main"

HEADERS = {
    "Authorization": f"token {GITHUB_TOKEN}",
    "Accept": "application/vnd.github.v3+json",
    "Content-Type": "application/json"
}

def get_file_content(filepath):
    """Read file and return base64 encoded content"""
    with open(filepath, 'rb') as f:
        content = f.read()
    return base64.b64encode(content).decode('utf-8')

def get_all_files(base_path):
    """Get all files in the repository"""
    files = []
    for root, dirs, filenames in os.walk(base_path):
        # Skip .git directory
        if '.git' in root:
            continue
        for filename in filenames:
            filepath = os.path.join(root, filename)
            rel_path = os.path.relpath(filepath, base_path)
            files.append(rel_path)
    return files

def create_blob(path, content):
    """Create a blob and return its SHA"""
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/git/blobs"
    data = {
        "content": content,
        "encoding": "base64"
    }
    response = requests.post(url, headers=HEADERS, json=data)
    if response.status_code not in [201, 422]:
        print(f"Error creating blob for {path}: {response.text}")
        return None
    return response.json().get('sha')

def create_tree(blobs):
    """Create a tree from blob SHAs"""
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/git/trees"
    tree_entries = []
    for path, sha in blobs.items():
        tree_entries.append({
            "path": path,
            "mode": "100644",
            "type": "blob",
            "sha": sha
        })
    data = {"tree": tree_entries, "base_tree": None}
    response = requests.post(url, headers=HEADERS, json=data)
    if response.status_code != 201:
        print(f"Error creating tree: {response.text}")
        return None
    return response.json().get('sha')

def get_main_branch_sha():
    """Get the SHA of the main branch"""
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/git/ref/heads/{BRANCH}"
    response = requests.get(url, headers=HEADERS)
    if response.status_code != 200:
        return None
    return response.json().get('object', {}).get('sha')

def create_commit(tree_sha, parent_sha, message):
    """Create a commit"""
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/git/commits"
    data = {
        "message": message,
        "tree": tree_sha,
        "parents": [parent_sha] if parent_sha else []
    }
    response = requests.post(url, headers=HEADERS, json=data)
    if response.status_code != 201:
        print(f"Error creating commit: {response.text}")
        return None
    return response.json().get('sha')

def update_ref(commit_sha):
    """Update the branch reference"""
    url = f"https://api.github.com/repos/{REPO_OWNER}/{REPO_NAME}/git/refs/heads/{BRANCH}"
    data = {
        "sha": commit_sha,
        "force": True
    }
    response = requests.patch(url, headers=HEADERS, json=data)
    if response.status_code not in [200, 422]:
        print(f"Error updating ref: {response.text}")
        return False
    return True

def main():
    print("Starting GitHub upload for NexusBlock Network...")
    
    base_path = "/workspace/NexusServer"
    
    # Get all files
    print("Collecting files...")
    files = get_all_files(base_path)
    print(f"Found {len(files)} files")
    
    # Create blobs for all files
    print("Creating blobs...")
    blobs = {}
    for filepath in files:
        content = get_file_content(os.path.join(base_path, filepath))
        sha = create_blob(filepath, content)
        if sha:
            blobs[filepath] = sha
            print(f"  Created blob: {filepath}")
    
    # Create tree
    print("Creating tree...")
    tree_sha = create_tree(blobs)
    if not tree_sha:
        print("Failed to create tree")
        return
    
    # Get parent commit
    parent_sha = get_main_branch_sha()
    
    # Create commit
    print("Creating commit...")
    commit_sha = create_commit(
        tree_sha,
        parent_sha,
        """Initial commit: NexusBlock Network - Hypixel-style Minecraft Server

Features implemented:
- Core server framework with NexusCore plugin
- Hub system with player management
- SkyBlock system with island management
- Hypixel-style Minion system with offline progress
- Quest system with objectives and rewards
- Heart of the Mountain (HotM) skill tree
- Custom shop system with GUI menus
- Treasure chest spawning system
- Economy system with coins
- Player authentication for cracked server support
- Cross-version compatibility via ViaVersion integration
- Velocity proxy configuration for network setup

Server Architecture:
- PaperMC 1.8.8 core for optimal performance
- Velocity proxy for public access and game mode switching
- Modular plugin architecture with separate managers
- SQLite database for player data persistence
- Custom stats system (Strength, Crit Damage, etc.)

Commands:
- /hub, /lobby - Return to hub
- /skyblock, /sb, /island - SkyBlock management
- /minion - Minion placement and management
- /quests, /quest - Quest tracking
- /hotm - Heart of the Mountain skills
- /shop - NPC shop access
- /sell - Sell items for coins
- /treasure - Treasure chest locations
- Standard commands: /msg, /reply, /tpa, /spawn, /warp"""
    )
    if not commit_sha:
        print("Failed to create commit")
        return
    
    # Update ref
    print("Updating branch reference...")
    if update_ref(commit_sha):
        print("\n✅ Successfully pushed NexusBlock Network to GitHub!")
        print(f"Repository: https://github.com/{REPO_OWNER}/{REPO_NAME}")
        print(f"Branch: {BRANCH}")
        print(f"Commit: {commit_sha}")
    else:
        print("Failed to update branch reference")

if __name__ == "__main__":
    main()
