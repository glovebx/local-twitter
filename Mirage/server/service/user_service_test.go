package service

import (
	"fmt"
	"github.com/sentrionic/mirage/mocks"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/stretchr/testify/mock"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestGet(t *testing.T) {
	t.Run("Success", func(t *testing.T) {
		uid, _ := GenerateId()
		mockUser := fixture.GetMockUser()
		mockUser.ID = uid

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})
		mockUserRepository.On("FindByID", uid).Return(mockUser, nil)

		u, err := us.Get(uid)

		assert.NoError(t, err)
		assert.Equal(t, u, mockUser)
		mockUserRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		uid, _ := GenerateId()

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})

		mockUserRepository.On("FindByID", uid).Return(nil, fmt.Errorf("some error down the call chain"))

		u, err := us.Get(uid)

		assert.Nil(t, u)
		assert.Error(t, err)
		mockUserRepository.AssertExpectations(t)
	})
}

func TestUserService_FindByUsername(t *testing.T) {
	t.Run("Success", func(t *testing.T) {
		mockUser := fixture.GetMockUser()

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})
		mockUserRepository.On("FindByUsername", mockUser.Username).Return(mockUser, nil)

		u, err := us.FindByUsername(mockUser.Username)

		assert.NoError(t, err)
		assert.Equal(t, u, mockUser)
		mockUserRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		username := fixture.Username()

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})

		mockUserRepository.On("FindByUsername", username).Return(nil, fmt.Errorf("some error down the call chain"))

		u, err := us.FindByUsername(username)

		assert.Nil(t, u)
		assert.Error(t, err)
		mockUserRepository.AssertExpectations(t)
	})
}

func TestRegister(t *testing.T) {
	t.Run("Success", func(t *testing.T) {
		uid, _ := GenerateId()
		mockUser := fixture.GetMockUser()

		initial := &model.User{
			Username:    mockUser.Username,
			Email:       mockUser.Email,
			Password:    mockUser.Password,
			DisplayName: mockUser.DisplayName,
		}

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})

		// We can use Run method to modify the user when the Create method is called.
		//  We can then chain on a Return method to return no error
		mockUserRepository.
			On("Create", initial).
			Run(func(args mock.Arguments) {
				mockUser.ID = uid
			}).Return(mockUser, nil)

		user, err := us.Register(initial)

		assert.NoError(t, err)

		// assert user now has a userID
		assert.Equal(t, uid, mockUser.ID)
		assert.Equal(t, user, mockUser)

		mockUserRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		mockUser := &model.User{
			Email:       "bob@bob.com",
			Username:    "bobby",
			DisplayName: "bob bob",
			Password:    "howdyhoneighbor!",
		}

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})

		mockErr := apperrors.NewConflict("email")

		// We can use Run method to modify the user when the Create method is called.
		//  We can then chain on a Return method to return no error
		mockUserRepository.
			On("Create", mockUser).
			Return(nil, mockErr)

		user, err := us.Register(mockUser)

		// assert error is error we response with in mock
		assert.EqualError(t, err, mockErr.Error())
		assert.Nil(t, user)

		mockUserRepository.AssertExpectations(t)
	})
}

