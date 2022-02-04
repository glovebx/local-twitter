package handler

import (
	"errors"
	"fmt"
	validation "github.com/go-ozzo/ozzo-validation/v4"
	"github.com/go-ozzo/ozzo-validation/v4/is"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"mime/multipart"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

type editAccountReq struct {
	Username    string                `form:"username"`
	DisplayName string                `form:"displayName"`
	Email       string                `form:"email"`
	Bio         *string               `form:"bio"`
	Image       *multipart.FileHeader `form:"image"`
	Banner      *multipart.FileHeader `form:"banner"`
}

func (r editAccountReq) Validate() error {
	return validation.ValidateStruct(&r,
		validation.Field(&r.Email, validation.Required, is.Email),
		validation.Field(&r.Username, validation.Required, validation.Length(4, 15), is.Alphanumeric),
		validation.Field(&r.DisplayName, validation.Required, validation.Length(4, 50)),
		validation.Field(&r.Bio, validation.Length(0, 160)),
	)
}

func (r *editAccountReq) Sanitize() {
	r.Username = strings.TrimSpace(r.Username)
	r.DisplayName = strings.TrimSpace(r.DisplayName)
	r.Email = strings.TrimSpace(r.Email)
	r.Email = strings.ToLower(r.Email)
	if r.Bio != nil {
		bio := strings.TrimSpace(*r.Bio)
		r.Bio = &bio
	}
}

// EditAccount handler
func (h *Handler) EditAccount(c *gin.Context) {
	userId := c.MustGet("userId").(string)

	var req editAccountReq

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

	authUser.Username = req.Username
	authUser.Email = req.Email
	authUser.DisplayName = req.DisplayName
	authUser.Bio = req.Bio

	if req.Image != nil {

		// Validate image mime-type is allowable
		mimeType := req.Image.Header.Get("Content-Type")

		if valid := isAllowedImageType(mimeType); !valid {
			e := apperrors.NewBadRequest("image must be 'image/jpeg' or 'image/png'")
			c.JSON(e.Status(), gin.H{
				"error": e,
			})
			return
		}

		directory := fmt.Sprintf("profile_images/%s", authUser.ID)
		url, err := h.UserService.ChangeAvatar(req.Image, directory)

		if err != nil {
			c.JSON(500, gin.H{
				"error": err,
			})
			return
		}

		_ = h.UserService.DeleteImage(authUser.Image)

		authUser.Image = url
	}

	if req.Banner != nil {
		// Validate image mime-type is allowable
		mimeType := req.Banner.Header.Get("Content-Type")

		if valid := isAllowedImageType(mimeType); !valid {
			e := apperrors.NewBadRequest("image must be 'image/jpeg' or 'image/png'")
			c.JSON(e.Status(), gin.H{
				"error": e,
			})
			return
		}

		directory := fmt.Sprintf("header_photo/%s", authUser.ID)
		url, err := h.UserService.ChangeBanner(req.Banner, directory)

		if err != nil {
			c.JSON(500, gin.H{
				"error": err,
			})
			return
		}

		if authUser.Banner != nil {
			_ = h.UserService.DeleteImage(authUser.Image)
		}

		authUser.Banner = &url
	}

	err = h.UserService.Update(authUser)

	if err != nil {
		log.Printf("Failed to update user: %v\n", err)

		c.JSON(apperrors.Status(err), gin.H{
			"error": err,
		})
		return
	}

	c.JSON(http.StatusOK, authUser.NewAccountResponse())
}
