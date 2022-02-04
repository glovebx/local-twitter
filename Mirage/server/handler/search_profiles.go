package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

func (h *Handler) SearchProfiles(c *gin.Context) {
	search := c.Query("search")

	userId := c.MustGet("userId").(string)

	users, err := h.UserService.Search(search)

	if err != nil {
		log.Printf("Unable to find profiles for term: %v\n%v", search, err)
		e := apperrors.NewNotFound("profiles", search)

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	response := make([]model.Profile, 0)

	if len(*users) > 0 {
		for _, p := range *users {
			profile := p.NewProfileResponse(userId)
			response = append(response, profile)
		}
	}

	c.JSON(http.StatusOK, response)
}
