package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

// Current handler calls services for getting
// a user's details
func (h *Handler) Current(c *gin.Context) {
	userId := c.MustGet("userId").(string)
	user, err := h.UserService.Get(userId)

	if err != nil {
		log.Printf("Unable to find user: %v\n%v", userId, err)
		e := apperrors.NewNotFound("user", userId)

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	c.JSON(http.StatusOK, user.NewAccountResponse())
}
