package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

func (h *Handler) LikePost(c *gin.Context) {
	userId := c.MustGet("userId").(string)
	postId := c.Param("id")

	post, err := h.PostService.FindPostByID(postId)

	if err != nil {
		log.Printf("Unable to find post: %v\n%v", postId, err)
		e := apperrors.NewNotFound("post", postId)

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	err = h.PostService.ToggleLike(post, userId)

	if err != nil {
		log.Printf("Failed to change like status: %v\n", err)

		c.JSON(apperrors.Status(err), gin.H{
			"error": err,
		})
		return
	}

	post, _ = h.PostService.FindPostByID(postId)

	c.JSON(http.StatusOK, post.NewPostResponse(userId))
}