func TestLogin(t *testing.T) {
	// setup valid email/pw combo with hashed password to test method
	// response when provided password is invalid
	validPW := "howdyhoneighbor!"
	hashedValidPW, _ := hashPassword(validPW)
	invalidPW := "howdyhodufus!"

	mockUserRepository := new(mocks.UserRepository)
	us := NewUserService(&USConfig{
		UserRepository: mockUserRepository,
	})

	t.Run("Success", func(t *testing.T) {
		mockUser := fixture.GetMockUser()
		mockUser.Password = hashedValidPW

		mockUserRepository.
			On("FindByEmail", mockUser.Email).Return(mockUser, nil)

		user, err := us.Login(mockUser.Email, validPW)

		assert.NoError(t, err)
		assert.Equal(t, user, mockUser)
		mockUserRepository.AssertCalled(t, "FindByEmail", mockUser.Email)
	})

	t.Run("Invalid email/password combination", func(t *testing.T) {
		uid, _ := GenerateId()
		email := "email@example.com"

		mockUserResp := &model.User{
			ID:       uid,
			Email:    email,
			Password: hashedValidPW,
		}

		mockArgs := mock.Arguments{
			email,
		}

		// We can use Run method to modify the user when the Create method is called.
		//  We can then chain on a Return method to return no error
		mockUserRepository.
			On("FindByEmail", mockArgs...).Return(mockUserResp, nil)

		user, err := us.Login(email, invalidPW)

		assert.Error(t, err)
		assert.EqualError(t, err, "Invalid email and password combination")
		assert.Nil(t, user)
		mockUserRepository.AssertCalled(t, "FindByEmail", mockArgs...)
	})
}

func TestUpdateDetails(t *testing.T) {
	mockUserRepository := new(mocks.UserRepository)
	us := NewUserService(&USConfig{
		UserRepository: mockUserRepository,
	})

	t.Run("Success", func(t *testing.T) {
		uid, _ := GenerateId()

		mockUser := fixture.GetMockUser()
		mockUser.ID = uid

		mockArgs := mock.Arguments{
			mockUser,
		}

		mockUserRepository.
			On("Update", mockArgs...).Return(nil)

		err := us.Update(mockUser)

		assert.NoError(t, err)
		mockUserRepository.AssertCalled(t, "Update", mockArgs...)
	})

	t.Run("Failure", func(t *testing.T) {
		uid, _ := GenerateId()

		mockUser := &model.User{
			ID: uid,
		}

		mockArgs := mock.Arguments{
			mockUser,
		}

		mockError := apperrors.NewInternal()

		mockUserRepository.
			On("Update", mockArgs...).Return(mockError)

		err := us.Update(mockUser)
		assert.Error(t, err)

		apperror, ok := err.(*apperrors.Error)
		assert.True(t, ok)
		assert.Equal(t, apperrors.Internal, apperror.Type)

		mockUserRepository.AssertCalled(t, "Update", mockArgs...)
	})
}

