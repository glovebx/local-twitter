package handler

import (
	"encoding/json"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/mocks"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/sentrionic/mirage/service"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"testing"
)

func TestHandler_CreatePost(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)

	uid, _ := service.GenerateId()
	mockUser := fixture.GetMockUser()
	mockUser.ID = uid

	router := gin.Default()
	router.Use(func(c *gin.Context) {
		c.Set("userId", uid)
	})
	store := cookie.NewStore([]byte("secret"))
	router.Use(sessions.Sessions("mqk", store))

	router.Use(func(c *gin.Context) {
		session := sessions.Default(c)
		session.Set("userId", uid)
	})

	mockUserService := new(mocks.UserService)
	mockUserService.On("Get", uid).Return(mockUser, nil)

	mockPostService := new(mocks.PostService)

	NewHandler(&Config{
		R:            router,
		UserService:  mockUserService,
		PostService:  mockPostService,
		MaxBodyBytes: 4 * 1024 * 1024,
	})

	t.Run("Unauthorized", func(t *testing.T) {
		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))
		NewHandler(&Config{
			R: router,
		})

		rr := httptest.NewRecorder()

		text := fixture.RandStringRunes(120)

		form := url.Values{}
		form.Add("text", text)

		request, _ := http.NewRequest(http.MethodPost, "/v1/posts", strings.NewReader(form.Encode()))
		request.Form = form

		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusUnauthorized, rr.Code)
		mockPostService.AssertNotCalled(t, "CreatePost")
	})

	t.Run("Text Post Creation Success", func(t *testing.T) {
		rr := httptest.NewRecorder()

		mockPost := fixture.GetMockPost()
		mockPost.User = *mockUser
		mockPost.UserID = mockUser.ID

		form := url.Values{}
		form.Add("text", *mockPost.Text)

		request, _ := http.NewRequest(http.MethodPost, "/v1/posts", strings.NewReader(form.Encode()))
		request.Form = form

		initial := &model.Post{
			Text:   mockPost.Text,
			UserID: mockUser.ID,
			User:   *mockUser,
		}

		mockPostService.
			On("CreatePost", initial).
			Run(func(args mock.Arguments) {
				id, _ := service.GenerateId()
				mockPost.ID = id
			}).
			Return(mockPost, nil)

		router.ServeHTTP(rr, request)

		respBody, _ := json.Marshal(mockPost.NewPostResponse(""))

		assert.Equal(t, http.StatusCreated, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertCalled(t, "CreatePost", initial)
	})

	t.Run("Text Post Creation failure", func(t *testing.T) {
		rr := httptest.NewRecorder()

		mockPost := fixture.GetMockPost()

		form := url.Values{}
		form.Add("text", *mockPost.Text)

		request, _ := http.NewRequest(http.MethodPost, "/v1/posts", strings.NewReader(form.Encode()))
		request.Form = form

		initial := &model.Post{
			Text:   mockPost.Text,
			UserID: mockUser.ID,
			User:   *mockUser,
		}

		mockError := apperrors.NewInternal()

		mockPostService.
			On("CreatePost", initial).
			Return(nil, mockError)

		router.ServeHTTP(rr, request)

		respBody, _ := json.Marshal(gin.H{
			"error": mockError,
		})

		assert.Equal(t, mockError.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertCalled(t, "CreatePost", initial)
	})

	t.Run("Disallowed mimetype", func(t *testing.T) {
		rr := httptest.NewRecorder()

		multipartImageFixture := fixture.NewMultipartImage("image.txt", "mage/svg+xml")
		defer multipartImageFixture.Close()

		request, _ := http.NewRequest(http.MethodPost, "/v1/posts", multipartImageFixture.MultipartBody)

		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusBadRequest, rr.Code)

		mockUserService.AssertNotCalled(t, "CreatePost")
		mockUserService.AssertNotCalled(t, "UploadFile")
	})

	t.Run("Image Post Creation Success", func(t *testing.T) {
		rr := httptest.NewRecorder()

		mockPost := fixture.GetMockPost()
		mockPost.User = *mockUser
		mockPost.UserID = mockUser.ID

		uploadedFile := &model.File{
			PostId:   mockPost.ID,
			Url:      fixture.RandStringRunes(8),
			FileType: "image/png",
			Filename: fixture.RandStringRunes(8),
		}

		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer multipartImageFixture.Close()

		request, _ := http.NewRequest(http.MethodPost, "/v1/posts", multipartImageFixture.MultipartBody)
		request.Header.Set("Content-Type", multipartImageFixture.ContentType)

		initial := &model.Post{
			UserID: mockUser.ID,
			User:   *mockUser,
			File:   uploadedFile,
		}

		uploadImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		defer uploadImageFixture.Close()
		formFile := uploadImageFixture.GetFormFile()
		mockPostService.
			On("UploadFile", formFile).
			Return(uploadedFile, nil)

		mockPostService.
			On("CreatePost", initial).
			Run(func(args mock.Arguments) {
				id, _ := service.GenerateId()
				mockPost.ID = id
			}).
			Return(mockPost, nil)

		router.ServeHTTP(rr, request)

		respBody, _ := json.Marshal(mockPost.NewPostResponse(""))

		assert.Equal(t, http.StatusCreated, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertCalled(t, "CreatePost", initial)
		mockPostService.AssertCalled(t, "UploadFile", formFile)
	})
}

func TestHandler_CreatePost_BadRequests(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)

	uid, _ := service.GenerateId()
	mockUser := fixture.GetMockUser()
	mockUser.ID = uid

	router := gin.Default()
	router.Use(func(c *gin.Context) {
		c.Set("userId", uid)
	})
	store := cookie.NewStore([]byte("secret"))
	router.Use(sessions.Sessions("mqk", store))

	router.Use(func(c *gin.Context) {
		session := sessions.Default(c)
		session.Set("userId", uid)
	})

	mockUserService := new(mocks.UserService)
	mockUserService.On("Get", uid).Return(mockUser, nil)

	mockPostService := new(mocks.PostService)

	NewHandler(&Config{
		R:            router,
		UserService:  mockUserService,
		PostService:  mockPostService,
		MaxBodyBytes: 4 * 1024 * 1024,
	})

	testCases := []struct {
		name string
		body url.Values
	}{
		{
			name: "No file nor text",
			body: map[string][]string{},
		},
		{
			name: "Text empty and no file",
			body: map[string][]string{
				"text": {""},
			},
		},
		{
			name: "Text too long",
			body: map[string][]string{
				"text": {fixture.RandStringRunes(300)},
			},
		},
	}

	for i := range testCases {
		tc := testCases[i]

		t.Run(tc.name, func(t *testing.T) {

			rr := httptest.NewRecorder()

			form := tc.body
			request, _ := http.NewRequest(http.MethodPost, "/v1/posts", strings.NewReader(form.Encode()))
			request.Form = form

			router.ServeHTTP(rr, request)

			assert.Equal(t, http.StatusBadRequest, rr.Code)
			mockPostService.AssertNotCalled(t, "CreatePost")
		})
	}
}
