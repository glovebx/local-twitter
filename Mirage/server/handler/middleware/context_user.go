package middleware

import (
	"fmt"
	"github.com/gin-contrib/sessions"
	"github.com/gin-gonic/gin"
)

func ContextUser() gin.HandlerFunc {
	return func(c *gin.Context) {
		session := sessions.Default(c)
		id := session.Get("userId")

		if id == nil {
			c.Next()
			return
		}

		userId := id.(string)

		c.Set("userId", userId)

		// Recreate session to extend its lifetime
		session.Set("userId", id)
		if err := session.Save(); err != nil {
			fmt.Println(err)
		}

		c.Next()
	}
}
