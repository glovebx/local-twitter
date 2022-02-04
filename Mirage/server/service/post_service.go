package service

import (
	"github.com/lucsky/cuid"
	"github.com/sentrionic/mirage/model"
	"github.com/sentrionic/mirage/model/apperrors"
	"log"
	"mime/multipart"
	"path"
)

type postService struct {
	PostRepository model.PostRepository
	FileRepository model.FileRepository
}

// PSConfig will hold repositories that will eventually be injected into this
// this service layer
type PSConfig struct {
	PostRepository model.PostRepository
	FileRepository model.FileRepository
}

// NewPostService is a factory function for
// initializing a PostService with its repository layer dependencies
func NewPostService(c *PSConfig) model.PostService {
	return &postService{
		PostRepository: c.PostRepository,
		FileRepository: c.FileRepository,
	}
}

func (p *postService) FindPostByID(id string) (*model.Post, error) {
	return p.PostRepository.FindByID(id)
}

func (p *postService) CreatePost(post *model.Post) (*model.Post, error) {
	id, err := GenerateId()

	if err != nil {
		log.Printf("Unable to create post for author: %v\n", post.UserID)
		return nil, apperrors.NewInternal()
	}
	post.ID = id

	if post.Text != nil {
		post.HashTags = GetHashtags(*post.Text)
	}

	return p.PostRepository.Create(post)
}

func (p *postService) DeletePost(post *model.Post) error {
	if post.File != nil {
		err := p.FileRepository.DeleteImage(post.File.Filename)
		if err != nil {
			return err
		}
	}

	return p.PostRepository.Delete(post)
}

func (p *postService) UploadFile(header *multipart.FileHeader) (*model.File, error) {
	slug := cuid.New()
	ext := path.Ext(header.Filename)
	filename := slug + ext
	mimetype := header.Header.Get("Content-Type")

	file := model.File{
		FileType: mimetype,
		Filename: filename,
	}

	id, err := GenerateId()
	if err != nil {
		return nil, err
	}

	file.ID = id

	directory := "media/"
	url, err := p.FileRepository.UploadFile(header, directory, filename, mimetype)

	if err != nil {
		return nil, err
	}

	file.Url = url

	return &file, nil
}

func (p *postService) ToggleLike(post *model.Post, uid string) error {
	if post.IsLiked(uid) {
		return p.PostRepository.RemoveLike(post, uid)
	} else {
		return p.PostRepository.AddLike(post, uid)
	}
}

func (p *postService) ToggleRetweet(post *model.Post, uid string) error {
	if post.IsRetweeted(uid) {
		return p.PostRepository.RemoveRetweet(post, uid)
	} else {
		return p.PostRepository.AddRetweet(post, uid)
	}
}

func (p *postService) GetUserFeed(userId, cursor string) (*[]model.Post, error) {
	return p.PostRepository.Feed(userId, cursor)
}

func (p *postService) ProfilePosts(id, cursor string) (*[]model.Post, error) {
	return p.PostRepository.List(id, cursor)
}

func (p *postService) ProfileLikes(id, cursor string) (*[]model.Post, error) {
	return p.PostRepository.Likes(id, cursor)
}

func (p *postService) SearchPosts(tag, cursor string) (*[]model.Post, error) {
	return p.PostRepository.GetPostsForHashtag(tag, cursor)
}

func (p *postService) ProfileMedia(id, cursor string) (*[]model.Post, error) {
	return p.PostRepository.Media(id, cursor)
}
