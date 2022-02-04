package service

import (
	"fmt"
	"github.com/sentrionic/mirage/mocks"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"testing"
)

func TestPostService_FindPostByID(t *testing.T) {
	t.Run("Success", func(t *testing.T) {
		uid, _ := GenerateId()
		mockPost := fixture.GetMockPost()
		mockPost.ID = uid

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("FindByID", uid).Return(mockPost, nil)

		post, err := ps.FindPostByID(uid)

		assert.NoError(t, err)
		assert.Equal(t, post, mockPost)
		mockPostRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		uid, _ := GenerateId()

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})

		mockPostRepository.On("FindByID", uid).Return(nil, fmt.Errorf("some error down the call chain"))

		post, err := ps.FindPostByID(uid)

		assert.Nil(t, post)
		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})
}

func TestPostService_CreatePost(t *testing.T) {
	t.Run("Success", func(t *testing.T) {
		uid, _ := GenerateId()
		mockPost := fixture.GetMockPost()

		initial := &model.Post{
			UserID: mockPost.UserID,
			Text:   mockPost.Text,
		}

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})

		mockPostRepository.
			On("Create", initial).
			Run(func(args mock.Arguments) {
				mockPost.ID = uid
			}).Return(mockPost, nil)

		post, err := ps.CreatePost(initial)

		assert.NoError(t, err)

		assert.Equal(t, uid, mockPost.ID)
		assert.Equal(t, post, mockPost)

		mockPostRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		initial := &model.Post{
			Text:   mockPost.Text,
			UserID: mockPost.UserID,
		}

		mockPostRepository := new(mocks.PostRepository)
		us := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})

		mockErr := apperrors.NewInternal()

		mockPostRepository.
			On("Create", initial).
			Return(nil, mockErr)

		post, err := us.CreatePost(initial)

		// assert error is error we response with in mock
		assert.EqualError(t, err, mockErr.Error())
		assert.Nil(t, post)

		mockPostRepository.AssertExpectations(t)
	})
}

func TestPostService_UploadFile(t *testing.T) {
	mockPostRepository := new(mocks.PostRepository)
	mockFileRepository := new(mocks.FileRepository)

	ps := NewPostService(&PSConfig{
		PostRepository: mockPostRepository,
		FileRepository: mockFileRepository,
	})

	mockUser := fixture.GetMockUser()

	t.Run("Successful image upload", func(t *testing.T) {
		mockPost := fixture.GetMockPost()

		file := &model.File{
			PostId:   mockPost.ID,
			FileType: "image/png",
		}

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "media/"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
			mock.AnythingOfType("string"),
			file.FileType,
		}

		imageURL := "https://imageurl.com/jdfkj34kljl"

		mockFileRepository.
			On("UploadFile", uploadFileArgs...).
			Return(imageURL, nil)

		file.Url = imageURL

		initial := &model.Post{
			File: file,
			User: *mockUser,
		}

		mockPostRepository.
			On("Create", initial).
			Return(mockPost, nil)

		uploadedFile, err := ps.UploadFile(imageFileHeader)
		assert.NoError(t, err)
		assert.NotNil(t, uploadedFile)

		newPost, err := ps.CreatePost(initial)

		assert.NoError(t, err)
		assert.Equal(t, newPost, mockPost)
		mockFileRepository.AssertCalled(t, "UploadFile", uploadFileArgs...)
		mockPostRepository.AssertCalled(t, "Create", initial)
	})

	t.Run("FileRepository Error", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		mockFileRepository := new(mocks.FileRepository)

		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
			FileRepository: mockFileRepository,
		})

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "media/"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
			mock.AnythingOfType("string"),
			mock.AnythingOfType("string"),
		}

		mockError := apperrors.NewInternal()
		mockFileRepository.
			On("UploadFile", uploadFileArgs...).
			Return("", mockError)

		uploadedFile, err := ps.UploadFile(imageFileHeader)
		assert.Nil(t, uploadedFile)
		assert.Error(t, err)

		mockFileRepository.AssertCalled(t, "UploadFile", uploadFileArgs...)
		mockPostRepository.AssertNotCalled(t, "Create")
	})

	t.Run("PostRepository Create Error", func(t *testing.T) {
		uid, _ := GenerateId()
		imageURL := "https://imageurl.com/jdfkj34kljl"

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()
		imageFileHeader := multipartImageFixture.GetFormFile()
		directory := "media/"

		uploadFileArgs := mock.Arguments{
			imageFileHeader,
			directory,
			mock.AnythingOfType("string"),
			mock.AnythingOfType("string"),
		}

		mockFileRepository.
			On("UploadFile", uploadFileArgs...).
			Return(imageURL, nil)

		file := &model.File{
			PostId:   uid,
			FileType: "image/png",
		}

		initial := &model.Post{
			ID:   uid,
			User: *mockUser,
			File: file,
		}

		mockError := apperrors.NewInternal()
		mockPostRepository.
			On("Create", initial).
			Return(nil, mockError)

		uploadedFile, err := ps.UploadFile(imageFileHeader)
		assert.NoError(t, err)
		assert.NotNil(t, uploadedFile)

		createdPost, err := ps.CreatePost(initial)

		assert.Error(t, err)
		assert.Nil(t, createdPost)
		mockFileRepository.AssertCalled(t, "UploadFile", uploadFileArgs...)
		mockPostRepository.AssertCalled(t, "Create", initial)
	})
}

