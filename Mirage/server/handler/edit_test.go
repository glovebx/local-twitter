package handler

import (
	"bytes"
	"encoding/json"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/sentrionic/mirage/mocks"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/sentrionic/mirage/service"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestHandler_EditAccount(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)

	uid, _ := service.GenerateId()
	mockUser := fixture.GetMockUser()
	mockUser.ID = uid

	t.Run("Unauthorized", func(t *testing.T) {
		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))
		mockUserService := new(mocks.UserService)
		mockUserService.On("Get", uid).Return(mockUser, nil)

		NewHandler(&Config{
			R:           router,
			UserService: mockUserService,
		})

		rr := httptest.NewRecorder()

		newName := fixture.Username()
		newEmail := fixture.Email()
		newBio := fixture.RandStringRunes(100)

		form := url.Values{}
		form.Add("username", newName)
		form.Add("email", newEmail)
		form.Add("bio", newBio)

		request, _ := http.NewRequest(http.MethodPut, "/v1/accounts", strings.NewReader(form.Encode()))
		request.Form = form

		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusUnauthorized, rr.Code)
		mockUserService.AssertNotCalled(t, "Update")
	})

	t.Run("Update success", func(t *testing.T) {
		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			c.Set("userId", uid)
			session := sessions.Default(c)
			session.Set("userId", uid)
		})

		mockUserService := new(mocks.UserService)
		mockUserService.On("Get", uid).Return(mockUser, nil)

		NewHandler(&Config{
			R:            router,
			UserService:  mockUserService,
			MaxBodyBytes: 4 * 1024 * 1024,
		})

		rr := httptest.NewRecorder()

		newName := fixture.Username()
		newEmail := fixture.Email()
		newBio := fixture.RandStringRunes(100)
		newDisplayName := fixture.DisplayName()

		body := &bytes.Buffer{}
		writer := multipart.NewWriter(body)
		_ = writer.WriteField("username", newName)
		_ = writer.WriteField("email", newEmail)
		_ = writer.WriteField("bio", newBio)
		_ = writer.WriteField("displayName", newDisplayName)

		_ = writer.Close()

		request, _ := http.NewRequest(http.MethodPut, "/v1/accounts", body)
		request.Header.Set("Content-Type", writer.FormDataContentType())

		mockUser.Username = newName
		mockUser.Email = newEmail
		mockUser.Bio = &newBio
		mockUser.DisplayName = newDisplayName

		updateArgs := mock.Arguments{
			mockUser,
		}

		dbImageURL := "https://website.com/696292a38f493a4283d1a308e4a11732/84d81/Profile.jpg"

		mockUserService.
			On("Update", updateArgs...).
			Run(func(args mock.Arguments) {
				userArg := args.Get(0).(*model.User)
				userArg.Image = dbImageURL
			}).
			Return(nil)

		router.ServeHTTP(rr, request)

		mockUser.Image = dbImageURL
		respBody, _ := json.Marshal(mockUser.NewAccountResponse())

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockUserService.AssertCalled(t, "Update", updateArgs...)
	})

	t.Run("Update Failure", func(t *testing.T) {
		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			c.Set("userId", uid)
			session := sessions.Default(c)
			session.Set("userId", uid)
		})

		mockUserService := new(mocks.UserService)
		mockUserService.On("Get", uid).Return(mockUser, nil)

		NewHandler(&Config{
			R:            router,
			UserService:  mockUserService,
			MaxBodyBytes: 4 * 1024 * 1024,
		})

		rr := httptest.NewRecorder()

		bio := fixture.RandStringRunes(121)

		form := url.Values{}
		form.Add("username", mockUser.Username)
		form.Add("displayName", mockUser.DisplayName)
		form.Add("email", mockUser.Email)
		form.Add("bio", bio)

		request, _ := http.NewRequest(http.MethodPut, "/v1/accounts", strings.NewReader(form.Encode()))
		request.Form = form

		mockError := apperrors.NewInternal()

		mockUserService.
			On("Update", mockUser).
			Return(mockError)

		router.ServeHTTP(rr, request)

		respBody, _ := json.Marshal(gin.H{
			"error": mockError,
		})

		assert.Equal(t, mockError.Status(), rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())
		mockUserService.AssertCalled(t, "Update", mockUser)
	})

	t.Run("Disallowed mimetype", func(t *testing.T) {
		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))

		router.Use(func(c *gin.Context) {
			c.Set("userId", uid)
			session := sessions.Default(c)
			session.Set("userId", uid)
		})

		mockUserService := new(mocks.UserService)
		mockUserService.On("Get", uid).Return(mockUser, nil)

		NewHandler(&Config{
			R:            router,
			UserService:  mockUserService,
			MaxBodyBytes: 4 * 1024 * 1024,
		})

		rr := httptest.NewRecorder()

		multipartImageFixture := fixture.NewMultipartImage("image.txt", "mage/svg+xml")
		defer multipartImageFixture.Close()

		request, _ := http.NewRequest(http.MethodPut, "/v1/accounts", multipartImageFixture.MultipartBody)

		router.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusBadRequest, rr.Code)

		mockUserService.AssertNotCalled(t, "ChangeAvatar")
	})
}

