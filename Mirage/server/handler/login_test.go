package handler

import (
	"bytes"
	"encoding/json"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/sentrionic/mirage/mocks"
	"github.com/sentrionic/mirage/model/apperrors"
	"github.com/sentrionic/mirage/model/fixture"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestLogin(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)

	// setup mock services, gin engine/router, handler layer
	mockUserService := new(mocks.UserService)

	router := gin.Default()
	store := cookie.NewStore([]byte("secret"))
	router.Use(sessions.Sessions("mqk", store))

	NewHandler(&Config{
		R:           router,
		UserService: mockUserService,
	})

	t.Run("Bad request data", func(t *testing.T) {
		// a response recorder for getting written http response
		rr := httptest.NewRecorder()

		// create a request body with invalid fields
		reqBody, err := json.Marshal(gin.H{
			"email":    "notanemail",
			"password": "short",
		})
		assert.NoError(t, err)

		request, err := http.NewRequest(http.MethodPost, "/v1/accounts/login", bytes.NewBuffer(reqBody))
		assert.NoError(t, err)

		request.Header.Set("Content-Type", "application/json")
		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusBadRequest, rr.Code)
		mockUserService.AssertNotCalled(t, "Login")
	})

	t.Run("Error Returned from UserService.Login", func(t *testing.T) {
		email := "bob@bob.com"
		password := "pwdoesnotmatch123"

		mockUSArgs := mock.Arguments{
			email,
			password,
		}

		mockError := apperrors.NewAuthorization("invalid email/password combo")

		mockUserService.On("Login", mockUSArgs...).Return(nil, mockError)

		rr := httptest.NewRecorder()

		// create a request body with valid fields
		reqBody, err := json.Marshal(gin.H{
			"email":    email,
			"password": password,
		})
		assert.NoError(t, err)

		request, err := http.NewRequest(http.MethodPost, "/v1/accounts/login", bytes.NewBuffer(reqBody))
		assert.NoError(t, err)

		request.Header.Set("Content-Type", "application/json")
		router.ServeHTTP(rr, request)

		mockUserService.AssertCalled(t, "Login", mockUSArgs...)
		assert.Equal(t, http.StatusUnauthorized, rr.Code)
	})

	t.Run("Successful Login", func(t *testing.T) {
		mockUser := fixture.GetMockUser()

		mockUSArgs := mock.Arguments{
			mockUser.Email,
			mockUser.Password,
		}

		mockUserService.On("Login", mockUSArgs...).Return(mockUser, nil)

		rr := httptest.NewRecorder()

		// create a request body with valid fields
		reqBody, err := json.Marshal(gin.H{
			"email":    mockUser.Email,
			"password": mockUser.Password,
		})
		assert.NoError(t, err)

		request, err := http.NewRequest(http.MethodPost, "/v1/accounts/login", bytes.NewBuffer(reqBody))
		assert.NoError(t, err)

		request.Header.Set("Content-Type", "application/json")
		router.ServeHTTP(rr, request)

		respBody, err := json.Marshal(mockUser.NewAccountResponse())
		assert.NoError(t, err)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		assert.Contains(t, rr.Header(), "Set-Cookie")

		mockUserService.AssertCalled(t, "Login", mockUSArgs...)
	})

}
