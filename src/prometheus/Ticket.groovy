package prometheus

import prometheus.utils.Bash

class Ticket implements Comparable<Ticket> {
    String id
    Status status
    static String suffix = '.zip'
    
    Ticket(String id, Status status) {
        this.id = id
        this.status = status
    }
    
    static String idFromFileName(String fileName) {
        if (!fileName.endsWith(suffix)) {
            throw new TicketException("Invalid file name '${fileName}' does not end with suffix '${suffix}'")
        }
        fileName - suffix    
    }
    
    static String idFromFile(File file) {
        idFromFileName(file.name)    
    }
    
    String getFileName() {
        id + suffix
    }
    
    void changeStatus(Status newStatus) {
        status = newStatus
    }
    
    String toString() {
        "${id} (${status})"
    }
    
    int compareTo(Ticket other) {
        this.id <=> other.id
    }
}