func TestPostService_ToggleLike(t *testing.T) {
	t.Run("Success change to liked", func(t *testing.T) {
		uid, _ := GenerateId()
		mockPost := fixture.GetMockPost()

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("AddLike", mockPost, uid).Return(nil)

		err := ps.ToggleLike(mockPost, uid)

		assert.NoError(t, err)
		mockPostRepository.AssertExpectations(t)
		mockPostRepository.AssertNotCalled(t, "RemoveLike", mockPost, uid)
	})

	t.Run("Success change to unliked", func(t *testing.T) {
		mockUser := fixture.GetMockUser()
		mockPost := fixture.GetMockPost()
		mockPost.Likes = append(mockPost.Likes, *mockUser)

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("RemoveLike", mockPost, mockUser.ID).Return(nil)

		err := ps.ToggleLike(mockPost, mockUser.ID)

		assert.NoError(t, err)
		mockPostRepository.AssertExpectations(t)
		mockPostRepository.AssertNotCalled(t, "AddLike", mockPost, mockUser.ID)
	})

	t.Run("Error from RemoveLike", func(t *testing.T) {
		mockUser := fixture.GetMockUser()
		mockPost := fixture.GetMockPost()
		mockPost.Likes = append(mockPost.Likes, *mockUser)

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("RemoveLike", mockPost, mockUser.ID).Return(fmt.Errorf("some error down the call chain"))

		err := ps.ToggleLike(mockPost, mockUser.ID)

		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})

	t.Run("Error from AddLike", func(t *testing.T) {
		mockUser := fixture.GetMockUser()
		mockPost := fixture.GetMockPost()

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("AddLike", mockPost, mockUser.ID).Return(fmt.Errorf("some error down the call chain"))

		err := ps.ToggleLike(mockPost, mockUser.ID)

		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})
}

func TestPostService_ToggleRetweet(t *testing.T) {
	t.Run("Successful retweet", func(t *testing.T) {
		uid, _ := GenerateId()
		mockPost := fixture.GetMockPost()

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("AddRetweet", mockPost, uid).Return(nil)

		err := ps.ToggleRetweet(mockPost, uid)

		assert.NoError(t, err)
		mockPostRepository.AssertExpectations(t)
		mockPostRepository.AssertNotCalled(t, "RemoveRetweet", mockPost, uid)
	})

	t.Run("Successfully removed retweet", func(t *testing.T) {
		mockUser := fixture.GetMockUser()
		mockPost := fixture.GetMockPost()
		mockPost.Retweets = append(mockPost.Retweets, *mockUser)

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("RemoveRetweet", mockPost, mockUser.ID).Return(nil)

		err := ps.ToggleRetweet(mockPost, mockUser.ID)

		assert.NoError(t, err)
		mockPostRepository.AssertExpectations(t)
		mockPostRepository.AssertNotCalled(t, "AddRetweet", mockPost, mockUser.ID)
	})

	t.Run("Error from RemoveRetweet", func(t *testing.T) {
		mockUser := fixture.GetMockUser()
		mockPost := fixture.GetMockPost()
		mockPost.Retweets = append(mockPost.Retweets, *mockUser)

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("RemoveRetweet", mockPost, mockUser.ID).Return(fmt.Errorf("some error down the call chain"))

		err := ps.ToggleRetweet(mockPost, mockUser.ID)

		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})

	t.Run("Error from AddRetweet", func(t *testing.T) {
		mockUser := fixture.GetMockUser()
		mockPost := fixture.GetMockPost()

		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("AddRetweet", mockPost, mockUser.ID).Return(fmt.Errorf("some error down the call chain"))

		err := ps.ToggleRetweet(mockPost, mockUser.ID)

		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})
}

