package prometheus

def analyser = new Analyser('/data/prometheus/analysis')

analyser.setZipsDirPath('/media/BrutusBackup/PrometheusContent')

//a.populateQueue()
analyser.loadTickets()

analyser.run()

//println System.getProperty('java.class.path')

/* Step through the whole process for a single ticket

Ticket ticket = analyser.getNextQueued()

println ticket

analyser.processTicket(ticket)
*/

/* Test an individual step
Ticket ticket = analyser.tickets.getTicket("nla.dp-n000000000185724-c")

analyser.harvestAnalysisData(ticket)
*/
