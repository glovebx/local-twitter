package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

func (h *Handler) GetProfilePosts(c *gin.Context) {
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

	posts, err := h.PostService.ProfilePosts(user.ID, cursor)

	if err != nil {
		log.Printf("Unable to find posts for user: %v\n%v", username, err)
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
				post := model.PostResponse{
					ID:        p.ID,
					Text:      p.Text,
					Likes:     uint(len(p.Likes)),
					Liked:     p.IsLiked(userId),
					Retweets:  uint(len(p.Retweets)),
					Retweeted: p.IsRetweeted(userId),
					IsRetweet: p.UserID != user.ID,
					File:      p.File,
					Author:    p.User.NewProfileResponse(userId),
					CreatedAt: p.CreatedAt,
				}
				response = append(response, post)
			}
		}
	}

	c.JSON(http.StatusOK, gin.H{
		"posts":   response,
		"hasMore": len(*posts) == model.LIMIT+1,
	})
}
