#!/usr/bin/env python3
import os
import sys
from pathlib import Path

project_root = Path(__file__).parent

def get_directory_tree(directory, prefix="", max_depth=20, current_depth=0, ignore_patterns=None):
    """Generate a tree structure of the directory."""
    if ignore_patterns is None:
        ignore_patterns = {'.', '__pycache__', 'node_modules', 'target', '.git', '.vscode'}
    
    if current_depth >= max_depth:
        return ""
    
    tree = ""
    try:
        items = sorted(os.listdir(directory), key=lambda x: (not os.path.isdir(os.path.join(directory, x)), x))
        
        for i, item in enumerate(items):
            if item.startswith('.') or item in ignore_patterns:
                continue
                
            item_path = os.path.join(directory, item)
            is_dir = os.path.isdir(item_path)
            is_last = i == len(items) - 1
            
            connector = "└── " if is_last else "├── "
            next_prefix = prefix + ("    " if is_last else "│   ")
            
            tree += prefix + connector + item + ("/" if is_dir else "") + "\n"
            
            if is_dir:
                tree += get_directory_tree(item_path, next_prefix, max_depth, current_depth + 1, ignore_patterns)
    except PermissionError:
        pass
    
    return tree

def find_java_files(directory, relative_path=""):
    """Find all .java files in the directory."""
    java_files = []
    
    ignore_patterns = {'.', '__pycache__', 'node_modules', 'target', '.git', '.vscode'}
    
    try:
        for item in os.listdir(directory):
            if item.startswith('.') or item in ignore_patterns:
                continue
            
            item_path = os.path.join(directory, item)
            rel_path = os.path.join(relative_path, item)
            
            if os.path.isdir(item_path):
                java_files.extend(find_java_files(item_path, rel_path))
            elif item.endswith('.java'):
                java_files.append({
                    'path': rel_path.replace('\\', '/'),
                    'full_path': item_path
                })
    except PermissionError:
        pass
    
    return sorted(java_files, key=lambda x: x['path'])

# Main execution
print("=" * 80)
print("EXPLORATION DU PROJET JAVA")
print("=" * 80)
print()

print("=== STRUCTURE DU PROJET ===")
print()
print(str(project_root) + "/")
print(get_directory_tree(project_root))
print()

java_files = find_java_files(project_root)

print("=== FICHIERS JAVA TROUVÉS ===")
print()
print(f"Total: {len(java_files)} fichiers .java")
print()
for java_file in java_files:
    print(f"- {java_file['path']}")
print()

print("=== CONTENUS DES FICHIERS ===")
print()

for index, java_file in enumerate(java_files, 1):
    print("=" * 80)
    print(f"[{index}/{len(java_files)}] {java_file['path']}")
    print("=" * 80)
    print()
    
    try:
        with open(java_file['full_path'], 'r', encoding='utf-8') as f:
            print(f.read())
    except Exception as e:
        print(f"ERREUR: Impossible de lire le fichier - {e}")
    
    print()
    print()

print("=" * 80)
print("EXPLORATION TERMINÉE")
print("=" * 80)
