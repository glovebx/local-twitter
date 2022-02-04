package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

func (h *Handler) ToggleFollow(c *gin.Context) {
	userId := c.MustGet("userId").(string)
	username := c.Param("username")

	user, err := h.UserService.FindByUsername(username)

	if err != nil {
		log.Printf("Unable to find user: %v\n%v", username, err)
		e := apperrors.NewNotFound("profile", username)

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	err = h.UserService.ChangeFollow(user, userId)

	if err != nil {
		log.Printf("Failed to change follow status: %v\n", err)

		c.JSON(apperrors.Status(err), gin.H{
			"error": err,
		})
		return
	}

	user, _ = h.UserService.FindByUsername(username)

	c.JSON(http.StatusOK, user.NewProfileResponse(userId))
}
