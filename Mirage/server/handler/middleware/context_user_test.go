package middleware

import (
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/sentrionic/mirage/service"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

func TestOptionalAuth(t *testing.T) {
	gin.SetMode(gin.TestMode)

	uid, _ := service.GenerateId()

	t.Run("Adds an userId to context when session is valid", func(t *testing.T) {
		mockProfile := fixture.GetMockUser()
		rr := httptest.NewRecorder()

		_, r := gin.CreateTestContext(rr)
		store := cookie.NewStore([]byte("secret"))
		r.Use(sessions.Sessions("mqk", store))

		r.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", uid)
		})

		var contextUserId string

		r.GET("/v1/profiles/"+mockProfile.Username, ContextUser(), func(c *gin.Context) {
			contextKeyVal, _ := c.Get("userId")
			contextUserId = contextKeyVal.(string)
		})

		request, _ := http.NewRequest(http.MethodGet, "/v1/profiles/"+mockProfile.Username, http.NoBody)
		r.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, contextUserId, uid)
	})

	t.Run("Allows access without setting the user context", func(t *testing.T) {
		mockProfile := fixture.GetMockUser()
		rr := httptest.NewRecorder()

		// creates a test context and gin engine
		_, r := gin.CreateTestContext(rr)
		store := cookie.NewStore([]byte("secret"))
		r.Use(sessions.Sessions("mqk", store))

		var contextUserId string

		r.GET("/v1/profiles/"+mockProfile.Username, ContextUser(), func(c *gin.Context) {
			contextKeyVal, exists := c.Get("userId")
			if exists {
				contextUserId = contextKeyVal.(string)
			}
		})

		request, _ := http.NewRequest(http.MethodGet, "/v1/profiles/"+mockProfile.Username, http.NoBody)

		r.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, "", contextUserId)
	})
}
