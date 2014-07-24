package prometheus.fits

class FITSRawToolOutputAnalyser {
    def dataDir = '/data/prometheus-analysis/test-corpus/thread9'
    def resultFile = '/data/prometheus-analysis/test-corpus/thread9/stats.csv'
    
    public final static void main(args) {
        def a = new FITSRawToolOutputAnalyser()
        
        a.processFiles()
        
    }
    
    def processFiles() {
        def files = new File(dataDir + '/processed').eachFile {
            if (it.name.endsWith('.fits.xml')) {
                extractStats(it.name)
            }
        }
        
    }
    
    def extractStats(String fileName) {
        String filePath = "${dataDir}/processed/${fileName}"
        String cmd = [
            "xmlstarlet sel -N f=\"http://hul.harvard.edu/ois/xml/ns/fits/fits_output\"",  
            "-t -m 'f:fits/f:statistics/f:tool'",
            "-f -o ', \"'",
            "-v '@toolname' -o ' v'", 
            "-v '@toolversion' -o '\",'", 
            "-v '@executionTime' -n '${filePath}'",
            "| sed -e 's/^.*processed\\///'",
            " -e 's/\\.fits\\.xml//'"
        ].join(' ')
            
        
        //println cmd

        ProcessBuilder pb = new ProcessBuilder('/bin/bash', '-c', cmd)
        Process proc = pb.start()
        proc.waitFor()
        
        println proc.text
        /*
        // Obtain status and output
        println "return code: ${ proc.exitValue()}"
        println "stderr: ${proc.err.text}"
        println "stdout: ${proc.in.text}" */
    }
}