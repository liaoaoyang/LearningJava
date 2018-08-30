package main

import (
    "flag"
    "math/rand"
    "time"
    "net"
    "fmt"
    "bufio"
    "runtime"
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

func handle(host string, port string, requestNum int, strLen *int, handled *int, handleSuccess *int, handleFail *int) {
    if requestNum <= *handled {
        return
    }

    tenPercentNum := requestNum / 10

    if (*handled) > 0 && (*handled)%tenPercentNum == 0 {
        fmt.Printf("Requested finished %d with %d go routine\n", *handled, runtime.NumGoroutine())
    }

    conn, err := net.Dial("tcp", fmt.Sprintf("%s:%s", host, port))

    if err != nil {
        fmt.Println("net.Dial(): " + err.Error())
        *handled++
        *handleFail++
        go handle(host, port, requestNum, strLen, handled, handleSuccess, handleFail)
        return
    }

    resp := bufio.NewReader(conn)

    wrote, err := conn.Write(getRandomString(*strLen))

    if err != nil {
        fmt.Println("conn.Write(): " + err.Error() + " and wrote " + string(wrote))
        *handled++
        *handleFail++
        conn.Close()
        go handle(host, port, requestNum, strLen, handled, handleSuccess, handleFail)
        return
    }

    nowReadLen := 0

    for ; nowReadLen < *strLen; {
        nowRead, err := resp.ReadByte()

        if err != nil {
            fmt.Println("resp.ReadByte(): " + err.Error())
            *handled++
            *handleFail++
            conn.Close()
            go handle(host, port, requestNum, strLen, handled, handleSuccess, handleFail)
            return
        }

        nowReadLen += len(string(nowRead))

        if nowReadLen >= *strLen {
            *handled++
            *handleSuccess++
            conn.Close()
            go handle(host, port, requestNum, strLen, handled, handleSuccess, handleFail)
            return
        }
    }
}

func main() {
    host := flag.String("h", "127.0.0.1", "Host")
    port := flag.String("p", "12345", "Port")
    strLen := flag.Int("l", 32, "Random string length")
    concurrent := flag.Int("c", 10, "Concurrent")
    requestNum := flag.Int("n", 10, "Request number")
    flag.Parse()

    fmt.Printf("host=%s port=%s strLen=%d concurrent=%d requestNum=%d\n", *host, *port, *strLen, *concurrent, *requestNum)

    handled := 0
    handleSuccess := 0
    handleFail := 0

    for i := 0; i < *concurrent; i++ {
        go handle(*host, *port, *requestNum, strLen, &handled, &handleSuccess, &handleFail)
    }

    for ; handled < *requestNum; {
        time.Sleep(1)
    }

    fmt.Printf("Success:%d Fail:%d\n", handleSuccess, handleFail)
}
