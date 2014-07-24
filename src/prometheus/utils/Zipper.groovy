package prometheus.utils

import org.apache.log4j.*
import groovy.util.logging.*

@Log4j
class Zipper {
    Bash zipCmd
    
    /**
     * Extract a ZIP file 
     * This method extracts the contents of a ZIP file into a directory
     * whose name is the ZIP filename minus the '.zip' suffix. 
     * Eg. 'file123.zip' -> 'file123/'
     * The directory is created in a location specified in the 2nd parameter.
     * @param zipFile ZIP file to be extracted
     * @param destDir the path to the directory in which to create the new folder with the ZIP contents
     */
    Integer unzip(File zipFile, String destDir) {
        unzip(zipFile, new File(destDir))
    }
    
    Integer unzip(String zipFilePath, String destDir) {
        unzip(new File(zipFilePath), destDir)
    }
    
    Integer unzip(File zipFile, File destDir) {
        String targetDirPath
        String zipFilePath = zipFile.canonicalPath
        String targetDirName = zipFile.name - '.zip'
        
        if (hasRootFolder(zipFilePath)) {
            targetDirPath = "${destDir.canonicalPath}"
        } else {
            targetDirPath = "${destDir.canonicalPath}${File.separator}${targetDirName}"
        }
        
        def targetDir = new File(targetDirPath)
        targetDir.mkdirs()
        zipCmd = new Bash("unzip ${zipFilePath} -d ${targetDirPath} ")
        zipCmd.run()
        return zipCmd.getStatus()
    }
    
    Integer zip(sourceDir, destDir) {
        def source = new File(sourceDir)
        def workingDir = source.getParentFile()
        def sourceName = source.getName()
        
        zipCmd = new Bash("zip -r ${destDir}/${sourceName}.zip ${sourceName}", workingDir.absolutePath)
        zipCmd.run()
        return zipCmd.exitValue()
    }
    
    Boolean hasRootFolder(String zipFilePath) {
        Bash zipCmd = new Bash("zipinfo -1 ${zipFilePath}")
        def files
        
        zipCmd.run { result ->
            if (!result.status in [0,1]) {
                throw new RuntimeException("zipinfo command returned status > 1")
            }
            
            files = result.out.readLines()
        }
        
        if (files.size() == 1) {
            files[0].endsWith('/')
        } else {
            def prefix = files[0] // root folder
            prefix.endsWith('/') && files[1..-1].every { it.startsWith(prefix) }
        }
    }
    
    Boolean hasRootFolder(File zipFile) {
        hasRootFolder(zipFile.canonicalPath)
    }
}
