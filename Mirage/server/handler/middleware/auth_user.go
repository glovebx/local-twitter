package middleware

import (
	"errors"
	"fmt"
	"github.com/gin-contrib/sessions"

	"github.com/gin-gonic/gin"
)

// AuthUser checks if the request contains a valid session
// and saves the session's userId in the context
func AuthUser() gin.HandlerFunc {
	return func(c *gin.Context) {
		session := sessions.Default(c)
		id := session.Get("userId")

		if id == nil {
			err := errors.New("provided session is invalid")
			c.JSON(401, gin.H{
				"error": err,
			})
			c.Abort()
			return
		}

		userId := id.(string)

		c.Set("userId", userId)

		// Recreate session to extend its lifetime
		if err := session.Save(); err != nil {
			fmt.Println(err)
		}
		c.Next()
	}
}
