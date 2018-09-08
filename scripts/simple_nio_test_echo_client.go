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
    "os"
    "syscall"
    "os/signal"
)

func getRandomString(strLen int) []byte {
    charTable := []byte("01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
    var result []byte
    r := rand.New(rand.NewSource(time.Now().UnixNano()))

    for i := 0; i < strLen; i++ {
        result = append(result, charTable[r.Intn(len(charTable))])
    }

    return result
}

func handle(concurrentChan chan int, resultChan chan int, requestId int, host string, port string, strLen *int, repeat int, intervalS int) {
    conn, err := net.Dial("tcp", fmt.Sprintf("%s:%s", host, port))
    if err != nil {
        _ = <-concurrentChan
        resultChan <- -1
        fmt.Printf("Request #%d net.Dial(): %s\n", requestId, err.Error())
        return
    }

    resp := bufio.NewReader(conn)

    for r := 0; r < repeat; r++ {
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
        var data2receive []byte
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
                if repeat > 1 {
                    break
                }

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

        if r < repeat {
            time.Sleep(time.Duration(intervalS) * time.Second)
        }
    }

    conn.Close()
    _ = <-concurrentChan
    resultChan <- 0
}

func doRequest(concurrentChan chan int, resultChan chan int, requestId int, host string, port string, strLen *int, repeat int, intervalS int) {
    handle(concurrentChan, resultChan, requestId, host, port, strLen, repeat, intervalS)
}

func main() {
    needExit := 0

    host := flag.String("h", "127.0.0.1", "Host")
    port := flag.String("p", "12345", "Port")
    portsStr := flag.String("P", "", "Ports separated by comma")
    strLen := flag.Int("l", 32, "Random string length")
    concurrent := flag.Int("c", 10, "Concurrent")
    requestNum := flag.Int("n", 10, "Request number")
    intervalS := flag.Int("i", 0, "Send data after (i) second(s)")
    repeat := flag.Int("r", 1, "Repeatedly sending data for (r) times")
    flag.Parse()
    ports := strings.Split(*portsStr, ",")

    fmt.Printf("host=%s port=%s strLen=%d concurrent=%d requestNum=%d portsStr=%s intervalS=%d repeat=%d\n", *host, *port, *strLen, *concurrent, *requestNum, *portsStr, *intervalS, *repeat)

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
    alreadySent := 0

    sigs := make(chan os.Signal, 1)
    signal.Notify(sigs, syscall.SIGINT)

    go func() {
        for ; needExit <= 1; {
            sig := <-sigs

            if sig == syscall.SIGINT {
                needExit++

                if needExit > 1 {
                    fmt.Printf("\nForce exit now!\n")
                    os.Exit(0)
                }

                fmt.Printf("\nSent %d requests and now need exit after finished request\n", alreadySent)
            }
        }
    }()

    go func() {
        for i := 1; i <= *requestNum; i++ {
            if needExit > 0 {
                break
            }

            tempPort := *port

            if len(*portsStr) > 0 {
                tempPort = ports[portsIndex]
                portsIndex = (portsIndex + 1) % len(ports)
            }

            concurrentChan <- i
            go doRequest(concurrentChan, resultChan, i, *host, tempPort, strLen, *repeat, *intervalS)
            alreadySent++
        }
    }()

    for ; handled < *requestNum; {
        if needExit > 0 && handled >= alreadySent {
            break
        }

        h := <-resultChan
        handled++

        if h < 0 {
            handleFail++
        } else {
            handleSuccess++
        }

        tenPercentNum := *requestNum / 10

        if (handled) > 0 && tenPercentNum > 0 && (handled)%tenPercentNum == 0 {
            fmt.Printf("Requested finished %d(success=%d fail=%d) with %d go routine\n", handled, handleSuccess, handleFail, runtime.NumGoroutine())
        }
    }

    fmt.Printf("Success:%d Fail:%d\n", handleSuccess, handleFail)
}
