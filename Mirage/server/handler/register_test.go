package handler

import (
	"bytes"
	"encoding/json"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/mocks"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHandler_Register(t *testing.T) {
	gin.SetMode(gin.TestMode)

	user := fixture.GetMockUser()
	reqUser := &model.User{
		Email:       user.Email,
		Password:    user.Password,
		Username:    user.Username,
		DisplayName: user.DisplayName,
	}

	user2 := fixture.GetMockUser()
	user2.DisplayName = "Test User"
	reqUser2 := &model.User{
		Email:       user2.Email,
		Password:    user2.Password,
		Username:    user2.Username,
		DisplayName: user2.DisplayName,
	}

	testCases := []struct {
		name          string
		body          gin.H
		buildStubs    func(mockUserService *mocks.UserService)
		checkResponse func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService)
	}{
		{
			name: "OK",
			body: gin.H{
				"username":    user.Username,
				"password":    user.Password,
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", reqUser).Return(user, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				assert.Equal(t, http.StatusCreated, recorder.Code)
				respBody, err := json.Marshal(user.NewAccountResponse())
				assert.NoError(t, err)
				assert.Equal(t, recorder.Body.Bytes(), respBody)
				mockUserService.AssertCalled(t, "Register", reqUser)
				assert.Contains(t, recorder.Header(), "Set-Cookie")
			},
		},
		{
			name: "OK with non alphanumeric display name",
			body: gin.H{
				"username":    user2.Username,
				"password":    user2.Password,
				"displayName": user2.DisplayName,
				"email":       user2.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", reqUser2).Return(user2, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				assert.Equal(t, http.StatusCreated, recorder.Code)
				respBody, err := json.Marshal(user2.NewAccountResponse())
				assert.NoError(t, err)
				assert.Equal(t, recorder.Body.Bytes(), respBody)
				mockUserService.AssertCalled(t, "Register", reqUser2)
				assert.Contains(t, recorder.Header(), "Set-Cookie")
			},
		},
		{
			name: "InternalError",
			body: gin.H{
				"username":    user.Username,
				"password":    user.Password,
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", reqUser).Return(nil, apperrors.NewInternal())
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusInternalServerError, recorder.Code)
			},
		},
		{
			name: "Email already in use",
			body: gin.H{
				"username":    user.Username,
				"password":    user.Password,
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", reqUser).Return(nil, apperrors.NewConflict("email"))
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusConflict, recorder.Code)
			},
		},
		{
			name: "Username already in use",
			body: gin.H{
				"username":    user.Username,
				"password":    user.Password,
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", reqUser).Return(nil, apperrors.NewConflict("username"))
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusConflict, recorder.Code)
			},
		},
		{
			name: "Username too short",
			body: gin.H{
				"username":    "te",
				"password":    user.Password,
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "Username too long",
			body: gin.H{
				"username":    fixture.RandStringRunes(20),
				"password":    user.Password,
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "Username not alphanumeric",
			body: gin.H{
				"username":    "Test User",
				"password":    user.Password,
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "Invalid Email",
			body: gin.H{
				"username":    user.Email,
				"password":    user.Password,
				"displayName": user.DisplayName,
				"email":       "test.email",
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "Password Too Short",
			body: gin.H{
				"username":    user.Email,
				"password":    "pass",
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "DisplayName too short",
			body: gin.H{
				"username":    user.Username,
				"password":    user.Password,
				"displayName": "tes",
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "DisplayName too long",
			body: gin.H{
				"username":    user.Username,
				"password":    user.Password,
				"displayName": fixture.RandStringRunes(55),
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "Username Required",
			body: gin.H{
				"password":    user.Password,
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "DisplayName Required",
			body: gin.H{
				"username": user.Username,
				"password": user.Password,
				"email":    user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "Email Required",
			body: gin.H{
				"username":    user.Username,
				"password":    user.Password,
				"displayName": user.DisplayName,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
		{
			name: "Password Required",
			body: gin.H{
				"username":    user.Username,
				"displayName": user.DisplayName,
				"email":       user.Email,
			},
			buildStubs: func(mockUserService *mocks.UserService) {
				mockUserService.On("Register", mock.AnythingOfType("*model.User")).Return(nil, nil)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder, mockUserService *mocks.UserService) {
				require.Equal(t, http.StatusBadRequest, recorder.Code)
				mockUserService.AssertNotCalled(t, "Register")
			},
		},
	}

	for i := range testCases {
		tc := testCases[i]

		t.Run(tc.name, func(t *testing.T) {
			mockUserService := new(mocks.UserService)
			tc.buildStubs(mockUserService)

			// a response recorder for getting written http response
			rr := httptest.NewRecorder()

			router := gin.Default()
			store := cookie.NewStore([]byte("secret"))
			router.Use(sessions.Sessions("mqk", store))

			NewHandler(&Config{
				R:           router,
				UserService: mockUserService,
			})

			// create a request body with empty email and password
			reqBody, err := json.Marshal(tc.body)
			assert.NoError(t, err)

			// use bytes.NewBuffer to create a reader
			request, err := http.NewRequest(http.MethodPost, "/v1/accounts/register", bytes.NewBuffer(reqBody))
			assert.NoError(t, err)

			request.Header.Set("Content-Type", "application/json")

			router.ServeHTTP(rr, request)

			tc.checkResponse(rr, mockUserService)
		})
	}
}
