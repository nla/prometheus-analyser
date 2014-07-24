package prometheus

import prometheus.utils.DirectoryManager

class ZipFileManager extends DirectoryManager {
    ZipFileManager(String path) {
        super(path)
    }
    
    List<String> getZipFileNames() {
        getFileNames() { it.endsWith('.zip') }
    }
}
