package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"net/http"
)

func (h *Handler) Feed(c *gin.Context) {
	authUser := c.MustGet("userId").(string)
	cursor := c.Query("cursor")

	posts, err := h.PostService.GetUserFeed(authUser, cursor)

	if err != nil {
		e := apperrors.NewNotFound("feed", authUser)

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	response := make([]model.PostResponse, 0)

	if len(*posts) > 0 {
		for i, p := range *posts {
			if i != model.LIMIT {
				post := p.NewFeedResponse(authUser)
				response = append(response, post)
			}
		}
	}

	c.JSON(http.StatusOK, gin.H{
		"posts":   response,
		"hasMore": len(*posts) == model.LIMIT+1,
	})
}
