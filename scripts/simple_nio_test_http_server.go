package main

import (
    "io"
    "net/http"
    "log"
    "os"
    "strings"
    "fmt"
)

// hello world, the web server
func HelloServer(w http.ResponseWriter, req *http.Request) {
    io.WriteString(w, "Hello, cyber traveller from "+req.RemoteAddr+"\n")
}

func serve(hostNPort string) {
    fmt.Println("Try serve on " + hostNPort)
    err := http.ListenAndServe(hostNPort, nil)
    if err != nil {
        log.Fatal("ListenAndServe: ", err)
    }
}

func main() {
    http.HandleFunc("/", HelloServer)
    hostNPorts := "127.0.0.1:12345"

    if len(os.Args) >= 2 {
        hostNPorts = os.Args[1]
    }

    hostNPortsArray := strings.Split(hostNPorts, ",")

    for index, hostNPort := range hostNPortsArray {
        if index < len(hostNPortsArray)-1 {
            go serve(hostNPort)
        } else {
            serve(hostNPort)
        }
    }
}
