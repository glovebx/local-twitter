package main

import (
	"fmt"
	"github.com/gin-contrib/sessions"
	"github.com/gin-contrib/sessions/redis"
	"github.com/sentrionic/mirage/handler"
	"github.com/sentrionic/mirage/repository"
	"github.com/sentrionic/mirage/service"
	"log"
	"net/http"
	"os"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	gredis "github.com/go-redis/redis/v8"
	_ "github.com/lib/pq"
)

func inject(d *dataSources) (*gin.Engine, error) {
	log.Println("Injecting data sources")

	/*
	 * repository layer
	 */
	userRepository := repository.NewUserRepository(d.DB)
	postRepository := repository.NewPostRepository(d.DB)

	bucketName := os.Getenv("AWS_STORAGE_BUCKET_NAME")
	fileRepository := repository.NewFileRepository(d.S3Session, bucketName)

	/*
	 * service layer
	 */
	userService := service.NewUserService(&service.USConfig{
		UserRepository: userRepository,
		FileRepository: fileRepository,
	})

	postService := service.NewPostService(&service.PSConfig{
		PostRepository: postRepository,
		FileRepository: fileRepository,
	})

	// initialize gin.Engine
	router := gin.Default()
	redisURL := os.Getenv("REDIS_URL")

	opt, err := gredis.ParseURL(redisURL)
	if err != nil {
		panic(err)
	}
	url := opt.Addr
	password := opt.Password

	// initialize session store
	secret := os.Getenv("SECRET")
	store, _ := redis.NewStore(10, "tcp", url, password, []byte(secret))

	domain := os.Getenv("DOMAIN")

	store.Options(sessions.Options{
		Domain:   domain,
		MaxAge:   60 * 60 * 24 * 7, // 7 days
		Secure:   gin.Mode() == gin.ReleaseMode,
		HttpOnly: true,
		Path:     "/",
		SameSite: http.SameSiteLaxMode,
	})

	cookie := os.Getenv("COOKIE_NAME")
	router.Use(sessions.Sessions(cookie, store))

	// read in HANDLER_TIMEOUT
	handlerTimeout := os.Getenv("HANDLER_TIMEOUT")
	ht, err := strconv.ParseInt(handlerTimeout, 0, 64)
	if err != nil {
		return nil, fmt.Errorf("could not parse HANDLER_TIMEOUT as int: %w", err)
	}

	maxBodyBytes := os.Getenv("MAX_BODY_BYTES")
	mbb, err := strconv.ParseInt(maxBodyBytes, 0, 64)
	if err != nil {
		return nil, fmt.Errorf("could not parse MAX_BODY_BYTES as int: %w", err)
	}

	handler.NewHandler(&handler.Config{
		R:               router,
		UserService:     userService,
		PostService:     postService,
		TimeoutDuration: time.Duration(ht) * time.Second,
		MaxBodyBytes:    mbb,
	})

	return router, nil
}
