package prometheus
import prometheus.utils.DirectoryManager

class Workspace extends DirectoryManager {
    Workspace(String baseDir) {
        super(baseDir, 'workspace')
        init()
    }
    
    void init() {
        
    }
}
