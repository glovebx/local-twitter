package middleware

import (
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/sentrionic/mirage/service"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

func TestAuthUser(t *testing.T) {
	gin.SetMode(gin.TestMode)

	uid, _ := service.GenerateId()

	t.Run("Adds an userId to context", func(t *testing.T) {
		rr := httptest.NewRecorder()

		_, r := gin.CreateTestContext(rr)
		store := cookie.NewStore([]byte("secret"))
		r.Use(sessions.Sessions("mqk", store))

		r.Use(func(c *gin.Context) {
			session := sessions.Default(c)
			session.Set("userId", uid)
		})

		var contextUserId string

		r.GET("/v1/accounts", AuthUser(), func(c *gin.Context) {
			contextKeyVal, _ := c.Get("userId")
			contextUserId = contextKeyVal.(string)
		})

		request, _ := http.NewRequest(http.MethodGet, "/v1/accounts", http.NoBody)
		r.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, contextUserId, uid)
	})

	t.Run("Missing Session", func(t *testing.T) {
		rr := httptest.NewRecorder()

		// creates a test context and gin engine
		_, r := gin.CreateTestContext(rr)
		store := cookie.NewStore([]byte("secret"))
		r.Use(sessions.Sessions("mqk", store))

		r.GET("/v1/accounts", AuthUser())

		request, _ := http.NewRequest(http.MethodGet, "/v1/accounts", http.NoBody)

		r.ServeHTTP(rr, request)

		assert.Equal(t, http.StatusUnauthorized, rr.Code)
	})
}
