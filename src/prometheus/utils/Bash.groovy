package prometheus.utils

/**
 * Bash shell command runner
 * @author mark
 *
 */
class Bash {
    ProcessBuilder procBuilder
    Process proc
    protected String cmd
    StringBuilder out, err
    Integer status
    
    Bash() {
        this('')
    }
    
    Bash(String cmd) {
        this.cmd = cmd
        out = new StringBuilder()
        err = new StringBuilder()
        procBuilder = new ProcessBuilder('/bin/bash', '-c', cmd)
    }

    Bash(String cmd, String workDirPath) {
        this(cmd)
        procBuilder.directory(new File(workDirPath))   
    }    
    
    Bash(List<String> cmd) {
        this(cmd.join(' '))
    }
    
    Bash(List<String> cmd, String workDirPath) {
        this(cmd.join(' '), workDirPath)
    }
    
    // Add a bash() method to GString and String
    static def addBashMethodToStrings(){
        GString.metaClass['bash'] = { Bash.bash(delegate) }
        String.metaClass['bash'] =  { Bash.bash(delegate) }
    }

    static String bash(cmd) {
        new Bash(cmd).run()
    }

    static Map exec(String cmd) {
        exec(cmd, null)
    }
    

    static Map exec(String cmd, String workingDir) {
        Bash bash = new Bash(cmd as String)
        if (workingDir != null) {
            bash.cd(workingDir)
        }
        bash.run()
        
        [
            stdout: bash.stdout,
            stderr: bash.stderr,
            status: bash.status 
        ]
    }
    
    void setCmd(cmd) {
        switch(cmd) {
            case String:
            case GString:
                this.@cmd = cmd as String
                break
            case List:
                this.@cmd = cmd.join(' ')
                break
        } 
        
        procBuilder.command('/bin/bash', '-c', this.@cmd)
    }
    
    String getCmd() {
        this.@cmd
    }

    void cd(File dir) {
        if (dir.isAbsolute()) {
            procBuilder.directory(dir.getCanonicalFile())
        } else {
            cd(procBuilder.directory().canonicalPath + '/' + dirPath)
        }    
    }
    
    void cd(String dirPath) {
        File dir = new File(dirPath)
        cd(dir)
    }
    
    String getCwd() {
        procBuilder.directory().getCanonicalPath()
    }
    
    String run() {
        out.setLength(0)
        err.setLength(0)
        proc = procBuilder.start()
        proc.waitForProcessOutput(out, err)
        status = proc.exitValue()
        out.toString()
    }
    
    Object run(Closure c) {
        run()
        c([out: stdout, err: stderr, status: status])   
    }
    
    String getStdout() {
        out.toString()
    }
    
    String getStderr() {
        err.toString()
    }
    
    Integer getStatus() {
        status
    }
}