func TestUserService_ChangeAvatar(t *testing.T) {
	mockUserRepository := new(mocks.UserRepository)
	mockFileRepository := new(mocks.FileRepository)

	us := NewUserService(&USConfig{
		UserRepository: mockUserRepository,
		FileRepository: mockFileRepository,
	})

	t.Run("Successful new image", func(t *testing.T) {
		uid, _ := GenerateId()

		// does not have have imageURL
		mockUser := fixture.GetMockUser()
		mockUser.ID = uid
		mockUser.Image = ""

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "test_dir"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
		}

		imageURL := "https://imageurl.com/jdfkj34kljl"

		mockFileRepository.
			On("UploadAvatar", uploadFileArgs...).
			Return(imageURL, nil)

		updateArgs := mock.Arguments{
			mockUser,
		}

		mockUpdatedUser := &model.User{
			ID:          uid,
			Email:       mockUser.Email,
			Bio:         mockUser.Bio,
			Username:    mockUser.Username,
			DisplayName: mockUser.DisplayName,
			Image:       imageURL,
			Password:    mockUser.Password,
			CreatedAt:   mockUser.CreatedAt,
			UpdatedAt:   mockUser.UpdatedAt,
		}

		mockUserRepository.
			On("Update", updateArgs...).
			Return(nil)

		url, err := us.ChangeAvatar(imageFileHeader, directory)
		assert.NoError(t, err)
		mockUser.Image = url

		err = us.Update(mockUser)

		assert.NoError(t, err)
		assert.Equal(t, mockUpdatedUser, mockUser)
		mockFileRepository.AssertCalled(t, "UploadAvatar", uploadFileArgs...)
		mockUserRepository.AssertCalled(t, "Update", updateArgs...)
	})

	t.Run("Successful update image", func(t *testing.T) {
		imageURL := "https://imageurl.com/jdfkj34kljl"
		uid, _ := GenerateId()

		mockUser := &model.User{
			Email:    "new@bob.com",
			Username: "NewRobert",
			Image:    imageURL,
		}
		mockUser.ID = uid

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "test_dir"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
		}

		deleteImageArgs := mock.Arguments{
			imageURL,
		}

		mockFileRepository.
			On("UploadAvatar", uploadFileArgs...).
			Return(imageURL, nil)

		mockFileRepository.
			On("DeleteImage", deleteImageArgs...).
			Return(nil)

		mockUpdatedUser := &model.User{
			Email:    "new@bob.com",
			Username: "NewRobert",
			Image:    imageURL,
		}
		mockUpdatedUser.ID = uid

		updateArgs := mock.Arguments{
			mockUser,
		}

		mockUserRepository.
			On("Update", updateArgs...).
			Return(nil)

		url, err := us.ChangeAvatar(imageFileHeader, directory)
		assert.NoError(t, err)
		err = us.DeleteImage(mockUser.Image)
		assert.NoError(t, err)

		mockUser.Image = url
		err = us.Update(mockUser)
		assert.NoError(t, err)

		assert.Equal(t, mockUpdatedUser, mockUser)
		mockFileRepository.AssertCalled(t, "UploadAvatar", uploadFileArgs...)
		mockFileRepository.AssertCalled(t, "DeleteImage", imageURL)
		mockUserRepository.AssertCalled(t, "Update", updateArgs...)
	})

	t.Run("FileRepository Error", func(t *testing.T) {
		// need to create a new UserService and repository
		// because testify has no way to overwrite a mock's
		// "On" call.
		mockUserRepository := new(mocks.UserRepository)
		mockFileRepository := new(mocks.FileRepository)

		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
			FileRepository: mockFileRepository,
		})

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "file_directory"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
		}

		mockError := apperrors.NewInternal()
		mockFileRepository.
			On("UploadAvatar", uploadFileArgs...).
			Return("", mockError)

		url, err := us.ChangeAvatar(imageFileHeader, directory)
		assert.Equal(t, "", url)
		assert.Error(t, err)

		mockFileRepository.AssertCalled(t, "UploadAvatar", uploadFileArgs...)
		mockUserRepository.AssertNotCalled(t, "Update")
	})

	t.Run("UserRepository UpdateImage Error", func(t *testing.T) {
		uid, _ := GenerateId()
		imageURL := "https://imageurl.com/jdfkj34kljl"

		// has imageURL
		mockUser := &model.User{
			Email:    "new@bob.com",
			Username: "A New Bob!",
			Image:    imageURL,
		}
		mockUser.ID = uid

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "file_dir"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
		}

		mockFileRepository.
			On("UploadAvatar", uploadFileArgs...).
			Return(imageURL, nil)

		updateArgs := mock.Arguments{
			mockUser,
		}

		mockError := apperrors.NewInternal()
		mockUserRepository.
			On("Update", updateArgs...).
			Return(mockError)

		url, err := us.ChangeAvatar(imageFileHeader, directory)
		assert.NoError(t, err)
		assert.Equal(t, imageURL, url)

		err = us.Update(mockUser)

		assert.Error(t, err)
		mockFileRepository.AssertCalled(t, "UploadAvatar", uploadFileArgs...)
		mockUserRepository.AssertCalled(t, "Update", updateArgs...)
	})
}

