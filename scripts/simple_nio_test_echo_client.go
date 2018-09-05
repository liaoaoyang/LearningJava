package main

import (
    "flag"
    "math/rand"
    "time"
    "net"
    "fmt"
    "bufio"
    "runtime"
    "bytes"
    "strings"
)

func getRandomString(strLen int) []byte {
    bytes := []byte("01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
    var result []byte
    r := rand.New(rand.NewSource(time.Now().UnixNano()))

    for i := 0; i < strLen; i++ {
        result = append(result, bytes[r.Intn(len(bytes))])
    }

    return result
}

func handle(concurrentChan chan int, resultChan chan int, requestId int, host string, port string, strLen *int) {
    conn, err := net.Dial("tcp", fmt.Sprintf("%s:%s", host, port))
    if err != nil {
        _ = <-concurrentChan
        resultChan <- -1
        fmt.Printf("Request #%d net.Dial(): %s\n", requestId, err.Error())
        return
    }

    resp := bufio.NewReader(conn)
    data2send := getRandomString(*strLen)

    wrote, err := conn.Write(data2send)

    if err != nil {
        fmt.Printf("Request #%d conn.Write(): %s and wrote %s\n", requestId, err.Error(), string(wrote))
        _ = <-concurrentChan
        resultChan <- -2
        conn.Close()
        return
    }

    nowReadLen := 0
    var data2receive []byte;
    for ; nowReadLen < *strLen; {
        nowRead, err := resp.ReadByte()

        if err != nil {
            fmt.Printf("Request #%d resp.ReadByte(): %s\n", requestId, err.Error())
            _ = <-concurrentChan
            resultChan <- -3
            conn.Close()
            return
        }

        nowReadLen += len(string(nowRead))
        data2receive = append(data2receive, nowRead)

        if nowReadLen >= *strLen {
            conn.Close()
            if bytes.Compare(data2send, data2receive) != 0 {
                fmt.Printf("Request #%d Read/Write data mismatch\n", requestId)
                _ = <-concurrentChan
                resultChan <- -4
                return
            }
            _ = <-concurrentChan
            resultChan <- 0
            return
        }
    }
}

func doRequest(concurrentChan chan int, resultChan chan int, requestId int, host string, port string, strLen *int) {
    go handle(concurrentChan, resultChan, requestId, host, port, strLen)
}

func main() {
    host := flag.String("h", "127.0.0.1", "Host")
    port := flag.String("p", "12345", "Port")
    portsStr := flag.String("P", "", "Ports separated by comma")
    strLen := flag.Int("l", 32, "Random string length")
    concurrent := flag.Int("c", 10, "Concurrent")
    requestNum := flag.Int("n", 10, "Request number")
    flag.Parse()
    ports := strings.Split(*portsStr, ",")

    fmt.Printf("host=%s port=%s strLen=%d concurrent=%d requestNum=%d portsStr=%s\n", *host, *port, *strLen, *concurrent, *requestNum, *portsStr)

    handled := 0
    handleSuccess := 0
    handleFail := 0
    portsIndex := 0

    if *requestNum < *concurrent {
        fmt.Printf("requestNum=%d < concurrent=%d, now set requestNum=%d\n", *requestNum, *concurrent, *concurrent)
        *requestNum = *concurrent
    }

    concurrentChan := make(chan int, *concurrent)
    resultChan := make(chan int, *requestNum)

    for i := 1; i <= *requestNum; i++ {
        tempPort := *port

        if len(*portsStr) > 0 {
            tempPort = ports[portsIndex]
            portsIndex = (portsIndex + 1) % len(ports)
        }

        concurrentChan <- i
        go doRequest(concurrentChan, resultChan, i, *host, tempPort, strLen)
    }

    for ; handled < *requestNum; {
        h := <-resultChan
        handled++

        if h < 0 {
            handleFail++
        } else {
            handleSuccess++
        }

        tenPercentNum := *requestNum / 10

        if (handled) > 0 && (handled)%tenPercentNum == 0 {
            fmt.Printf("Requested finished %d(success=%d fail=%d) with %d go routine\n", handled, handleSuccess, handleFail, runtime.NumGoroutine())
        }
    }

    fmt.Printf("Success:%d Fail:%d\n", handleSuccess, handleFail)
}
