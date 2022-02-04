package fixture

import (
	"fmt"
	"github.com/sentrionic/mirage/model"
	"time"
)

func GetMockUser() *model.User {
	email := Email()
	return &model.User{
		ID:          RandID(),
		Username:    Username(),
		DisplayName: DisplayName(),
		Email:       email,
		Password:    RandStr(8),
		Image:       fmt.Sprintf("https://gravatar.com/avatar/%s?d=identicon", getMD5Hash(email)),
		CreatedAt:   time.Now(),
		UpdatedAt:   time.Now(),
	}
}
