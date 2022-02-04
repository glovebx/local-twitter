package model

import (
	"github.com/lib/pq"
	"mime/multipart"
	"time"
)

type PostResponse struct {
	ID        string    `json:"id"`
	Text      *string   `json:"text"`
	Likes     uint      `json:"likes"`
	Liked     bool      `json:"liked"`
	Retweets  uint      `json:"retweets"`
	Retweeted bool      `json:"retweeted"`
	IsRetweet bool      `json:"isRetweet"`
	File      *File     `json:"file"`
	Author    Profile   `json:"author"`
	CreatedAt time.Time `json:"createdAt"`
}

func (post *Post) NewPostResponse(id string) PostResponse {
	return PostResponse{
		ID:        post.ID,
		Text:      post.Text,
		Likes:     uint(len(post.Likes)),
		Liked:     post.IsLiked(id),
		Retweets:  uint(len(post.Retweets)),
		Retweeted: post.IsRetweeted(id),
		File:      post.File,
		Author:    post.User.NewProfileResponse(id),
		CreatedAt: post.CreatedAt,
	}
}

func (post *Post) NewFeedResponse(id string) PostResponse {
	return PostResponse{
		ID:        post.ID,
		Text:      post.Text,
		Likes:     uint(len(post.Likes)),
		Liked:     post.IsLiked(id),
		Retweets:  uint(len(post.Retweets)),
		Retweeted: post.IsRetweeted(id),
		IsRetweet: post.UserID != id && !post.User.IsFollowing(id),
		File:      post.File,
		Author:    post.User.NewProfileResponse(id),
		CreatedAt: post.CreatedAt,
	}
}

func (post *Post) IsLiked(id string) bool {
	if id == "" {
		return false
	}

	for _, v := range post.Likes {
		if v.ID == id {
			return true
		}
	}
	return false
}

func (post *Post) IsRetweeted(id string) bool {
	if id == "" {
		return false
	}

	for _, v := range post.Retweets {
		if v.ID == id {
			return true
		}
	}
	return false
}

type Post struct {
	ID        string `gorm:"primaryKey"`
	Text      *string
	File      *File          `gorm:"constraint:OnDelete:CASCADE;"`
	HashTags  pq.StringArray `gorm:"type:text[]"`
	UserID    string         `gorm:"not null;constraint:OnDelete:CASCADE;"`
	User      User           `gorm:"not null;constraint:OnDelete:CASCADE;"`
	Likes     []User         `gorm:"many2many:post_likes;constraint:OnDelete:CASCADE;"`
	Retweets  []User         `gorm:"many2many:retweets;constraint:OnDelete:CASCADE;"`
	CreatedAt time.Time      `gorm:"index"`
}

type PostService interface {
	FindPostByID(id string) (*Post, error)
	CreatePost(post *Post) (*Post, error)
	DeletePost(post *Post) error
	UploadFile(header *multipart.FileHeader) (*File, error)
	ToggleLike(post *Post, uid string) error
	ToggleRetweet(post *Post, uid string) error
	GetUserFeed(userId, cursor string) (*[]Post, error)
	ProfilePosts(id, cursor string) (*[]Post, error)
	ProfileLikes(id, cursor string) (*[]Post, error)
	ProfileMedia(id, cursor string) (*[]Post, error)
	SearchPosts(tag, cursor string) (*[]Post, error)
}

type PostRepository interface {
	FindByID(id string) (*Post, error)
	Create(post *Post) (*Post, error)
	Delete(post *Post) error
	AddLike(post *Post, uid string) error
	RemoveLike(post *Post, uid string) error
	AddRetweet(post *Post, uid string) error
	RemoveRetweet(post *Post, uid string) error
	Feed(userId, cursor string) (*[]Post, error)
	List(id, cursor string) (*[]Post, error)
	Likes(id, cursor string) (*[]Post, error)
	GetPostsForHashtag(tag, cursor string) (*[]Post, error)
	Media(id, cursor string) (*[]Post, error)
}