func TestPostService_ProfilePosts(t *testing.T) {

	authUser := fixture.GetMockUser()
	profile := fixture.GetMockUser()

	for i := 0; i < 5; i++ {
		mockPost := fixture.GetMockPost()
		profile.Posts = append(profile.Posts, *mockPost)
	}

	t.Run("Success", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("List", authUser.ID, "").Return(&profile.Posts, nil)

		posts, err := ps.ProfilePosts(authUser.ID, "")

		assert.NoError(t, err)
		assert.Equal(t, len(*posts), 5)
		mockPostRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})

		mockPostRepository.On("List", authUser.ID, "").Return(nil, fmt.Errorf("some error down the call chain"))

		posts, err := ps.ProfilePosts(authUser.ID, "")

		assert.Nil(t, posts)
		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})
}

func TestPostService_GetUserFeed(t *testing.T) {

	authUser := fixture.GetMockUser()
	profile := fixture.GetMockUser()
	profile.Followers = append(profile.Followers, authUser)

	for i := 0; i < 5; i++ {
		mockPost := fixture.GetMockPost()
		profile.Posts = append(profile.Posts, *mockPost)
	}

	t.Run("Success", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("Feed", authUser.ID, "").Return(&profile.Posts, nil)

		posts, err := ps.GetUserFeed(authUser.ID, "")

		assert.NoError(t, err)
		assert.Equal(t, len(*posts), 5)
		mockPostRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})

		mockPostRepository.On("Feed", authUser.ID, "").Return(nil, fmt.Errorf("some error down the call chain"))

		posts, err := ps.GetUserFeed(authUser.ID, "")

		assert.Nil(t, posts)
		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})
}

func TestPostService_ProfileLikes(t *testing.T) {

	authUser := fixture.GetMockUser()
	posts := make([]model.Post, 0)

	for i := 0; i < 5; i++ {
		mockPost := fixture.GetMockPost()
		mockPost.Likes = append(mockPost.Likes, *authUser)
		posts = append(posts, *mockPost)
	}

	t.Run("Success", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("Likes", authUser.ID, "").Return(&posts, nil)

		rsp, err := ps.ProfileLikes(authUser.ID, "")

		assert.NoError(t, err)
		assert.Equal(t, len(*rsp), 5)
		mockPostRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})

		mockPostRepository.On("Likes", authUser.ID, "").Return(nil, fmt.Errorf("some error down the call chain"))

		rsp, err := ps.ProfileLikes(authUser.ID, "")

		assert.Nil(t, rsp)
		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})
}

func TestPostService_SearchPosts(t *testing.T) {

	authUser := fixture.GetMockUser()
	posts := make([]model.Post, 0)

	for i := 0; i < 5; i++ {
		mockPost := fixture.GetMockPost()
		mockPost.Likes = append(mockPost.Likes, *authUser)
		posts = append(posts, *mockPost)
	}

	t.Run("Success", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})

		term := "tes"

		mockPostRepository.On("GetPostsForHashtag", term, "").Return(&posts, nil)

		rsp, err := ps.SearchPosts(term, "")

		assert.NoError(t, err)
		assert.Equal(t, 5, len(*rsp))
		mockPostRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})

		term := "tes"
		mockPostRepository.On("GetPostsForHashtag", term, "").Return(nil, fmt.Errorf("some error down the call chain"))

		rsp, err := ps.SearchPosts(term, "")

		assert.Nil(t, rsp)
		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})
}

func TestPostService_ProfileMedia(t *testing.T) {

	profile := fixture.GetMockUser()
	posts := make([]model.Post, 0)

	for i := 0; i < 5; i++ {
		mockPost := fixture.GetMockPost()
		file := fixture.GetMockFile(mockPost.ID)
		mockPost.File = file
		posts = append(posts, *mockPost)
	}

	t.Run("Success", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})
		mockPostRepository.On("Media", profile.ID, "").Return(&posts, nil)

		rsp, err := ps.ProfileMedia(profile.ID, "")

		assert.NoError(t, err)
		assert.Equal(t, len(*rsp), 5)
		mockPostRepository.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		mockPostRepository := new(mocks.PostRepository)
		ps := NewPostService(&PSConfig{
			PostRepository: mockPostRepository,
		})

		mockPostRepository.On("Media", profile.ID, "").Return(nil, fmt.Errorf("some error down the call chain"))

		rsp, err := ps.ProfileMedia(profile.ID, "")

		assert.Nil(t, rsp)
		assert.Error(t, err)
		mockPostRepository.AssertExpectations(t)
	})
}
