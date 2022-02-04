package repository

import (
	"database/sql"
	"errors"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"gorm.io/gorm"
	"log"
	"strings"
)

// postRepository is data/repository implementation
// of service layer PostRepository
type postRepository struct {
	DB *gorm.DB
}

// NewPostRepository is a factory for initializing Post Repositories
func NewPostRepository(db *gorm.DB) model.PostRepository {
	return &postRepository{
		DB: db,
	}
}

// FindByID returns a post for the given ID
func (r *postRepository) FindByID(id string) (*model.Post, error) {
	post := &model.Post{}

	// we need to actually check errors as it could be something other than not found
	if err := r.DB.
		Preload("Likes").
		Preload("Retweets").
		Preload("File").
		Preload("User.Followers").
		Preload("User.Followers").
		Where("id = ?", id).
		First(&post).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return post, apperrors.NewNotFound("id", id)
		}
		return post, apperrors.NewInternal()
	}

	return post, nil
}

// Create inserts the post in the DB
func (r *postRepository) Create(post *model.Post) (*model.Post, error) {
	if result := r.DB.Create(&post); result.Error != nil {
		log.Printf("Could not create a post for author: %v. Reason: %v\n", post.UserID, result.Error)
		return nil, apperrors.NewInternal()
	}

	return post, nil
}

func (r *postRepository) Delete(post *model.Post) error {
	return r.DB.Delete(&post).Error
}

func (r *postRepository) AddLike(post *model.Post, uid string) error {
	err := r.DB.Table("post_likes").
		Create(map[string]interface{}{
			"user_id": uid,
			"post_id": post.ID,
		}).Error
	return err
}

func (r *postRepository) RemoveLike(post *model.Post, uid string) error {
	err := r.DB.
		Exec("DELETE FROM post_likes WHERE user_id = ? AND post_id = ?", uid, post.ID).
		Error
	return err
}

func (r *postRepository) AddRetweet(post *model.Post, uid string) error {
	err := r.DB.Table("retweets").
		Create(map[string]interface{}{
			"user_id": uid,
			"post_id": post.ID,
		}).Error
	return err
}

func (r *postRepository) RemoveRetweet(post *model.Post, uid string) error {
	err := r.DB.
		Exec("DELETE FROM retweets WHERE user_id = ? AND post_id = ?", uid, post.ID).
		Error
	return err
}

func (r *postRepository) Feed(userId, cursor string) (*[]model.Post, error) {
	var posts []model.Post
	query := r.DB.
		Preload("Likes").
		Preload("Retweets").
		Preload("File").
		Preload("User.Followers").
		Preload("User.Followers").
		Joins("LEFT JOIN followers on \"posts\".user_id = followers.user_id").
		Joins("LEFT JOIN retweets r on \"posts\".id = r.post_id").
		Where(`
			("posts".user_id = @id
			OR followers.follower_id = @id
			OR r.user_id = @id
			OR r.user_id IN (
				SELECT id
				from "users"
				join followee f on "users".id = f.followee_id
				WHERE f.user_id = @id
			))
		`, sql.Named("id", userId))

	if cursor != "" {
		cursor = strings.Replace(cursor, " ", "+", 1)
		query.
			Where("\"posts\".created_at::timestamptz < ?", cursor)
	}

	query.
		Order("(CASE when r.created_at notnull THEN r.created_at ELSE \"posts\".created_at end) DESC").
		Limit(model.LIMIT + 1).
		Find(&posts)

	return &posts, query.Error
}

func (r *postRepository) List(id, cursor string) (*[]model.Post, error) {
	var posts []model.Post

	query := r.DB.
		Preload("Likes").
		Preload("Retweets").
		Preload("File").
		Preload("User.Followers").
		Preload("User.Followers").
		Joins("LEFT JOIN retweets r on \"posts\".id = r.post_id").
		Where("(\"posts\".user_id = @id OR r.user_id IN (SELECT id from \"users\" join followee f on \"users\".id = f.followee_id WHERE f.user_id = @id))", sql.Named("id", id))

	if cursor != "" {
		cursor = strings.Replace(cursor, " ", "+", 1)
		query.
			Where("\"posts\".created_at::timestamptz < ?", cursor)
	}

	query.
		Order("(CASE when r.created_at notnull THEN r.created_at ELSE \"posts\".created_at end) DESC").
		Limit(model.LIMIT + 1).
		Find(&posts)

	return &posts, query.Error
}

func (r *postRepository) Likes(id, cursor string) (*[]model.Post, error) {
	var posts []model.Post

	query := r.DB.
		Preload("Likes").
		Preload("Retweets").
		Preload("File").
		Preload("User.Followers").
		Preload("User.Followers").
		Joins("LEFT JOIN post_likes pl on \"posts\".id = pl.post_id").
		Where("pl.user_id = ?", id)

	if cursor != "" {
		cursor = strings.Replace(cursor, " ", "+", 1)
		query.
			Where("created_at::timestamptz < ?", cursor)
	}

	query.
		Order("created_at DESC").
		Limit(model.LIMIT + 1).
		Find(&posts)

	return &posts, query.Error
}

func (r *postRepository) GetPostsForHashtag(term, cursor string) (*[]model.Post, error) {
	posts := &[]model.Post{}

	if !strings.HasPrefix(term, "#") {
		term = "#" + term
	}

	query := r.DB.
		Preload("Likes").
		Preload("Retweets").
		Preload("File").
		Preload("User.Followers").
		Preload("User.Followers").
		Joins("LEFT JOIN users u ON u.id = \"posts\".user_id").
		Where("@term ILIKE ANY (\"posts\".hash_tags)", sql.Named("term", strings.ToLower(term)))

	if cursor != "" {
		cursor = strings.Replace(cursor, " ", "+", 1)
		query.Where("created_at::timestamptz < ?", cursor)
	}

	query.
		Order("created_at DESC").
		Limit(model.LIMIT + 1).
		Find(&posts)

	return posts, query.Error
}

func (r *postRepository) Media(id, cursor string) (*[]model.Post, error) {
	var posts []model.Post

	query := r.DB.
		Preload("Likes").
		Preload("Retweets").
		Preload("File").
		Preload("User.Followers").
		Preload("User.Followers").
		Joins("LEFT JOIN files f on \"posts\".id = f.post_id").
		Where("\"posts\".user_id = ? AND f IS NOT NULL", id)

	if cursor != "" {
		cursor = strings.Replace(cursor, " ", "+", 1)
		query.
			Where("created_at::timestamptz < ?", cursor)
	}

	query.
		Order("created_at DESC").
		Limit(model.LIMIT + 1).
		Find(&posts)

	return &posts, query.Error
}
