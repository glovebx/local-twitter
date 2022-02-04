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
	"github.com/sentrionic/mirage/service"
	"github.com/stretchr/testify/assert"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHandler_GetPost(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)
	uid, _ := service.GenerateId()

	t.Run("Success", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		mockUser := fixture.GetMockUser()
		mockPost.User = *mockUser

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)

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

		request, err := http.NewRequest(http.MethodGet, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockPost.NewPostResponse(uid))
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertExpectations(t)
	})

	t.Run("NoContextUser", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		mockUser := fixture.GetMockUser()
		mockPost.User = *mockUser

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		request, err := http.NewRequest(http.MethodGet, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockPost.NewPostResponse(uid))
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		post := &model.PostResponse{}
		err = json.Unmarshal(rr.Body.Bytes(), post)
		assert.NoError(t, err)
		assert.Equal(t, false, post.Author.Following)
		assert.Equal(t, false, post.Liked)

		mockPostService.AssertExpectations(t)
	})

	t.Run("NotFound", func(t *testing.T) {
		id, _ := service.GenerateId()

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", id).Return(nil, fmt.Errorf("some error down call chain"))

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

		request, err := http.NewRequest(http.MethodGet, "/v1/posts/"+id, nil)
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

	t.Run("Response post's author contains current user as follower", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		mockUser := fixture.GetMockUser()

		current := fixture.GetMockUser()
		current.ID = uid
		mockUser.Followers = append(mockPost.User.Followers, current)
		mockPost.User = *mockUser

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)

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

		request, err := http.NewRequest(http.MethodGet, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockPost.NewPostResponse(uid))
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		post := &model.PostResponse{}
		err = json.Unmarshal(rr.Body.Bytes(), post)
		assert.NoError(t, err)
		assert.Equal(t, uint(1), post.Author.Followers)
		assert.Equal(t, true, post.Author.Following)

		mockPostService.AssertExpectations(t)
	})

	t.Run("Response post is liked by current user", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		current := fixture.GetMockUser()
		current.ID = uid
		mockPost.Likes = append(mockPost.Likes, *current)

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)

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

		request, err := http.NewRequest(http.MethodGet, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockPost.NewPostResponse(uid))
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

	t.Run("Response post is retweeted by current user", func(t *testing.T) {
		mockPost := fixture.GetMockPost()
		current := fixture.GetMockUser()
		current.ID = uid
		mockPost.Retweets = append(mockPost.Retweets, *current)

		mockPostService := new(mocks.PostService)
		mockPostService.On("FindPostByID", mockPost.ID).Return(mockPost, nil)

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

		request, err := http.NewRequest(http.MethodGet, "/v1/posts/"+mockPost.ID, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockPost.NewPostResponse(uid))
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		post := &model.PostResponse{}
		err = json.Unmarshal(rr.Body.Bytes(), post)
		assert.NoError(t, err)
		assert.Equal(t, uint(1), post.Retweets)
		assert.Equal(t, true, post.Retweeted)

		mockPostService.AssertExpectations(t)
	})
}
