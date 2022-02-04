package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

func (h *Handler) GetPost(c *gin.Context) {
	postId := c.Param("id")

	var userId string
	value, exists := c.Get("userId")

	if exists {
		userId = value.(string)
	}

	post, err := h.PostService.FindPostByID(postId)

	if err != nil {
		log.Printf("Unable to find post: %v\n%v", postId, err)
		e := apperrors.NewNotFound("post", postId)

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	c.JSON(http.StatusOK, post.NewPostResponse(userId))
}
