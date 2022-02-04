package handler

import (
	"github.com/gin-gonic/gin"
	validation "github.com/go-ozzo/ozzo-validation/v4"
	"github.com/go-ozzo/ozzo-validation/v4/is"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
	"strings"
)

type registerReq struct {
	Email       string `json:"email"`
	Username    string `json:"username"`
	DisplayName string `json:"displayName"`
	Password    string `json:"password"`
}

func (r registerReq) Validate() error {
	return validation.ValidateStruct(&r,
		validation.Field(&r.Email, validation.Required, is.Email),
		validation.Field(&r.Username, validation.Required, validation.Length(4, 15), is.Alphanumeric),
		validation.Field(&r.DisplayName, validation.Required, validation.Length(4, 50)),
		validation.Field(&r.Password, validation.Required, validation.Length(6, 150)),
	)
}

func (r *registerReq) Sanitize() {
	r.Username = strings.TrimSpace(r.Username)
	r.DisplayName = strings.TrimSpace(r.DisplayName)
	r.Email = strings.TrimSpace(r.Email)
	r.Email = strings.ToLower(r.Email)
	r.Password = strings.TrimSpace(r.Password)
}

// Register handler
func (h *Handler) Register(c *gin.Context) {
	var req registerReq

	// Bind incoming json to struct and check for validation errors
	if ok := bindData(c, &req); !ok {
		return
	}

	req.Sanitize()

	initial := &model.User{
		Email:       req.Email,
		Username:    req.Username,
		Password:    req.Password,
		DisplayName: req.DisplayName,
	}

	user, err := h.UserService.Register(initial)

	if err != nil {
		log.Printf("Failed to sign up user: %v\n", err.Error())
		c.JSON(apperrors.Status(err), gin.H{
			"error": err,
		})
		return
	}

	setUserSession(c, user.ID)

	c.JSON(http.StatusCreated, user.NewAccountResponse())
}
