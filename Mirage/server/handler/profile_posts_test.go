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
	"github.com/stretchr/testify/mock"
	"net/http"
	"net/http/httptest"
	"testing"
)

func getPostResponse(posts *[]model.Post) []model.PostResponse {
	response := make([]model.PostResponse, 0)

	for _, p := range *posts {
		post := model.PostResponse{
			ID:        p.ID,
			Text:      p.Text,
			Likes:     uint(len(p.Likes)),
			Retweets:  uint(len(p.Retweets)),
			File:      p.File,
			Author:    p.User.NewProfileResponse(""),
			CreatedAt: p.CreatedAt,
		}
		response = append(response, post)
	}

	return response
}

func TestHandler_GetProfilePosts(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)
	uid, _ := service.GenerateId()

	t.Run("Success", func(t *testing.T) {
		mockUserResp := fixture.GetMockUser()

		mockUserService := new(mocks.UserService)
		mockUserService.On("FindByUsername", mockUserResp.Username).Return(mockUserResp, nil)

		posts := make([]model.Post, 0)

		for i := 0; i < 5; i++ {
			mockPost := fixture.GetMockPost()
			mockPost.UserID = mockUserResp.ID
			posts = append(posts, *mockPost)
		}

		mockPostService := new(mocks.PostService)
		mockPostService.On("ProfilePosts", mockUserResp.ID, "").Return(&posts, nil)

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
			UserService: mockUserService,
			PostService: mockPostService,
		})

		url := fmt.Sprintf("/v1/profiles/%s/posts", mockUserResp.Username)
		request, err := http.NewRequest(http.MethodGet, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(gin.H{
			"posts":   getPostResponse(&posts),
			"hasMore": false,
		})
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockUserService.AssertExpectations(t)
	})

	t.Run("NoContextUser", func(t *testing.T) {
		mockUserResp := fixture.GetMockUser()

		mockUserService := new(mocks.UserService)
		mockUserService.On("FindByUsername", mockUserResp.Username).Return(mockUserResp, nil)

		posts := make([]model.Post, 0)

		for i := 0; i < 5; i++ {
			mockPost := fixture.GetMockPost()
			mockPost.UserID = mockUserResp.ID
			posts = append(posts, *mockPost)
		}

		mockPostService := new(mocks.PostService)
		mockPostService.On("ProfilePosts", mockUserResp.ID, "").Return(&posts, nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		NewHandler(&Config{
			R:           router,
			UserService: mockUserService,
			PostService: mockPostService,
		})

		url := fmt.Sprintf("/v1/profiles/%s/posts", mockUserResp.Username)
		request, err := http.NewRequest(http.MethodGet, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(gin.H{
			"posts":   getPostResponse(&posts),
			"hasMore": false,
		})
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockUserService.AssertExpectations(t)
	})

	t.Run("NotFound", func(t *testing.T) {
		username, _ := service.GenerateId()

		mockUserService := new(mocks.UserService)
		mockUserService.On("FindByUsername", username).Return(nil, fmt.Errorf("some error down call chain"))

		mockPostService := new(mocks.PostService)
		mockPostService.On("ProfilePosts", mock.AnythingOfType("string"), mock.AnythingOfType("string")).Return(nil, nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", uid)
		})

		NewHandler(&Config{
			R:           router,
			UserService: mockUserService,
			PostService: mockPostService,
		})

		url := fmt.Sprintf("/v1/profiles/%s/posts", username)
		request, err := http.NewRequest(http.MethodGet, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respErr := apperrors.NewNotFound("profile", username)

		respBody, err := json.Marshal(gin.H{
			"error": respErr,
		})
		assert.NoError(t, err)

		assert.Equal(t, respErr.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockUserService.AssertExpectations(t)
		mockPostService.AssertNotCalled(t, "ProfilePosts")
	})

}
