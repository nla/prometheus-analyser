package prometheus.utils

import java.io.File;
import java.util.List;
import java.util.regex.Pattern

class DirectoryManager {
    String baseDirPath
    String dirName
    String homePath
    private File _dir
    // Form platform-independent regex patterns for stripping leading or
    // trailing slashes whether '/' or '\'
    private leadingSlashesRegex = ~"^${Pattern.quote(File.separator)}+"
    private trailingSlashesRegex = ~"${Pattern.quote(File.separator)}+\$"
    
    DirectoryManager(String dirPath) {
        this.homePath = dirPath
        setUp()    
    }
    
    DirectoryManager(String baseDirPath, String dirName) {
        this.baseDirPath = baseDirPath
        this.dirName = dirName
        this.homePath = "${baseDirPath}/${dirName}"
        setUp()
    }
    
    /**
     * Create the directory which will be managed by this object
     */
    private void setUp() {
        _dir = new File(homePath)
        
        if (!_dir.mkdir()) {
            // Directory was not *created* but may exist or not
            // depending on permissions
            if (!_dir.exists()) {
                throw new DirectoryManagerException("Permission denied when attempting to create 'home' directory '${homePath}'")
            }
        }
    }
    
    /**
     * Empty the directory
     */
    void clear() {
        _dir.eachFile {
            if (it.isDirectory()) {
                it.deleteDir()
            } else {
                it.delete()
            }
        }
    }
    
    /**
     * Delete the managed directory and all its contents
     */
    void tearDown() {
        _dir.deleteDir()
    }
    
    String normalisePath(String path) {
        def normPath = homePath + File.separator + (path - leadingSlashesRegex - trailingSlashesRegex)
        def normFile = new File(normPath).canonicalFile
        if (!normFile.path.startsWith(homePath)) {
            throw new DirectoryManagerException("Path '${path}' lies outside of directory manager's 'home' directory '${homePath}'")
        }
        
        normFile.path
    }

    Boolean validPath(String absolutePath) {
        def file = new File(absolutePath)
        if (!file.isAbsolute()) {
            throw new DirectoryManagerException("coversPath(): Path must be absolute - '${absolutePath}'")
        }    
        
        file.canonicalPath.startsWith(homePath)
    }
    
    Boolean validFile(File file) {
        validPath(file.canonicalPath)    
    }
    
    File mkdir(String localPath) {
        String path = normalisePath(localPath)
        File dir = new File(path)
        dir.mkdirs()
        dir
    }
    
    File createFile(String localPath) {
        // remove leading and trailing slashes
        def file = getFile(localPath) 
        if (validFile(file.parentFile) && file.parentFile.isDirectory()) {
            file.createNewFile()
            file
        } else {
            throw new DirectoryManagerException("Cannot create file in non-existent directory '${file.parentFile.path}'")
        } 
    }
    
    void moveFile(String fileName, String fromPath, String toPath) {
        fromPath = normalisePath(fromPath)
        toPath = normalisePath(toPath)
        String cmd = "mv ${fromPath}/${fileName} ${toPath}"
        Map result = Bash.exec(cmd)
        if (result.status != 0) {
            throw new IOException(result.stderr)
        }
    }
    
    String getPath(localPath) {
        getFile(localPath).canonicalPath
    }
    
    File getFile(localPath) {
        new File(normalisePath(localPath))    
    }
    
    Boolean isDir(localPath) {
        getFile(localPath).isDirectory()
    }
    
    File getDir(localPath) {
        File dir = getFile(localPath)
        if (dir.isDirectory()) {
            dir
        } else {
            throw new DirectoryManagerException("'${dir.canonicalPath}' is not a directory")
        }
    }
    
    File getHomeDir() {
        new File(homePath)    
    }
    
    void clearDir(localPath) {
        File dir = getDir(localPath)
        dir.eachFile {
            if (it.isDirectory()) {
                it.deleteDir()
            } else {
                it.delete()
            }
        }
    }
    
    List<File> getFiles() {
        getFiles('')    
    }
    
    List<File> getFiles(localPath) {
        def dir = getDir(localPath)
        def sortedFiles = dir.listFiles() as TreeSet
        sortedFiles as List
    }
    
    List<File> getFiles(Closure filter) {
        getFiles('', filter)
    }
    
    List<File> getFiles(String localPath, Closure filter) {
        def dir = getDir(localPath)
        def sortedFiles = dir.listFiles().findAll { filter(it) } as TreeSet
        sortedFiles as List
    }

    List<String> getFileNames() {
        getFiles().collect { it.name }
    }
    
    List<String> getFileNames(Closure filter) {
        getFileNames().findAll { filter(it) }
    }
    
    public static void main(String[] args) {
        def mgr    
        mgr = new DirectoryManager('/tmp', 'test')
        
        mgr.createFile('queued/file1')
        mgr.createFile('queued/file2')
        mgr.mkdir('complete')
        mgr.mkdir('failed')
        mgr.moveFile('file1', 'queued', 'complete')
        mgr.moveFile('file2', 'queued', 'failed')
    }
}
