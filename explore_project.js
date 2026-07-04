const fs = require('fs');
const path = require('path');

const projectRoot = __dirname;

// Fonction pour obtenir la structure du répertoire
function getDirectoryTree(dir, prefix = '', maxDepth = 20, currentDepth = 0) {
    if (currentDepth >= maxDepth) return '';
    
    let tree = '';
    try {
        const items = fs.readdirSync(dir);
        const sortedItems = items.sort((a, b) => {
            const isADir = fs.statSync(path.join(dir, a)).isDirectory();
            const isBDir = fs.statSync(path.join(dir, b)).isDirectory();
            if (isADir !== isBDir) return isBDir ? 1 : -1;
            return a.localeCompare(b);
        });

        sortedItems.forEach((item, index) => {
            const itemPath = path.join(dir, item);
            const isDir = fs.statSync(itemPath).isDirectory();
            const isLast = index === sortedItems.length - 1;
            
            // Skip node_modules, .git, etc.
            if (item.startsWith('.') || item === 'node_modules' || item === 'target') {
                return;
            }
            
            const connector = isLast ? '└── ' : '├── ';
            const nextPrefix = prefix + (isLast ? '    ' : '│   ');
            
            tree += prefix + connector + item + (isDir ? '/' : '') + '\n';
            
            if (isDir) {
                tree += getDirectoryTree(itemPath, nextPrefix, maxDepth, currentDepth + 1);
            }
        });
    } catch (err) {
        // Skip on error
    }
    
    return tree;
}

// Fonction pour trouver tous les fichiers .java
function findJavaFiles(dir, relativePath = '') {
    let javaFiles = [];
    try {
        const items = fs.readdirSync(dir);
        items.forEach(item => {
            const itemPath = path.join(dir, item);
            const relPath = path.join(relativePath, item);
            
            if (item.startsWith('.') || item === 'node_modules' || item === 'target') {
                return;
            }
            
            const stats = fs.statSync(itemPath);
            if (stats.isDirectory()) {
                javaFiles = javaFiles.concat(findJavaFiles(itemPath, relPath));
            } else if (item.endsWith('.java')) {
                javaFiles.push({
                    path: relPath.replace(/\\/g, '/'),
                    fullPath: itemPath
                });
            }
        });
    } catch (err) {
        // Skip on error
    }
    
    return javaFiles.sort((a, b) => a.path.localeCompare(b.path));
}

// Main execution
console.log('='.repeat(80));
console.log('EXPLORATION DU PROJET JAVA');
console.log('='.repeat(80));
console.log();

console.log('=== STRUCTURE DU PROJET ===');
console.log();
console.log(projectRoot + '/');
console.log(getDirectoryTree(projectRoot));
console.log();

const javaFiles = findJavaFiles(projectRoot);

console.log('=== FICHIERS JAVA TROUVÉS ===');
console.log();
console.log(`Total: ${javaFiles.length} fichiers .java`);
console.log();
javaFiles.forEach(file => {
    console.log(`- ${file.path}`);
});
console.log();

console.log('=== CONTENUS DES FICHIERS ===');
console.log();

javaFiles.forEach((file, index) => {
    console.log('='.repeat(80));
    console.log(`[${index + 1}/${javaFiles.length}] ${file.path}`);
    console.log('='.repeat(80));
    console.log();
    
    try {
        const content = fs.readFileSync(file.fullPath, 'utf-8');
        console.log(content);
    } catch (err) {
        console.log(`ERREUR: Impossible de lire le fichier - ${err.message}`);
    }
    
    console.log();
    console.log();
});

console.log('='.repeat(80));
console.log('EXPLORATION TERMINÉE');
console.log('='.repeat(80));
