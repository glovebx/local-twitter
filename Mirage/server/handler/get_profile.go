package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

func (h *Handler) GetProfile(c *gin.Context) {
	username := c.Param("username")

	var userId string
	value, exists := c.Get("userId")

	if exists {
		userId = value.(string)
	}

	user, err := h.UserService.FindByUsername(username)

	if err != nil {
		log.Printf("Unable to find user: %v\n%v", username, err)
		e := apperrors.NewNotFound("profile", username)

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	c.JSON(http.StatusOK, user.NewProfileResponse(userId))
}
