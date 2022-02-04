package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

func (h *Handler) GetProfileMedia(c *gin.Context) {
	username := c.Param("username")
	cursor := c.Query("cursor")

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

	posts, err := h.PostService.ProfileMedia(user.ID, cursor)

	if err != nil {
		log.Printf("Unable to find media posts for user: %v\n%v", username, err)
		e := apperrors.NewNotFound("posts", username)

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	response := make([]model.PostResponse, 0)

	if len(*posts) > 0 {
		for i, p := range *posts {
			if i != model.LIMIT {
				post := p.NewPostResponse(userId)
				response = append(response, post)
			}
		}
	}

	c.JSON(http.StatusOK, gin.H{
		"posts":   response,
		"hasMore": len(*posts) == model.LIMIT+1,
	})
}
