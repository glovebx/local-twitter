package fixture

import (
	"github.com/sentrionic/mirage/model"
	"time"
)

func GetMockPost() *model.Post {
	text := RandStringRunes(60)
	return &model.Post{
		ID:        RandID(),
		Text:      &text,
		UserID:    RandID(),
		CreatedAt: time.Now(),
	}
}

func GetMockFile(id string) *model.File {
	return &model.File{
		ID:        RandID(),
		PostId:    id,
		Url:       RandStringRunes(10),
		FileType:  "image/jpeg",
		Filename:  RandStringRunes(10),
		CreatedAt: time.Now(),
	}
}