func TestHandler_EditAccount_BadRequest(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)

	uid, _ := service.GenerateId()
	mockUser := fixture.GetMockUser()
	mockUser.ID = uid

	router := gin.Default()
	router.Use(func(c *gin.Context) {
		c.Set("userId", uid)
	})
	store := cookie.NewStore([]byte("secret"))
	router.Use(sessions.Sessions("mqk", store))

	router.Use(func(c *gin.Context) {
		session := sessions.Default(c)
		session.Set("userId", uid)
	})

	mockUserService := new(mocks.UserService)
	mockUserService.On("Get", uid).Return(mockUser, nil)

	NewHandler(&Config{
		R:            router,
		UserService:  mockUserService,
		MaxBodyBytes: 4 * 1024 * 1024,
	})

	testCases := []struct {
		name string
		body url.Values
	}{
		{
			name: "Invalid Email",
			body: map[string][]string{
				"email":       {"notanemail"},
				"username":    {fixture.Username()},
				"displayName": {fixture.DisplayName()},
				"bio":         {fixture.RandStringRunes(100)},
			},
		},
		{
			name: "Username too short",
			body: map[string][]string{
				"email":       {fixture.Email()},
				"username":    {fixture.RandStringRunes(3)},
				"displayName": {fixture.DisplayName()},
				"bio":         {fixture.RandStringRunes(100)},
			},
		},
		{
			name: "Username too long",
			body: map[string][]string{
				"email":       {fixture.Email()},
				"username":    {fixture.RandStringRunes(16)},
				"displayName": {fixture.DisplayName()},
				"bio":         {fixture.RandStringRunes(100)},
			},
		},
		{
			name: "Username not alpha numeric",
			body: map[string][]string{
				"email":       {fixture.Email()},
				"username":    {"Test User"},
				"displayName": {fixture.DisplayName()},
				"bio":         {fixture.RandStringRunes(100)},
			},
		},
		{
			name: "DisplayName too short",
			body: map[string][]string{
				"email":       {fixture.Email()},
				"username":    {fixture.Username()},
				"displayName": {fixture.RandStringRunes(3)},
				"bio":         {fixture.RandStringRunes(100)},
			},
		},
		{
			name: "DisplayName too long",
			body: map[string][]string{
				"email":       {fixture.Email()},
				"username":    {fixture.Username()},
				"displayName": {fixture.RandStringRunes(51)},
				"bio":         {fixture.RandStringRunes(100)},
			},
		},
		{
			name: "Bio too long",
			body: map[string][]string{
				"email":       {fixture.Email()},
				"username":    {fixture.Username()},
				"displayName": {fixture.DisplayName()},
				"bio":         {fixture.RandStringRunes(161)},
			},
		},
		{
			name: "Email required",
			body: map[string][]string{
				"username":    {fixture.Username()},
				"displayName": {fixture.DisplayName()},
				"bio":         {fixture.RandStringRunes(160)},
			},
		},
		{
			name: "Username required",
			body: map[string][]string{
				"email":       {fixture.Email()},
				"displayName": {fixture.DisplayName()},
				"bio":         {fixture.RandStringRunes(160)},
			},
		},
		{
			name: "DisplayName required",
			body: map[string][]string{
				"email":    {fixture.Email()},
				"username": {fixture.Username()},
				"bio":      {fixture.RandStringRunes(160)},
			},
		},
	}

	for i := range testCases {
		tc := testCases[i]

		t.Run(tc.name, func(t *testing.T) {

			rr := httptest.NewRecorder()

			form := tc.body
			request, _ := http.NewRequest(http.MethodPut, "/v1/accounts", strings.NewReader(form.Encode()))
			request.Form = form

			router.ServeHTTP(rr, request)

			assert.Equal(t, http.StatusBadRequest, rr.Code)
			mockUserService.AssertNotCalled(t, "Update")
		})
	}
}
