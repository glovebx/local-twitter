package handler

import (
	"github.com/gin-gonic/gin"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
)

func (h *Handler) DeletePost(c *gin.Context) {
	postId := c.Param("id")

	userId := c.MustGet("userId").(string)

	post, err := h.PostService.FindPostByID(postId)

	if err != nil {
		log.Printf("Unable to find post: %v\n%v", postId, err)
		e := apperrors.NewNotFound("post", postId)

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	if post.UserID != userId {
		e := apperrors.NewAuthorization("you are not the owner")

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	err = h.PostService.DeletePost(post)

	if err != nil {
		log.Printf("Unable to delete post: %v\n%v", postId, err)
		e := apperrors.NewInternal()

		c.JSON(e.Status(), gin.H{
			"error": e,
		})
		return
	}

	c.JSON(http.StatusOK, post.NewPostResponse(userId))
}
