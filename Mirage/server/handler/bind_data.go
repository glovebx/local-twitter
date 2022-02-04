package handler

import (
	"github.com/sentrionic/mirage/model/apperrors"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

// used to help extract validation errors
type fieldError struct {
	Field   string `json:"field"`
	Message string `json:"message"`
}

type Request interface {
	Validate() error
}

// bindData is helper function, returns false if data is not bound
func bindData(c *gin.Context, req Request) bool {
	// Bind incoming json to struct and check for validation errors
	if err := c.ShouldBind(req); err != nil {
		c.JSON(apperrors.Status(err), gin.H{
			"error": err,
		})
		return false
	}

	if err := req.Validate(); err != nil {
		errors := strings.Split(err.Error(), ";")
		fErrors := make([]fieldError, 0)

		for _, e := range errors {
			split := strings.Split(e, ":")
			er := fieldError{
				Field:   strings.TrimSpace(split[0]),
				Message: strings.TrimSpace(split[1]),
			}
			fErrors = append(fErrors, er)
		}

		c.JSON(http.StatusBadRequest, gin.H{
			"errors": fErrors,
		})
		return false
	}
	return true
}
