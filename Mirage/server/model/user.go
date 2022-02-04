package model

import (
	"mime/multipart"
	"time"
)

type AccountResponse struct {
	ID          string    `json:"id"`
	Email       string    `json:"email"`
	Username    string    `json:"username"`
	DisplayName string    `json:"displayName"`
	Image       string    `json:"image"`
	Banner      *string   `json:"banner"`
	Bio         *string   `json:"bio"`
	CreatedAt   time.Time `json:"createdAt"`
}

func (user *User) NewAccountResponse() AccountResponse {
	return AccountResponse{
		ID:          user.ID,
		Username:    user.Username,
		DisplayName: user.DisplayName,
		Email:       user.Email,
		Image:       user.Image,
		Banner:      user.Banner,
		Bio:         user.Bio,
		CreatedAt:   user.CreatedAt,
	}
}

type Profile struct {
	ID          string    `json:"id"`
	Username    string    `json:"username"`
	DisplayName string    `json:"displayName"`
	Image       string    `json:"image"`
	Banner      *string   `json:"banner"`
	Bio         *string   `json:"bio"`
	Followers   uint      `json:"followers"`
	Followee    uint      `json:"followee"`
	Following   bool      `json:"following"`
	CreatedAt   time.Time `json:"createdAt"`
}

func (user *User) NewProfileResponse(id string) Profile {
	return Profile{
		ID:          user.ID,
		Username:    user.Username,
		DisplayName: user.DisplayName,
		Image:       user.Image,
		Banner:      user.Banner,
		Bio:         user.Bio,
		Followers:   uint(len(user.Followers)),
		Followee:    uint(len(user.Followee)),
		Following:   user.IsFollowing(id),
		CreatedAt:   user.CreatedAt,
	}
}

func (user *User) IsFollowing(id string) bool {
	if id == "" {
		return false
	}

	for _, v := range user.Followers {
		if v.ID == id {
			return true
		}
	}
	return false
}

type User struct {
	ID          string `gorm:"primaryKey"`
	Username    string `gorm:"not null;index;uniqueIndex"`
	DisplayName string `gorm:"not null;index"`
	Email       string `gorm:"not null;uniqueIndex"`
	Password    string `gorm:"not null" json:"-"`
	Image       string `gorm:"not null"`
	Banner      *string
	Bio         *string
	CreatedAt   time.Time
	UpdatedAt   time.Time
	Posts       []Post
	Followers   []*User `gorm:"many2many:followers" json:"-"`
	Followee    []*User `gorm:"many2many:followee" json:"-"`
}

type UserService interface {
	Get(uid string) (*User, error)
	FindByUsername(username string) (*User, error)
	Register(user *User) (*User, error)
	Login(email, password string) (*User, error)
	Update(user *User) error
	ChangeAvatar(header *multipart.FileHeader, directory string) (string, error)
	ChangeBanner(header *multipart.FileHeader, directory string) (string, error)
	DeleteImage(key string) error
	ChangeFollow(user *User, current string) error
	Search(term string) (*[]User, error)
}

type UserRepository interface {
	FindByID(uid string) (*User, error)
	FindByEmail(email string) (*User, error)
	FindByUsername(username string) (*User, error)
	Create(user *User) (*User, error)
	Update(user *User) error
	AddFollow(userId, currentId string) error
	RemoveFollow(userId, currentId string) error
	SearchProfiles(term string) (*[]User, error)
}
