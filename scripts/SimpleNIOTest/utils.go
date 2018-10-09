package SimpleNIOTest

import (
    "math/rand"
    "time"
)

func GetRandomString(strLen int) []byte {
    charTable := []byte("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
    var result []byte
    r := rand.New(rand.NewSource(time.Now().UnixNano()))

    for i := 0; i < strLen; i++ {
        result = append(result, charTable[r.Intn(len(charTable))])
    }

    return result
}
