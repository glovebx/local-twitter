package service

import (
	"github.com/sentrionic/mirage/model/fixture"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestGetHashtags(t *testing.T) {
	t.Run("Returns an empty array if no hashtags", func(t *testing.T) {
		text := fixture.RandStr(120)
		list := GetHashtags(text)
		assert.Empty(t, list)
	})

	t.Run("Returns an empty array if hashtags are at the wrong position", func(t *testing.T) {
		text := "This is a test# pos#t"
		list := GetHashtags(text)
		assert.Empty(t, list)
	})

	t.Run("Returns a list of hashtags for a given text", func(t *testing.T) {
		text := "This is a #test #post"
		list := GetHashtags(text)
		assert.Equal(t, len(list), 2)
		assert.Equal(t, list[0], "#test")
		assert.Equal(t, list[1], "#post")
	})
}
