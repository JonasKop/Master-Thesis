package main

import (
	"fmt"
	"github.com/JonasKop/Master-Thesis.git/internal/pkg/config"
)

func main() {
	conf, err := config.Load()
	if err != nil {
		panic(err)
	}
	fmt.Println(conf)
}

