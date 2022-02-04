package handler

import (
	"encoding/json"
	"fmt"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/mocks"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHandler_LikePost(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)
	current := fixture.GetMockUser()

	t.Run("Successful like", func(t *testing.T) {
		mockPost := fixture.GetMockPost()

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)
		mockPostService.On("ToggleLike", mockPost, current.ID).
			Run(func(args mock.Arguments) {
				mockPost.Likes = append(mockPost.Likes, *current)
			}).
			Return(nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			c.Set("userId", current.ID)
			session.Set("userId", current.ID)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		url := fmt.Sprintf("/v1/posts/%s/like", mockPost.ID)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockPost.NewPostResponse(current.ID))
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		post := &model.PostResponse{}
		err = json.Unmarshal(rr.Body.Bytes(), post)
		assert.NoError(t, err)
		assert.Equal(t, uint(1), post.Likes)
		assert.Equal(t, true, post.Liked)

		mockPostService.AssertExpectations(t)
	})

	t.Run("Successful unlike", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		mockPost.Likes = append(mockPost.Likes, *current)
		assert.Equal(t, true, mockPost.IsLiked(current.ID))

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)
		mockPostService.On("ToggleLike", mockPost, current.ID).
			Run(func(args mock.Arguments) {
				mockPost.Likes = mockPost.Likes[1:]
			}).
			Return(nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			c.Set("userId", current.ID)
			session.Set("userId", current.ID)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		url := fmt.Sprintf("/v1/posts/%s/like", mockPost.ID)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockPost.NewPostResponse(current.ID))
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		post := &model.PostResponse{}
		err = json.Unmarshal(rr.Body.Bytes(), post)
		assert.NoError(t, err)
		assert.Equal(t, uint(0), post.Likes)
		assert.Equal(t, false, post.Liked)

		mockPostService.AssertExpectations(t)
	})

	t.Run("Unauthorized", func(t *testing.T) {
		id := fixture.RandID()

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", id).Return(nil, nil)

		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		url := fmt.Sprintf("/v1/posts/%s/like", id)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusUnauthorized, rr.Code)
		mockPostService.AssertNotCalled(t, "FindPostByID", id)
	})

	t.Run("NotFound", func(t *testing.T) {
		id := fixture.RandID()

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", id).Return(nil, fmt.Errorf("some error down call chain"))

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			c.Set("userId", current.ID)
			session.Set("userId", current.ID)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		url := fmt.Sprintf("/v1/posts/%s/like", id)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respErr := apperrors.NewNotFound("post", id)

		respBody, err := json.Marshal(gin.H{
			"error": respErr,
		})
		assert.NoError(t, err)

		assert.Equal(t, respErr.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertExpectations(t)
	})

	t.Run("Error", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)

		mockError := apperrors.NewInternal()
		mockPostService.On("ToggleLike", mockPost, current.ID).Return(mockError)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			c.Set("userId", current.ID)
			session.Set("userId", current.ID)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		url := fmt.Sprintf("/v1/posts/%s/like", mockPost.ID)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, _ := json.Marshal(gin.H{
			"error": mockError,
		})

		assert.Equal(t, mockError.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		mockPostService.AssertExpectations(t)
	})
}
