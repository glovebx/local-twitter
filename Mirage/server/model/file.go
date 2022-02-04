package model

import (
	"mime/multipart"
	"time"
)

type File struct {
	ID        string    `gorm:"primaryKey" json:"-"`
	PostId    string    `gorm:"not null;constraint:OnDelete:CASCADE;" json:"-"`
	Url       string    `json:"url"`
	FileType  string    `json:"filetype"`
	Filename  string    `json:"filename"`
	CreatedAt time.Time `json:"-"`
}

type FileRepository interface {
	UploadAvatar(header *multipart.FileHeader, directory string) (string, error)
	UploadBanner(header *multipart.FileHeader, directory string) (string, error)
	UploadFile(header *multipart.FileHeader, directory, filename, mimetype string) (string, error)
	DeleteImage(key string) error
}
