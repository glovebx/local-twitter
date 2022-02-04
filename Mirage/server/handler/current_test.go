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

func TestHandler_Current(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)

	t.Run("Success", func(t *testing.T) {
		uid, _ := service.GenerateId()

		mockUserResp := fixture.GetMockUser()
		mockUserResp.ID = uid

		mockUserService := new(mocks.UserService)
		mockUserService.On("Get", uid).Return(mockUserResp, nil)

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", uid)
		})

		router.Use(func(c *gin.Context) {
			c.Set("userId", uid)
		})

		NewHandler(&Config{
			R:           router,
			UserService: mockUserService,
		})

		request, err := http.NewRequest(http.MethodGet, "/v1/accounts", nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockUserResp.NewAccountResponse())
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockUserService.AssertExpectations(t)
	})

	t.Run("NotFound", func(t *testing.T) {
		uid, _ := service.GenerateId()
		mockUserService := new(mocks.UserService)
		mockUserService.On("Get", uid).Return(nil, fmt.Errorf("some error down call chain"))

		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			c.Set("userId", uid)
			session := sessions.Default(c)
			session.Set("userId", uid)
		})

		NewHandler(&Config{
			R:           router,
			UserService: mockUserService,
		})

		request, err := http.NewRequest(http.MethodGet, "/v1/accounts", nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		respErr := apperrors.NewNotFound("user", uid)

		respBody, err := json.Marshal(gin.H{
			"error": respErr,
		})
		assert.NoError(t, err)

		assert.Equal(t, respErr.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockUserService.AssertExpectations(t) // assert that UserService.Get was called
	})

	t.Run("Unauthorized", func(t *testing.T) {
		uid, _ := service.GenerateId()
		mockUserService := new(mocks.UserService)
		mockUserService.On("Get", uid).Return(nil, nil)

		rr := httptest.NewRecorder()

		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		NewHandler(&Config{
			R:           router,
			UserService: mockUserService,
		})

		request, err := http.NewRequest(http.MethodGet, "/v1/accounts", nil)
		assert.NoError(t, err)

		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusUnauthorized, rr.Code)
		mockUserService.AssertNotCalled(t, "Get", uid)
	})
}
