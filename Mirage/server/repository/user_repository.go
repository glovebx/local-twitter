package repository

import (
	"errors"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"gorm.io/gorm"
	"log"
	"regexp"
	"strings"
)

// userRepository is data/repository implementation
// of service layer UserRepository
type userRepository struct {
	DB *gorm.DB
}

// NewUserRepository is a factory for initializing User Repositories
func NewUserRepository(db *gorm.DB) model.UserRepository {
	return &userRepository{
		DB: db,
	}
}

// FindByID returns a user for the given ID
func (r *userRepository) FindByID(id string) (*model.User, error) {
	user := &model.User{}

	// we need to actually check errors as it could be something other than not found
	if err := r.DB.Where("id = ?", id).First(&user).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return user, apperrors.NewNotFound("uid", id)
		}
		return user, apperrors.NewInternal()
	}

	return user, nil
}

func (r *userRepository) FindByEmail(email string) (*model.User, error) {
	user := &model.User{}

	// we need to actually check errors as it could be something other than not found
	if err := r.DB.Where("email = ?", email).First(&user).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return user, apperrors.NewNotFound("email", email)
		}
		return user, apperrors.NewInternal()
	}

	return user, nil
}

func (r *userRepository) FindByUsername(username string) (*model.User, error) {
	user := &model.User{}

	// we need to actually check errors as it could be something other than not found
	if err := r.DB.
		Preload("Followers").
		Preload("Followee").
		Where("LOWER(username) = ?", strings.ToLower(username)).
		First(&user).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return user, apperrors.NewNotFound("username", username)
		}
		return user, apperrors.NewInternal()
	}

	return user, nil
}

// Create inserts the user in the DB
func (r *userRepository) Create(user *model.User) (*model.User, error) {
	if result := r.DB.Create(&user); result.Error != nil {
		// check unique constraint
		if isDuplicateKeyError(result.Error) {
			if strings.Contains(result.Error.Error(), "email") {
				log.Printf("Could not create a user with email: %v. Reason: %v\n", user.Email, result.Error)
				return nil, apperrors.NewConflict("email")
			} else if strings.Contains(result.Error.Error(), "username") {
				log.Printf("Could not create a user with username: %v. Reason: %v\n", user.Username, result.Error)
				return nil, apperrors.NewConflict("username")
			}
		}

		log.Printf("Could not create a user with email: %v. Reason: %v\n", user.Email, result.Error)
		return nil, apperrors.NewInternal()
	}

	return user, nil
}

// Update updates the user in the DB
func (r *userRepository) Update(user *model.User) error {
	if result := r.DB.Save(&user); result.Error != nil {
		// check unique constraint
		if isDuplicateKeyError(result.Error) {
			if strings.Contains(result.Error.Error(), "email") {
				log.Printf("Could not update a user with email: %v. Reason: %v\n", user.Email, result.Error)
				return apperrors.NewConflict("email")
			} else if strings.Contains(result.Error.Error(), "username") {
				log.Printf("Could not create a user with username: %v. Reason: %v\n", user.Username, result.Error)
				return apperrors.NewConflict("username")
			}
		}

		log.Printf("Could not update a user with email: %v. Reason: %v\n", user.Email, result.Error)
		return apperrors.NewInternal()
	}

	return nil
}

func (r *userRepository) AddFollow(userId, currentId string) error {
	err := r.DB.Table("followers").Create(map[string]interface{}{
		"user_id":     userId,
		"follower_id": currentId,
	}).Table("followee").Create(map[string]interface{}{
		"followee_id": userId,
		"user_id":     currentId,
	}).Error
	return err
}

func (r *userRepository) RemoveFollow(userId, currentId string) error {
	err := r.DB.
		Exec("DELETE FROM followers WHERE user_id = ? AND follower_id = ?", userId, currentId).
		Exec("DELETE FROM followee WHERE followee_id = ? AND user_id = ?", userId, currentId).
		Error
	return err
}

func (r *userRepository) SearchProfiles(term string) (*[]model.User, error) {
	users := &[]model.User{}

	err := r.DB.
		Preload("Followers").
		Preload("Followee").
		Where("username ILIKE ?", "%"+strings.ToLower(term)+"%").
		Find(&users).Error

	return users, err
}

// isDuplicateKeyError checks if the provided error is a PostgreSQL duplicate key error
func isDuplicateKeyError(err error) bool {
	duplicate := regexp.MustCompile(`\(SQLSTATE 23505\)$`)
	return duplicate.MatchString(err.Error())
}
