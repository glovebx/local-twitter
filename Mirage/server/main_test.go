package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/stretchr/testify/assert"
	"log"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"testing"
)

type PostListResponse struct {
	Posts   []model.PostResponse `json:"posts"`
	HasMore bool                 `json:"hasMore"`
}

func TestMain_E2E(t *testing.T) {
	gin.SetMode(gin.ReleaseMode)

	_ = godotenv.Load()

	ds, err := initDS()

	if err != nil {
		log.Fatalf("Unable to initialize data sources: %v\n", err)
	}

	router, err := inject(ds)

	if err != nil {
		log.Fatalf("Failure to inject data sources: %v\n", err)
	}

	if err := ds.close(); err != nil {
		log.Fatalf("A problem occured gracefully shutting down data sources: %v\n", err)
	}

	mockUser := fixture.GetMockUser()
	bio := fixture.RandStringRunes(100)
	mockUser.Bio = &bio
	cookie := ""

	mockProfile := fixture.GetMockUser()
	profileCookie := ""

	userPost := fixture.GetMockPost()

	profilePost := fixture.GetMockPost()
	tag := fixture.RandStringRunes(4)
	text := fmt.Sprintf("%s #%s", *profilePost.Text, tag)
	profilePost.Text = &text

	testCases := []struct {
		name          string
		setupRequest  func() (*http.Request, error)
		setupHeaders  func(t *testing.T, request *http.Request)
		checkResponse func(recorder *httptest.ResponseRecorder)
	}{
		// ------------------ ACCOUNTS --------------------
		{
			name: "Register Account",
			setupRequest: func() (*http.Request, error) {
				data := gin.H{
					"username":    mockUser.Username,
					"password":    mockUser.Password,
					"displayName": mockUser.DisplayName,
					"email":       mockUser.Email,
				}

				reqBody, err := json.Marshal(data)
				assert.NoError(t, err)

				return http.NewRequest(http.MethodPost, "/v1/accounts/register", bytes.NewBuffer(reqBody))
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusCreated, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.AccountResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)

				assert.Equal(t, mockUser.Username, respBody.Username)
				assert.Equal(t, mockUser.Email, respBody.Email)
				assert.Equal(t, mockUser.DisplayName, respBody.DisplayName)
				assert.Nil(t, respBody.Bio)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.Image)
			},
		},
		{
			name: "Login Account",
			setupRequest: func() (*http.Request, error) {
				data := gin.H{
					"password": mockUser.Password,
					"email":    mockUser.Email,
				}

				reqBody, err := json.Marshal(data)
				assert.NoError(t, err)

				return http.NewRequest(http.MethodPost, "/v1/accounts/login", bytes.NewBuffer(reqBody))
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.AccountResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)

				assert.Equal(t, mockUser.Username, respBody.Username)
				assert.Equal(t, mockUser.Email, respBody.Email)
				assert.Equal(t, mockUser.DisplayName, respBody.DisplayName)
				assert.Nil(t, respBody.Bio)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.Image)
				assert.Contains(t, recorder.Header(), "Set-Cookie")

				cookie = recorder.Header().Get("Set-Cookie")
			},
		},
		{
			name: "Get Account",
			setupRequest: func() (*http.Request, error) {
				return http.NewRequest(http.MethodGet, "/v1/accounts", nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Set("Content-Type", "application/json")
				request.Header.Add("Cookie", cookie)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.AccountResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)

				assert.Equal(t, mockUser.Username, respBody.Username)
				assert.Equal(t, mockUser.Email, respBody.Email)
				assert.Equal(t, mockUser.DisplayName, respBody.DisplayName)
				assert.Nil(t, respBody.Bio)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.Image)
			},
		},
		{
			name: "Edit Account",
			setupRequest: func() (*http.Request, error) {
				form := url.Values{}
				form.Add("username", mockUser.Username)
				form.Add("email", mockUser.Email)
				form.Add("bio", *mockUser.Bio)
				form.Add("displayName", mockUser.DisplayName)

				request, err := http.NewRequest(http.MethodPut, "/v1/accounts", strings.NewReader(form.Encode()))

				if err != nil {
					return nil, err
				}

				request.Form = form

				return request, nil
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.AccountResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)

				assert.Equal(t, mockUser.Username, respBody.Username)
				assert.Equal(t, mockUser.Email, respBody.Email)
				assert.Equal(t, mockUser.DisplayName, respBody.DisplayName)
				assert.Equal(t, *mockUser.Bio, *respBody.Bio)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.Image)
			},
		},
		// ------------------ Profiles --------------------
		{
			name: "Register Profile",
			setupRequest: func() (*http.Request, error) {
				data := gin.H{
					"username":    mockProfile.Username,
					"password":    mockProfile.Password,
					"displayName": mockProfile.DisplayName,
					"email":       mockProfile.Email,
				}

				reqBody, err := json.Marshal(data)
				assert.NoError(t, err)

				return http.NewRequest(http.MethodPost, "/v1/accounts/register", bytes.NewBuffer(reqBody))
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusCreated, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.AccountResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)

				assert.Equal(t, mockProfile.Username, respBody.Username)
				assert.Equal(t, mockProfile.Email, respBody.Email)
				assert.Equal(t, mockProfile.DisplayName, respBody.DisplayName)
				assert.Nil(t, respBody.Bio)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.Image)

				mockProfile.ID = respBody.ID
				profileCookie = recorder.Header().Get("Set-Cookie")
			},
		},
		{
			name: "Get Profile",
			setupRequest: func() (*http.Request, error) {
				return http.NewRequest(http.MethodGet, "/v1/profiles/"+mockProfile.Username, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.Profile{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)

				assert.Equal(t, mockProfile.Username, respBody.Username)
				assert.Equal(t, mockProfile.DisplayName, respBody.DisplayName)
				assert.Equal(t, mockProfile.ID, respBody.ID)
				assert.Equal(t, mockProfile.Image, respBody.Image)
				assert.NotNil(t, respBody.CreatedAt)
				assert.Equal(t, uint(0), respBody.Followers)
				assert.Equal(t, uint(0), respBody.Followee)
				assert.Equal(t, false, respBody.Following)
			},
		},
		{
			name: "Create Profile Post",
			setupRequest: func() (*http.Request, error) {
				form := url.Values{}
				form.Add("text", *profilePost.Text)

				request, err := http.NewRequest(http.MethodPost, "/v1/posts", strings.NewReader(form.Encode()))

				if err != nil {
					return nil, err
				}

				request.Form = form

				return request, nil
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", profileCookie)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusCreated, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.PostResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)

				assert.Equal(t, profilePost.Text, respBody.Text)
				assert.Equal(t, uint(0), respBody.Likes)
				assert.Equal(t, uint(0), respBody.Retweets)
				assert.Equal(t, false, respBody.Liked)
				assert.Equal(t, false, respBody.Retweeted)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.Author)
				assert.NotNil(t, respBody.CreatedAt)
				assert.Nil(t, respBody.File)

				author := respBody.Author

				assert.Equal(t, mockProfile.Username, author.Username)
				assert.Equal(t, mockProfile.DisplayName, author.DisplayName)
				assert.Equal(t, mockProfile.Image, author.Image)
				assert.NotNil(t, author.CreatedAt)
				assert.Equal(t, uint(0), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, false, author.Following)

				profilePost.ID = respBody.ID
				profilePost.UserID = mockProfile.ID
			},
		},
		//{
		//	name: "Create File Post",
		//	setupRequest: func() (*http.Request, error) {
		//		multipartImageFixture := fixture.NewMultipartImage("image.png", "image/png")
		//		defer multipartImageFixture.Close()
		//
		//		request, err := http.NewRequest(http.MethodPost, "/v1/posts", multipartImageFixture.MultipartBody)
		//
		//		if err != nil {
		//			return nil, err
		//		}
		//
		//		request.Header.Set("Content-Type", multipartImageFixture.ContentType)
		//
		//		return request, nil
		//	},
		//	setupHeaders: func(t *testing.T, request *http.Request) {
		//		request.Header.Add("Cookie", cookie)
		//	},
		//	checkResponse: func(recorder *httptest.ResponseRecorder) {
		//		assert.Equal(t, http.StatusCreated, recorder.Code)
		//		assert.NoError(t, err)
		//
		//		respBody := &model.PostResponse{}
		//		err = json.Unmarshal(recorder.Body.Bytes(), respBody)
		//		assert.NoError(t, err)
		//
		//		assert.Nil(t, respBody.Text)
		//		assert.Equal(t, uint(0), respBody.Likes)
		//		assert.Equal(t, uint(0), respBody.Retweets)
		//		assert.Equal(t, false, respBody.Liked)
		//		assert.Equal(t, false, respBody.Retweeted)
		//		assert.NotNil(t, respBody.ID)
		//		assert.NotNil(t, respBody.Author)
		//		assert.NotNil(t, respBody.File)
		//
		//		author := respBody.Author
		//
		//		assert.Equal(t, mockUser.Username, author.Username)
		//		assert.Equal(t, mockUser.DisplayName, author.DisplayName)
		//		assert.Equal(t, mockUser.Image, author.Image)
		//		assert.Equal(t, uint(0), author.Followers)
		//		assert.Equal(t, uint(0), author.Followee)
		//		assert.Equal(t, false, author.Following)
		//		assert.Equal(t, *mockUser.Bio, *author.Bio)
		//
		//		file := respBody.File
		//		assert.NotNil(t, file.Url)
		//		assert.NotNil(t, file.Filename)
		//		assert.NotNil(t, file.FileType)
		//
		//		userPost.ID = respBody.ID
		//		userPost.UserID = author.ID
		//	},
		//},
		{
			name: "Create User Post",
			setupRequest: func() (*http.Request, error) {
				form := url.Values{}
				form.Add("text", *userPost.Text)

				request, err := http.NewRequest(http.MethodPost, "/v1/posts", strings.NewReader(form.Encode()))

				if err != nil {
					return nil, err
				}

				request.Form = form

				return request, nil
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusCreated, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.PostResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)

				assert.Equal(t, userPost.Text, respBody.Text)
				assert.Equal(t, uint(0), respBody.Likes)
				assert.Equal(t, uint(0), respBody.Retweets)
				assert.Equal(t, false, respBody.Liked)
				assert.Equal(t, false, respBody.Retweeted)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.Author)
				assert.NotNil(t, respBody.CreatedAt)
				assert.Nil(t, respBody.File)

				author := respBody.Author

				assert.Equal(t, mockUser.Username, author.Username)
				assert.Equal(t, mockUser.DisplayName, author.DisplayName)
				assert.Equal(t, mockUser.Image, author.Image)
				assert.Equal(t, uint(0), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, false, author.Following)
				assert.Equal(t, *mockUser.Bio, *author.Bio)

				userPost.ID = respBody.ID
				userPost.UserID = author.ID
			},
		},
		{
			name: "Profile likes User's post",
			setupRequest: func() (*http.Request, error) {
				requestUrl := fmt.Sprintf("/v1/posts/%s/like", userPost.ID)
				return http.NewRequest(http.MethodPost, requestUrl, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", profileCookie)
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.PostResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)

				assert.NotNil(t, respBody.Text)
				assert.Equal(t, uint(1), respBody.Likes)
				assert.Equal(t, uint(0), respBody.Retweets)
				assert.Equal(t, true, respBody.Liked)
				assert.Equal(t, false, respBody.Retweeted)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.Author)
				assert.Nil(t, respBody.File)

				author := respBody.Author

				assert.Equal(t, mockUser.Username, author.Username)
				assert.Equal(t, mockUser.DisplayName, author.DisplayName)
				assert.Equal(t, mockUser.Image, author.Image)
				assert.NotNil(t, respBody.CreatedAt)
				assert.Equal(t, uint(0), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, false, author.Following)
			},
		},
		{
			name: "Get profile's posts",
			setupRequest: func() (*http.Request, error) {
				requestUrl := fmt.Sprintf("/v1/profiles/%s/posts", mockProfile.Username)
				return http.NewRequest(http.MethodGet, requestUrl, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &PostListResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)

				assert.NotEmpty(t, respBody.Posts)
				assert.Equal(t, 1, len(respBody.Posts))
				assert.False(t, respBody.HasMore)

				post := respBody.Posts[0]

				assert.NotNil(t, post.Text)
				assert.Equal(t, uint(0), post.Likes)
				assert.Equal(t, uint(0), post.Retweets)
				assert.Equal(t, false, post.Liked)
				assert.Equal(t, false, post.Retweeted)
				assert.NotNil(t, post.CreatedAt)
				assert.NotNil(t, post.ID)
				assert.NotNil(t, post.Author)
				assert.Nil(t, post.File)

				author := post.Author

				assert.Equal(t, mockProfile.Username, author.Username)
				assert.Equal(t, mockProfile.DisplayName, author.DisplayName)
				assert.Equal(t, mockProfile.Image, author.Image)
				assert.NotNil(t, mockProfile.CreatedAt)
				assert.Equal(t, uint(0), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, false, author.Following)
			},
		},
		{
			name: "Get profile's liked posts",
			setupRequest: func() (*http.Request, error) {
				requestUrl := fmt.Sprintf("/v1/profiles/%s/likes", mockProfile.Username)
				return http.NewRequest(http.MethodGet, requestUrl, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &PostListResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)

				assert.NotEmpty(t, respBody.Posts)
				assert.Equal(t, 1, len(respBody.Posts))
				assert.False(t, respBody.HasMore)

				post := respBody.Posts[0]

				assert.NotNil(t, post.Text)
				assert.Equal(t, uint(1), post.Likes)
				assert.Equal(t, uint(0), post.Retweets)
				assert.Equal(t, false, post.Liked)
				assert.Equal(t, false, post.Retweeted)
				assert.NotNil(t, post.ID)
				assert.NotNil(t, post.Author)
				assert.NotNil(t, post.CreatedAt)
				assert.Nil(t, post.File)

				author := post.Author

				assert.Equal(t, mockUser.Username, author.Username)
				assert.Equal(t, mockUser.DisplayName, author.DisplayName)
				assert.Equal(t, mockUser.Image, author.Image)
				assert.NotNil(t, author.CreatedAt)
				assert.Equal(t, uint(0), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, false, author.Following)
			},
		},
		{
			name: "Get profiles for search term",
			setupRequest: func() (*http.Request, error) {
				requestUrl := fmt.Sprintf("/v1/profiles?search=%s", mockProfile.Username)
				return http.NewRequest(http.MethodGet, requestUrl, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &[]model.Profile{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)
				assert.NotNil(t, respBody)

				profiles := *respBody
				assert.Equal(t, 1, len(profiles))
				profile := profiles[0]

				assert.Equal(t, mockProfile.Username, profile.Username)
				assert.Equal(t, mockProfile.DisplayName, profile.DisplayName)
				assert.Equal(t, mockProfile.Image, profile.Image)
				assert.NotNil(t, profile.CreatedAt)
				assert.Equal(t, uint(0), profile.Followers)
				assert.Equal(t, uint(0), profile.Followee)
				assert.Equal(t, false, profile.Following)
			},
		},
		{
			name: "Follow Profile",
			setupRequest: func() (*http.Request, error) {
				requestUrl := fmt.Sprintf("/v1/profiles/%s/follow", mockProfile.Username)
				return http.NewRequest(http.MethodPost, requestUrl, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.Profile{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)

				assert.Equal(t, mockProfile.Username, respBody.Username)
				assert.Equal(t, mockProfile.DisplayName, respBody.DisplayName)
				assert.Equal(t, mockProfile.ID, respBody.ID)
				assert.Equal(t, mockProfile.Image, respBody.Image)
				assert.NotNil(t, respBody.CreatedAt)
				assert.Equal(t, uint(1), respBody.Followers)
				assert.Equal(t, uint(0), respBody.Followee)
				assert.Equal(t, true, respBody.Following)
			},
		},
		{
			name: "Get post's for hash tag",
			setupRequest: func() (*http.Request, error) {
				requestUrl := fmt.Sprintf("/v1/posts?search=%s", tag)
				return http.NewRequest(http.MethodGet, requestUrl, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &PostListResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)

				assert.NotEmpty(t, respBody.Posts)
				assert.Equal(t, 1, len(respBody.Posts))
				assert.False(t, respBody.HasMore)

				post := respBody.Posts[0]

				assert.NotNil(t, post.Text)
				assert.True(t, strings.Contains(*post.Text, tag))
				assert.Equal(t, uint(0), post.Likes)
				assert.Equal(t, uint(0), post.Retweets)
				assert.Equal(t, false, post.Liked)
				assert.Equal(t, false, post.Retweeted)
				assert.NotNil(t, post.ID)
				assert.NotNil(t, post.Author)
				assert.Nil(t, post.File)

				author := post.Author

				assert.Equal(t, mockProfile.Username, author.Username)
				assert.Equal(t, mockProfile.DisplayName, author.DisplayName)
				assert.Equal(t, mockProfile.Image, author.Image)
				assert.Equal(t, uint(1), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, true, author.Following)
			},
		},
		{
			name: "User retweets profile's post",
			setupRequest: func() (*http.Request, error) {
				requestUrl := fmt.Sprintf("/v1/posts/%s/retweet", profilePost.ID)
				return http.NewRequest(http.MethodPost, requestUrl, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				post := &model.PostResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), post)
				assert.NoError(t, err)

				assert.NotNil(t, post.Text)
				assert.Equal(t, uint(0), post.Likes)
				assert.Equal(t, uint(1), post.Retweets)
				assert.Equal(t, false, post.Liked)
				assert.Equal(t, true, post.Retweeted)
				assert.NotNil(t, post.ID)
				assert.NotNil(t, post.Author)
				assert.NotNil(t, post.CreatedAt)
				assert.Nil(t, post.File)

				author := post.Author

				assert.Equal(t, mockProfile.Username, author.Username)
				assert.Equal(t, mockProfile.DisplayName, author.DisplayName)
				assert.Equal(t, mockProfile.Image, author.Image)
				assert.Equal(t, uint(1), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, true, author.Following)
			},
		},
		{
			name: "Get user's feed",
			setupRequest: func() (*http.Request, error) {
				return http.NewRequest(http.MethodGet, "/v1/posts/feed", nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Add("Cookie", cookie)
				request.Header.Set("Content-Type", "application/json")
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &PostListResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)

				assert.NotEmpty(t, respBody.Posts)
				assert.Equal(t, 2, len(respBody.Posts))
				assert.False(t, respBody.HasMore)

				post := respBody.Posts[0]

				assert.NotNil(t, post.Text)
				assert.Equal(t, uint(0), post.Likes)
				assert.Equal(t, uint(1), post.Retweets)
				assert.Equal(t, false, post.Liked)
				assert.Equal(t, true, post.Retweeted)
				assert.NotNil(t, post.ID)
				assert.NotNil(t, post.Author)
				assert.NotNil(t, post.CreatedAt)
				assert.Nil(t, post.File)

				author := post.Author

				assert.Equal(t, mockProfile.Username, author.Username)
				assert.Equal(t, mockProfile.DisplayName, author.DisplayName)
				assert.Equal(t, mockProfile.Image, author.Image)
				assert.NotNil(t, author.CreatedAt)
				assert.Equal(t, uint(1), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, true, author.Following)
			},
		},
		{
			name: "Get Post By ID",
			setupRequest: func() (*http.Request, error) {
				return http.NewRequest(http.MethodGet, "/v1/posts/"+profilePost.ID, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Set("Content-Type", "application/json")
				request.Header.Add("Cookie", cookie)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.PostResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)

				assert.Equal(t, profilePost.Text, respBody.Text)
				assert.Equal(t, uint(0), respBody.Likes)
				assert.Equal(t, uint(1), respBody.Retweets)
				assert.Equal(t, false, respBody.Liked)
				assert.Equal(t, true, respBody.Retweeted)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.CreatedAt)
				assert.NotNil(t, respBody.Author)
				assert.Nil(t, respBody.File)

				author := respBody.Author

				assert.Equal(t, mockProfile.Username, author.Username)
				assert.Equal(t, mockProfile.DisplayName, author.DisplayName)
				assert.Equal(t, mockProfile.Image, author.Image)
				assert.Equal(t, uint(1), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, true, author.Following)
			},
		},
		{
			name: "Delete Post",
			setupRequest: func() (*http.Request, error) {
				return http.NewRequest(http.MethodDelete, "/v1/posts/"+profilePost.ID, nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Set("Content-Type", "application/json")
				request.Header.Add("Cookie", profileCookie)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)

				respBody := &model.PostResponse{}
				err = json.Unmarshal(recorder.Body.Bytes(), respBody)
				assert.NoError(t, err)

				assert.Equal(t, profilePost.Text, respBody.Text)
				assert.Equal(t, uint(0), respBody.Likes)
				assert.Equal(t, uint(1), respBody.Retweets)
				assert.Equal(t, false, respBody.Liked)
				assert.Equal(t, false, respBody.Retweeted)
				assert.NotNil(t, respBody.ID)
				assert.NotNil(t, respBody.Author)
				assert.NotNil(t, respBody.CreatedAt)
				assert.Nil(t, respBody.File)

				author := respBody.Author

				assert.Equal(t, mockProfile.Username, author.Username)
				assert.Equal(t, mockProfile.DisplayName, author.DisplayName)
				assert.Equal(t, mockProfile.Image, author.Image)
				assert.Equal(t, uint(1), author.Followers)
				assert.Equal(t, uint(0), author.Followee)
				assert.Equal(t, false, author.Following)
			},
		},
		{
			name: "Logout",
			setupRequest: func() (*http.Request, error) {
				return http.NewRequest(http.MethodPost, "/v1/accounts/logout", nil)
			},
			setupHeaders: func(t *testing.T, request *http.Request) {
				request.Header.Set("Content-Type", "application/json")
				request.Header.Add("Cookie", cookie)
			},
			checkResponse: func(recorder *httptest.ResponseRecorder) {
				assert.Equal(t, http.StatusOK, recorder.Code)
				assert.NoError(t, err)
			},
		},
	}

	for i := range testCases {
		tc := testCases[i]

		t.Run(tc.name, func(t *testing.T) {
			rr := httptest.NewRecorder()
			request, err := tc.setupRequest()
			tc.setupHeaders(t, request)
			assert.NoError(t, err)
			router.ServeHTTP(rr, request)
			tc.checkResponse(rr)
		})
	}
}
