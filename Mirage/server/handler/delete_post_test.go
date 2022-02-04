package handler

import (
	"encoding/json"
	"fmt"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/mocks"
	"github.com/sentrionic/mirage/model/apperrors"
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/sentrionic/mirage/service"
	"github.com/stretchr/testify/assert"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHandler_DeletePost(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)
	uid, _ := service.GenerateId()

	t.Run("Success", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		mockPost.UserID = uid

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)
		mockPostService.On("DeletePost", mockPost).Return(nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", uid)
			c.Set("userId", uid)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		request, err := http.NewRequest(http.MethodDelete, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockPost.NewPostResponse(uid))
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertExpectations(t)
	})

	t.Run("Not the owner of the post", func(t *testing.T) {
		mockPost := fixture.GetMockPost()

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)
		mockPostService.On("DeletePost", mockPost).Return(nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", uid)
			c.Set("userId", uid)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		request, err := http.NewRequest(http.MethodDelete, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusUnauthorized, rr.Code)
		mockPostService.AssertCalled(t, "FindPostByID", mockPost.ID)
		mockPostService.AssertNotCalled(t, "DeletePost", mockPost)
	})

	t.Run("Unauthorized", func(t *testing.T) {
		mockPost := fixture.GetMockPost()

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)
		mockPostService.On("DeletePost", mockPost).Return(nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		request, err := http.NewRequest(http.MethodDelete, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusUnauthorized, rr.Code)

		mockPostService.AssertNotCalled(t, "FindPostByID", mockPost.ID)
		mockPostService.AssertNotCalled(t, "DeletePost", mockPost)
	})

	t.Run("NotFound", func(t *testing.T) {
		mockPost := fixture.GetMockPost()

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(nil, fmt.Errorf("some error down call chain"))
		mockPostService.On("DeletePost", mockPost).Return(nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", uid)
			c.Set("userId", uid)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		request, err := http.NewRequest(http.MethodDelete, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respErr := apperrors.NewNotFound("post", mockPost.ID)

		respBody, err := json.Marshal(gin.H{
			"error": respErr,
		})
		assert.NoError(t, err)

		assert.Equal(t, respErr.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertCalled(t, "FindPostByID", mockPost.ID)
		mockPostService.AssertNotCalled(t, "DeletePost", mockPost)
	})

	t.Run("Error returned from DeletePost", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		mockPost.UserID = uid

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)
		mockPostService.On("DeletePost", mockPost).Return(fmt.Errorf("some error down call chain"))

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", uid)
			c.Set("userId", uid)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		request, err := http.NewRequest(http.MethodDelete, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respErr := apperrors.NewInternal()

		respBody, err := json.Marshal(gin.H{
			"error": respErr,
		})
		assert.NoError(t, err)

		assert.Equal(t, respErr.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertCalled(t, "FindPostByID", mockPost.ID)
		mockPostService.AssertCalled(t, "DeletePost", mockPost)
	})
}
