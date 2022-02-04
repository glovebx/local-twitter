package handler

import (
	"encoding/json"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/cookie"
	"github.com/sentrionic/mirage/service"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"

	"github.com/stretchr/testify/assert"
)

func TestLogout(t *testing.T) {
	gin.SetMode(gin.TestMode)

	t.Run("Success", func(t *testing.T) {
		uid, _ := service.GenerateId()

		rr := httptest.NewRecorder()

		// creates a test context for setting a user
		router := gin.Default()
		store := cookie.NewStore([]byte("secret"))
		router.Use(sessions.Sessions("mqk", store))
		router.Use(func(c *gin.Context) {
			c.Set("userId", uid)
			session := sessions.Default(c)
			session.Set("userId", uid)
		})

		NewHandler(&Config{
			R: router,
		})

		request, _ := http.NewRequest(http.MethodPost, "/v1/accounts/logout", nil)
		router.ServeHTTP(rr, request)

		respBody, _ := json.Marshal(true)

		assert.Equal(t, http.StatusOK, rr.Code)
		assert.Equal(t, respBody, rr.Body.Bytes())

		router.Use(func(c *gin.Context) {
			contextUserId, exists := c.Get("userId")
			assert.Equal(t, exists, false)
			assert.Nil(t, contextUserId)

			session := sessions.Default(c)
			id := session.Get("userId")
			assert.Nil(t, id)
		})
	})
}