func TestUserService_ChangeFollow(t *testing.T) {
	t.Run("Success change to following", func(t *testing.T) {
		uid, _ := GenerateId()
		mockUser := fixture.GetMockUser()

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})
		mockUserRepository.On("AddFollow", mockUser.ID, uid).Return(nil)

		err := us.ChangeFollow(mockUser, uid)

		assert.NoError(t, err)
		mockUserRepository.AssertExpectations(t)
		mockUserRepository.AssertNotCalled(t, "RemoveFollow", mockUser, uid)
	})

	t.Run("Success change to unfollowed", func(t *testing.T) {
		current := fixture.GetMockUser()
		mockUser := fixture.GetMockUser()

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})
		mockUserRepository.On("RemoveFollow", mockUser.ID, current.ID).Return(nil)

		mockUser.Followers = append(mockUser.Followers, current)
		err := us.ChangeFollow(mockUser, current.ID)

		assert.NoError(t, err)
		mockUserRepository.AssertExpectations(t)
		mockUserRepository.AssertNotCalled(t, "AddFollow", mockUser, current.ID)
	})

	t.Run("Error from AddFollow", func(t *testing.T) {
		current := fixture.GetMockUser()
		mockUser := fixture.GetMockUser()

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})

		mockUserRepository.On("AddFollow", mockUser.ID, current.ID).Return(fmt.Errorf("some error down the call chain"))

		err := us.ChangeFollow(mockUser, current.ID)

		assert.Error(t, err)
		mockUserRepository.AssertExpectations(t)
	})

	t.Run("Error from RemoveFollow", func(t *testing.T) {
		current := fixture.GetMockUser()
		mockUser := fixture.GetMockUser()

		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})

		mockUserRepository.On("RemoveFollow", mockUser.ID, current.ID).Return(fmt.Errorf("some error down the call chain"))

		mockUser.Followers = append(mockUser.Followers, current)
		err := us.ChangeFollow(mockUser, current.ID)

		assert.Error(t, err)
		mockUserRepository.AssertExpectations(t)
	})
}

func TestUserService_Search(t *testing.T) {

	users := make([]model.User, 0)

	for i := 0; i < 5; i++ {
		mockUser := fixture.GetMockUser()
		users = append(users, *mockUser)
	}

	t.Run("Success", func(t *testing.T) {
		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})

		term := "tes"

		mockUserRepository.On("SearchProfiles", term).Return(&users, nil)

		rsp, err := us.Search(term)

		assert.NoError(t, err)
		assert.Equal(t, 5, len(*rsp))
		mockUserRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		mockUserRepository := new(mocks.UserRepository)
		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
		})

		term := "tes"
		mockUserRepository.On("SearchProfiles", term).Return(nil, fmt.Errorf("some error down the call chain"))

		rsp, err := us.Search(term)

		assert.Nil(t, rsp)
		assert.Error(t, err)
		mockUserRepository.AssertExpectations(t)
	})
}

