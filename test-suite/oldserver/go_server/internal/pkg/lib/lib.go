package lib

import (
	"crypto/sha256"
	"encoding/base64"
	"gopkg.in/yaml.v2"
)

func GenChecksum(in interface{}) (string, error) {
	bb, err := yaml.Marshal(in)
	if err != nil {
		return "", err
	}

	data := sha256.Sum256(bb)
	hash := base64.StdEncoding.EncodeToString(data[:])

	return hash, nil
}
