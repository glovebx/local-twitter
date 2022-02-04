postgres:
	docker run --name postgres12 -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD=secret -d postgres:12-alpine

redis:
	docker run -d --name redis -p 6379:6379  -v /path/to/redisconf/redis.conf:/redis.conf redis redis-server /redis.conf

createdb:
	docker exec -it postgres12 createdb --username=root --owner=root mirage

dropdb:
	docker exec -it postgres12 dropdb mirage

recreate:
	make dropdb && make createdb

start:
	docker start postgres12 && docker start redis

test:
	go test -v -cover ./service/... ./handler/...

e2e:
	go test -run '^\QTestMain_E2E\E$\'

lint:
	golangci-lint run

build:
	go build github.com/sentrionic/mirage

fmt:
	go fmt github.com/sentrionic/...