package main

import (
    "bufio"
    "bytes"
    "flag"
    "fmt"
    "github.com/panjf2000/ants"
    "net"
    "runtime"
    "strings"
    "time"
)

func req(resultChan chan int, requestId int, host string, port string, strLen *int, repeat int, intervalS int, randomIntervalMS int) {
    conn, err := net.Dial("tcp", fmt.Sprintf("%s:%s", host, port))

    if err != nil {
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
            resultChan <- -2
            conn.Close()
            return
        }

        nowReadLen := 0
        var data2receive []byte
        for nowReadLen < *strLen {
            nowRead, err := resp.ReadByte()

            if err != nil {
                fmt.Printf("Request #%d resp.ReadByte(): %s\n", requestId, err.Error())
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
                    resultChan <- -4
                    return
                }
                resultChan <- 0
                return
            }
        }

        if r < repeat {
            sleepTime := time.Duration(intervalS) * time.Second

            if randomIntervalMS > 0 {
                sleepTime = time.Duration(intervalS*1000+randomIntervalMS) * time.Millisecond
            }

            time.Sleep(sleepTime)
        }
    }

    conn.Close()
    resultChan <- 0
}

func main() {
    fmt.Println("Echo Client Use panjf2000/ants")
    host := flag.String("h", "127.0.0.1", "Host")
    port := flag.String("p", "12345", "Port")
    portsStr := flag.String("P", "", "Ports separated by comma")
    strLen := flag.Int("l", 32, "Random string length")
    concurrent := flag.Int("c", 10, "Concurrent")
    requestNum := flag.Int("n", 10, "Request number")
    intervalS := flag.Int("i", 0, "Send data after (i) second(s)")
    randomIntervalMS := flag.Int("R", 0, "Plus (R) ms when sleep")
    repeat := flag.Int("r", 1, "Repeatedly sending data for (r) times")
    flag.Parse()
    ports := strings.Split(*portsStr, ",")
    handled := 0
    handleSuccess := 0
    handleFail := 0
    portsIndex := 0

    if *requestNum < *concurrent {
        fmt.Printf("requestNum=%d < concurrent=%d, now set requestNum=%d\n", *requestNum, *concurrent, *concurrent)
        *requestNum = *concurrent
    }

    pool, err := ants.NewPool(*concurrent)

    if err != nil {
        return
    }

    resultChan := make(chan int, *requestNum)

    for i := 1; i <= *requestNum; i++ {
        tempPort := *port

        if len(*portsStr) > 0 {
            tempPort = ports[portsIndex]
            portsIndex = (portsIndex + 1) % len(ports)
        }

        pool.Submit(func() error {
            req(resultChan, i, *host, tempPort, strLen, *repeat, *intervalS, *randomIntervalMS)
            return nil
        })
    }

    for handled < *requestNum {
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
