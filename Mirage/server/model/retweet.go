package model

import "time"

type Retweet struct {
	UserID    string    `gorm:"primaryKey;constraint:OnDelete:CASCADE;"`
	PostId    string    `gorm:"primaryKey;constraint:OnDelete:CASCADE;"`
	CreatedAt time.Time `gorm:"index;default:now()"`
}
