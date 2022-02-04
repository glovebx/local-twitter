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

func TestHandler_GetProfileLikes(t *testing.T) {
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
			posts = append(posts, *mockPost)
		}

		mockPostService := new(mocks.PostService)
		mockPostService.On("ProfileLikes", mockUserResp.ID, "").Return(&posts, nil)

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

		url := fmt.Sprintf("/v1/profiles/%s/likes", mockUserResp.Username)
		request, err := http.NewRequest(http.MethodGet, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		rsp := make([]model.PostResponse, 0)

		for _, p := range posts {
			post := p.NewPostResponse(uid)
			rsp = append(rsp, post)
		}

		respBody, err := json.Marshal(gin.H{
			"posts":   rsp,
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
			posts = append(posts, *mockPost)
		}

		mockPostService := new(mocks.PostService)
		mockPostService.On("ProfileLikes", mockUserResp.ID, "").Return(&posts, nil)

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

		url := fmt.Sprintf("/v1/profiles/%s/likes", mockUserResp.Username)
		request, err := http.NewRequest(http.MethodGet, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		rsp := make([]model.PostResponse, 0)

		for _, p := range posts {
			post := p.NewPostResponse(uid)
			rsp = append(rsp, post)
		}

		respBody, err := json.Marshal(gin.H{
			"posts":   rsp,
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
		mockPostService.On("ProfileLikes", mock.AnythingOfType("string"), mock.AnythingOfType("string")).Return(nil, nil)

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

		url := fmt.Sprintf("/v1/profiles/%s/likes", username)
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
		mockPostService.AssertNotCalled(t, "ProfileLikes")
	})

}
