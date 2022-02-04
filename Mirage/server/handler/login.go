package handler

import (
	"github.com/gin-gonic/gin"
	validation "github.com/go-ozzo/ozzo-validation/v4"
	"github.com/go-ozzo/ozzo-validation/v4/is"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"net/http"
	"strings"
)

type loginReq struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

func (r loginReq) Validate() error {
	return validation.ValidateStruct(&r,
		validation.Field(&r.Email, validation.Required, is.Email),
		validation.Field(&r.Password, validation.Required, validation.Length(6, 150)),
	)
}

func (r *loginReq) Sanitize() {
	r.Email = strings.TrimSpace(r.Email)
	r.Email = strings.ToLower(r.Email)
	r.Password = strings.TrimSpace(r.Password)
}

// Login handler
func (h *Handler) Login(c *gin.Context) {
	var req loginReq

	if ok := bindData(c, &req); !ok {
		return
	}

	req.Sanitize()

	user, err := h.UserService.Login(req.Email, req.Password)

	if err != nil {
		log.Printf("Failed to sign in user: %v\n", err.Error())
		c.JSON(apperrors.Status(err), gin.H{
			"error": err,
		})
		return
	}

	setUserSession(c, user.ID)

	c.JSON(http.StatusOK, user.NewAccountResponse())
}
