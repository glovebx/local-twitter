package handler

import (
	"fmt"
	"github.com/gin-contrib/sessions"
	"net/http"

	"github.com/gin-gonic/gin"
)

// Logout handler
func (h *Handler) Logout(c *gin.Context) {
	c.Set("userId", nil)

	session := sessions.Default(c)
	session.Set("userId", "")
	session.Clear()
	session.Options(sessions.Options{Path: "/", MaxAge: -1})
	err := session.Save()

	if err != nil {
		fmt.Printf("error clearing session: %v", err)
	}

	c.JSON(http.StatusOK, true)
}
