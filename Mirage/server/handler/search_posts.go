package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

func (h *Handler) SearchPosts(c *gin.Context) {
	search := c.Query("search")
	cursor := c.Query("cursor")

	userId := c.MustGet("userId").(string)

	posts, err := h.PostService.SearchPosts(search, cursor)

	if err != nil {
		log.Printf("Unable to find posts for term: %v\n%v", search, err)
		e := apperrors.NewNotFound("posts", search)

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
