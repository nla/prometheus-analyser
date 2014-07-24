package prometheus

import org.apache.log4j.*
import org.apache.log4j.spi.RootLogger
import groovy.util.logging.*
import org.apache.commons.io.FileUtils
import prometheus.fits.FITSRunner
import prometheus.utils.*

@Log4j
class Analyser {
    String analysisDirPath
    DirectoryManager dirManager
    String zipsDirPath
    Workspace workspace
    TicketManager tickets
    ZipFileManager zipFiles
    FITSRunner fitsRunner
    Zipper zipper
    Bash shell
    
    Analyser(String analysisDirPath) {
        this.analysisDirPath = analysisDirPath
        init()
    }
    
    void init() {
        zipper = new Zipper()
        shell = new Bash()
        dirManager = new DirectoryManager(analysisDirPath)
        dirManager.mkdir('logs')
        dirManager.mkdir('results')
        workspace = new Workspace(analysisDirPath)
        tickets = new TicketManager(analysisDirPath)
        fitsRunner = new FITSRunner(workspace)
        initLogging()
    }
    
    void initLogging() {
        RootLogger root = Logger.getRootLogger()
        PatternLayout layout = new PatternLayout();
        String conversionPattern = "[%-5p] %d{MM-dd-yyyy HH:mm:ss} %c - %m%n";
        layout.setConversionPattern(conversionPattern);
        root.addAppender(new FileAppender(layout, dirManager.getPath('logs/analysis.log')));
        root.setLevel(Level.ALL)
        
        log.info '==== Starting Analysis Engine ===='
    }
    
    void setZipsDirPath(String zipsDirPath) {
        this.zipsDirPath = zipsDirPath
        zipFiles = new ZipFileManager(zipsDirPath)
    }
    
    void populateQueue() {
        List<String> fileNames = zipFiles.getZipFileNames()
        List<String> ticketIds = fileNames.collect { it - '.zip' }
        log.info "Found ${ticketIds.size()} ZIP files to queue"
        ticketIds.each {
            tickets.createTicket(it, Status.QUEUED)
        }
    } 
    
    void loadTickets() {
        tickets.loadTickets()
    }
    
    Ticket getNextQueued() {
        tickets.getNextTicket(Status.QUEUED)    
    }
    
    void copyZipFileToWorkspace(Ticket ticket) {
        File dest = workspace.getHomeDir()
        String zipFileName = ticket.fileName
        File zipFile = new File(zipsDirPath, zipFileName)
        
        log.info("Copying file '${zipFileName}' to workspace")
        try {
            tickets.moveTicket(ticket, Status.COPYING)
            FileUtils.copyFileToDirectory(zipFile, dest)
            tickets.moveTicket(ticket, Status.COPIED)
        } catch(IOException e) {
            log.error "Failed to copy ${zipFileName} to workspace directory ${dest.canonicalPath}. Original error was IOException '${e.message}'"
            tickets.moveTicket(ticket, Status.ERROR)
        } catch(TicketException e) {
            log.error "Failed to copy ${zipFileName} to workspace directory ${dest.canonicalPath}. Original error was TicketError '${e.message}'"
            tickets.moveTicket(ticket, Status.ERROR)
        }
    }
        
    void extractZipFile(Ticket ticket) {
        tickets.moveTicket(ticket, Status.UNZIPPING)
        try {
            File zipFile = workspace.getFile(ticket.fileName)
            zipper.unzip(zipFile, workspace.homeDir)
            tickets.moveTicket(ticket, Status.UNZIPPED)
            log.info "Unzipped file ${zipFile.name}"
        } catch(Exception e) {
            log.error "Failed to unzip file '${ticket.fileName}'"
            tickets.moveTicket(ticket, Status.ERROR)
            throw e
        }
            
    }
    
    void analyseContent(Ticket ticket) {
        tickets.moveTicket(ticket, Status.ANALYSING)
        Map result = fitsRunner.analyse(ticket)
        if (result.status != 0) {
            log.error "FITS analysis failed for ticket ${ticket.id}. Original error was:\n'${result.err}'"
            tickets.moveTicket(ticket, Status.ERROR)
        } else {
            log.info "FITS analysis succeeded for ticket ${ticket.id}"
            tickets.moveTicket(ticket, Status.ANALYSED)
        }
    }
    
    void harvestAnalysisData(Ticket ticket) {
        shell.cd(workspace.getDir(ticket.id))
        String destDirName = "results/${ticket.id}"
        File destDir = dirManager.mkdir(destDirName)
        shell.cmd = "mv *.fits.xml ${destDir.canonicalPath}"
        
        tickets.moveTicket(ticket, Status.HARVESTING)
        
        shell.run {
            if (it.status != 0) {
                log.error "Data harvesting: '${shell.cmd}' failed with error:\n'${it.err}'"
                tickets.moveTicket(ticket, Status.ERROR)
            } else {
                log.info "Data harvested for ticket ${ticket.id}"
                tickets.moveTicket(ticket, Status.COMPLETE)
            }
        }
    }
    

    void run() {
        Ticket ticket 
        while(ticket = getNextQueued()) {
            processTicket(ticket)
        }
        
    }
    
    void processTicket(Ticket ticket) {
        workspace.clear()
        copyZipFileToWorkspace(ticket)
        extractZipFile(ticket)
        analyseContent(ticket)
        harvestAnalysisData(ticket)
    }
}
