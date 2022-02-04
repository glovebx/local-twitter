package handler

import (
	"errors"
	"github.com/gin-gonic/gin"
	validation "github.com/go-ozzo/ozzo-validation/v4"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"mime/multipart"
	"net/http"
	"strings"
)

type createPostReq struct {
	Text *string               `form:"text"`
	File *multipart.FileHeader `form:"file"`
}

func (r createPostReq) Validate() error {
	return validation.ValidateStruct(&r,
		validation.Field(&r.Text,
			validation.Required.When(r.File == nil).
				Error("text is required if no files are provided"),
			validation.Length(1, 280),
		),
	)
}

func (r *createPostReq) Sanitize() {
	if r.Text != nil {
		text := strings.TrimSpace(*r.Text)
		r.Text = &text
	}
}

// CreatePost handler
func (h *Handler) CreatePost(c *gin.Context) {
	userId := c.MustGet("userId").(string)

	var req createPostReq

	if ok := bindData(c, &req); !ok {
		return
	}

	req.Sanitize()

	authUser, err := h.UserService.Get(userId)

	if err != nil {
		err := errors.New("provided session is invalid")
		c.JSON(401, gin.H{
			"error": err,
		})
		c.Abort()
		return
	}

	initial := &model.Post{
		UserID: authUser.ID,
		User:   *authUser,
	}

	initial.Text = req.Text

	if req.File != nil {

		// Validate image mime-type is allowable
		mimeType := req.File.Header.Get("Content-Type")

		if valid := isAllowedImageType(mimeType); !valid {
			e := apperrors.NewBadRequest("image must be 'image/jpeg' or 'image/png'")
			c.JSON(e.Status(), gin.H{
				"error": e,
			})
			return
		}

		file, err := h.PostService.UploadFile(req.File)

		if err != nil {
			c.JSON(500, gin.H{
				"error": err,
			})
			return
		}

		initial.File = file
	}

	post, err := h.PostService.CreatePost(initial)

	if err != nil {
		log.Printf("Failed to create post: %v\n", err)

		c.JSON(apperrors.Status(err), gin.H{
			"error": err,
		})
		return
	}

	c.JSON(http.StatusCreated, post.NewPostResponse(""))
}