func TestUserService_ChangeBanner(t *testing.T) {
	mockUserRepository := new(mocks.UserRepository)
	mockFileRepository := new(mocks.FileRepository)

	us := NewUserService(&USConfig{
		UserRepository: mockUserRepository,
		FileRepository: mockFileRepository,
	})

	t.Run("Successful new banner", func(t *testing.T) {
		// does not have have banner
		mockUser := fixture.GetMockUser()

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "test_dir"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
		}

		imageURL := "https://imageurl.com/jdfkj34kljl"

		mockFileRepository.
			On("UploadBanner", uploadFileArgs...).
			Return(imageURL, nil)

		updateArgs := mock.Arguments{
			mockUser,
		}

		mockUpdatedUser := &model.User{
			ID:          mockUser.ID,
			Email:       mockUser.Email,
			Bio:         mockUser.Bio,
			Username:    mockUser.Username,
			DisplayName: mockUser.DisplayName,
			Image:       mockUser.Image,
			Banner:      &imageURL,
			Password:    mockUser.Password,
			CreatedAt:   mockUser.CreatedAt,
			UpdatedAt:   mockUser.UpdatedAt,
		}

		mockUserRepository.
			On("Update", updateArgs...).
			Return(nil)

		url, err := us.ChangeBanner(imageFileHeader, directory)
		assert.NoError(t, err)
		mockUser.Banner = &url

		err = us.Update(mockUser)

		assert.NoError(t, err)
		assert.Equal(t, mockUpdatedUser, mockUser)
		mockFileRepository.AssertCalled(t, "UploadBanner", uploadFileArgs...)
		mockUserRepository.AssertCalled(t, "Update", updateArgs...)
	})

	t.Run("Successful update banner", func(t *testing.T) {
		imageURL := "https://imageurl.com/jdfkj34kljl"

		mockUser := fixture.GetMockUser()
		mockUser.Banner = &imageURL

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "test_dir"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
		}

		deleteImageArgs := mock.Arguments{
			imageURL,
		}

		mockFileRepository.
			On("UploadBanner", uploadFileArgs...).
			Return(imageURL, nil)

		mockFileRepository.
			On("DeleteImage", deleteImageArgs...).
			Return(nil)

		mockUpdatedUser := &model.User{
			ID:          mockUser.ID,
			Email:       mockUser.Email,
			Bio:         mockUser.Bio,
			Username:    mockUser.Username,
			DisplayName: mockUser.DisplayName,
			Image:       mockUser.Image,
			Banner:      &imageURL,
			Password:    mockUser.Password,
			CreatedAt:   mockUser.CreatedAt,
			UpdatedAt:   mockUser.UpdatedAt,
		}

		updateArgs := mock.Arguments{
			mockUser,
		}

		mockUserRepository.
			On("Update", updateArgs...).
			Return(nil)

		url, err := us.ChangeBanner(imageFileHeader, directory)
		assert.NoError(t, err)
		err = us.DeleteImage(*mockUser.Banner)
		assert.NoError(t, err)

		mockUser.Banner = &url
		err = us.Update(mockUser)
		assert.NoError(t, err)

		assert.Equal(t, mockUpdatedUser, mockUser)
		mockFileRepository.AssertCalled(t, "UploadBanner", uploadFileArgs...)
		mockFileRepository.AssertCalled(t, "DeleteImage", imageURL)
		mockUserRepository.AssertCalled(t, "Update", updateArgs...)
	})

	t.Run("FileRepository Error", func(t *testing.T) {
		// need to create a new UserService and repository
		// because testify has no way to overwrite a mock's
		// "On" call.
		mockUserRepository := new(mocks.UserRepository)
		mockFileRepository := new(mocks.FileRepository)

		us := NewUserService(&USConfig{
			UserRepository: mockUserRepository,
			FileRepository: mockFileRepository,
		})

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "file_directory"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
		}

		mockError := apperrors.NewInternal()
		mockFileRepository.
			On("UploadBanner", uploadFileArgs...).
			Return("", mockError)

		url, err := us.ChangeBanner(imageFileHeader, directory)
		assert.Equal(t, "", url)
		assert.Error(t, err)

		mockFileRepository.AssertCalled(t, "UploadBanner", uploadFileArgs...)
		mockUserRepository.AssertNotCalled(t, "Update")
	})

	t.Run("UserRepository Update Error", func(t *testing.T) {
		imageURL := "https://imageurl.com/jdfkj34kljl"

		mockUser := fixture.GetMockUser()
		mockUser.Banner = &imageURL

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "file_dir"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
		}

		mockFileRepository.
			On("UploadBanner", uploadFileArgs...).
			Return(imageURL, nil)

		updateArgs := mock.Arguments{
			mockUser,
		}

		mockError := apperrors.NewInternal()
		mockUserRepository.
			On("Update", updateArgs...).
			Return(mockError)

		url, err := us.ChangeBanner(imageFileHeader, directory)
		assert.NoError(t, err)
		assert.Equal(t, imageURL, url)

		err = us.Update(mockUser)

		assert.Error(t, err)
		mockFileRepository.AssertCalled(t, "UploadBanner", uploadFileArgs...)
		mockUserRepository.AssertCalled(t, "Update", updateArgs...)
	})
}
