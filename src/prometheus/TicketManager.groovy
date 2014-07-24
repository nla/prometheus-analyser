package prometheus

import java.util.List;
import java.util.Map;
import java.util.TreeMap

import prometheus.utils.DirectoryManager

class TicketManager extends DirectoryManager {
    Map ticketsByStatus
    SortedMap tickets
    
    TicketManager(String baseDir) {
        super(baseDir, 'tickets')
        init()
    }

    void init() {
        Status.each {
            mkdir(it.value)
        }
        
        initTicketRegister()
    }

    void initTicketRegister() {
        tickets = new TreeMap()
        ticketsByStatus = [:]
        
        Status.each {
            ticketsByStatus[it] = new TreeSet()
        }
    }   
     
    /**
     * Reset the StatusManager's file system memory back to initial state
     * 
     * This method resets the StatusManager's file system memory back to its
     * pristine initial state, i.e. with all status folders empty except
     * QUEUED, which contains all the tickets. 
     */
    void clearSavedTickets() {
        Status.each { status ->
            clearDir(status.value)
        }
    }
    
    /**
     * Create a new ticket
     * 
     * This method creates a new Ticket object with the given ID and status. It then
     * registers the ticket in the 'tickets' map and creates a file with name the same
     * as the ID in the corresponding status directory (the directory named ${status.value}).
     * @param ID
     * @param status
     */
    Ticket createTicket(String ID, Status status) {
        Ticket ticket = new Ticket(ID, status)
        registerTicket(ticket)
        saveTicket(ticket)
        ticket
    }
    
    /**
     * Reconstruct the in-memory representation of a ticket from the file system
     * 
     * This method 'recovers' a ticket which already has a representation in the
     * file system. For example if the ID is 'nla.dp-0000012345-c' and the status
     * is Status.UNZIPPING then a Ticket object with those values will be created
     * and registered in the 'tickets' map but the filesystem will be left as is.
     * @param ID
     * @param status
     */
    void loadTicket(String id, Status status) {
        Ticket ticket = new Ticket(id, status)
        registerTicket(ticket)
    }
    
    void loadTickets() {
        Status.each { status ->
            loadTickets(status)
        }
    }

    void loadTickets(Status status) {
        List ids = getSavedTicketIDs(status)
        ids.each {
            loadTicket(it, status)
        }
    }
    
    Ticket getTicket(String id) {
        Ticket ticket = tickets[id] 
        if (ticket == null) {
            throw new TicketException("No ticket exists with ID '${id}'")
        }
        
        ticket
    }
    
    void registerTicket(Ticket ticket) {
        tickets[ticket.id] = ticket
        ticketsByStatus[ticket.status] << ticket
    }
    
    void saveTicket(Ticket ticket) {
        createFile("${ticket.status.value}/${ticket.id}")
    }
    
    void saveTickets() {
        tickets.values().each {
            saveTicket(it)
        }
    }
    
    List getTickets(Status status) {
        ticketsByStatus[status] as List
    }
    
    /**
     * Move a ticket from one status to another
     * 
     * This method affects the Ticket object, the tickets map and the filesystem to
     * reflect the new status.
     * @param ticket
     * @param newStatus
     */
    void moveTicket(Ticket ticket, Status newStatus) {
        Status oldStatus = ticket.status
        moveFile(ticket.id, oldStatus.value, newStatus.value)
        ticketsByStatus[newStatus] << ticket
        ticketsByStatus[oldStatus] -= ticket
        ticket.changeStatus(newStatus)
    }
    
    Ticket getNextTicket(Status status) {
        ticketsByStatus[status][0]    
    }
    
    List getSavedTicketIDs(Status status) {
        getFiles(status.value) { it.isFile() }.collect { it.name }
    }

    
    String toString() {
        StringWriter out = new StringWriter()
        
        out.println "STATUS MANAGER [${homePath}]"
        out.println ''
        
        Status.each { status -> 
            out.println status
            out.println getTickets(status)
        }
        
        out as String
    }
}