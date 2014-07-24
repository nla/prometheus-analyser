package prometheus.fits

import prometheus.Workspace
import prometheus.Ticket
import prometheus.utils.*

class FITSRunner {
    Bash shell
    String fits = '/apps/fits/fits.sh'
    File contentPath
    Workspace workspace
    
    FITSRunner(Workspace workspace) {
        this.workspace = workspace
        shell = new Bash("${fits} -xc -i . -o .")
    }
    
    Map analyse(Ticket ticket) {
        shell.cd(workspace.getDir(ticket.id))
        shell.run { it }
    }
}
