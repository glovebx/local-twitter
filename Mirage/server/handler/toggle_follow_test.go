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

func TestHandler_ToggleFollow(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)
	current := fixture.GetMockUser()

	t.Run("Successful follow", func(t *testing.T) {
		mockUser := fixture.GetMockUser()

		mockUserService := new(mocks.UserService)
		mockUserService.On("FindByUsername", mockUser.Username).Return(mockUser, nil)
		mockUserService.On("ChangeFollow", mockUser, current.ID).
			Run(func(args mock.Arguments) {
				mockUser.Followers = append(mockUser.Followers, current)
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
			UserService: mockUserService,
		})

		url := fmt.Sprintf("/v1/profiles/%s/follow", mockUser.Username)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockUser.NewProfileResponse(current.ID))
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		profile := &model.Profile{}
		err = json.Unmarshal(rr.Body.Bytes(), profile)
		assert.NoError(t, err)
		assert.Equal(t, uint(1), profile.Followers)
		assert.Equal(t, true, profile.Following)

		mockUserService.AssertExpectations(t)
	})

	t.Run("Successful unfollow", func(t *testing.T) {
		mockUser := fixture.GetMockUser()
		mockUser.Followers = append(mockUser.Followers, current)
		assert.Equal(t, true, mockUser.IsFollowing(current.ID))

		mockUserService := new(mocks.UserService)
		mockUserService.On("FindByUsername", mockUser.Username).Return(mockUser, nil)
		mockUserService.On("ChangeFollow", mockUser, current.ID).
			Run(func(args mock.Arguments) {
				mockUser.Followers = mockUser.Followers[1:]
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
			UserService: mockUserService,
		})

		url := fmt.Sprintf("/v1/profiles/%s/follow", mockUser.Username)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockUser.NewProfileResponse(current.ID))
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		profile := &model.Profile{}
		err = json.Unmarshal(rr.Body.Bytes(), profile)
		assert.NoError(t, err)
		assert.Equal(t, uint(0), profile.Followers)
		assert.Equal(t, false, profile.Following)

		mockUserService.AssertExpectations(t)
	})

	t.Run("Unauthorized", func(t *testing.T) {
		username := fixture.Username()

		mockUserService := new(mocks.UserService)
		mockUserService.On("FindByUsername", username).Return(nil, nil)

		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		NewHandler(&Config{
			R:           router,
			UserService: mockUserService,
		})

		url := fmt.Sprintf("/v1/profiles/%s/follow", username)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusUnauthorized, rr.Code)
		mockUserService.AssertNotCalled(t, "FindByUsername", username)
	})

	t.Run("NotFound", func(t *testing.T) {
		username, _ := service.GenerateId()

		mockUserService := new(mocks.UserService)
		mockUserService.On("FindByUsername", username).Return(nil, fmt.Errorf("some error down call chain"))

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
			UserService: mockUserService,
		})

		url := fmt.Sprintf("/v1/profiles/%s/follow", username)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respErr := apperrors.NewNotFound("profile", username)

		respBody, err := json.Marshal(gin.H{
			"error": respErr,
		})
		assert.NoError(t, err)

		assert.Equal(t, respErr.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockUserService.AssertExpectations(t) // assert that UserService.Get was called
	})

	t.Run("Error", func(t *testing.T) {
		mockUser := fixture.GetMockUser()
		mockUserService := new(mocks.UserService)
		mockUserService.On("FindByUsername", mockUser.Username).Return(mockUser, nil)

		mockError := apperrors.NewInternal()
		mockUserService.On("ChangeFollow", mockUser, current.ID).Return(mockError)

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
			UserService: mockUserService,
		})

		url := fmt.Sprintf("/v1/profiles/%s/follow", mockUser.Username)
		request, err := http.NewRequest(http.MethodPost, url, nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, _ := json.Marshal(gin.H{
			"error": mockError,
		})

		assert.Equal(t, mockError.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		mockUserService.AssertExpectations(t)
	})
}
