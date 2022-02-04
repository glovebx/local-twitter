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
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHandler_Feed(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)
	authUser := fixture.GetMockUser()

	profile := fixture.GetMockUser()
	profile.Followers = append(profile.Followers, authUser)

	for i := 0; i < 5; i++ {
		mockPost := fixture.GetMockPost()
		profile.Posts = append(profile.Posts, *mockPost)
	}

	t.Run("Success", func(t *testing.T) {
		mockPostService := new(mocks.PostService)
		mockPostService.On("GetUserFeed", authUser.ID, "").Return(&profile.Posts, nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", authUser.ID)
			c.Set("userId", authUser.ID)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		request, err := http.NewRequest(http.MethodGet, "/v1/posts/feed", nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		rsp := make([]model.PostResponse, 0)

		for _, p := range profile.Posts {
			post := p.NewFeedResponse(authUser.ID)
			rsp = append(rsp, post)
		}

		respBody, err := json.Marshal(gin.H{
			"posts":   rsp,
			"hasMore": false,
		})
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertExpectations(t)
	})

	t.Run("Unauthorized", func(t *testing.T) {
		mockPostService := new(mocks.PostService)
		mockPostService.On("GetUserFeed", authUser.ID, "").Return(&profile.Posts, nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		request, err := http.NewRequest(http.MethodGet, "/v1/posts/feed", nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		assert.NoError(t, err)

		assert.Equal(t, http.StatusUnauthorized, rr.Code)

		mockPostService.AssertNotCalled(t, "GetUserFeed", authUser.ID, "")
	})

	t.Run("Error", func(t *testing.T) {
		mockPostService := new(mocks.PostService)
		mockPostService.On("GetUserFeed", authUser.ID, "").Return(nil, fmt.Errorf("some error down call chain"))

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", authUser.ID)
			c.Set("userId", authUser.ID)
		})

		NewHandler(&Config{
			R:           router,
			PostService: mockPostService,
		})

		request, err := http.NewRequest(http.MethodGet, "/v1/posts/feed", nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respErr := apperrors.NewNotFound("feed", authUser.ID)

		respBody, err := json.Marshal(gin.H{
			"error": respErr,
		})
		assert.NoError(t, err)

		assert.Equal(t, respErr.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockPostService.AssertExpectations(t)
	})
}
