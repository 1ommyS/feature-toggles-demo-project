.PHONY: test build run infra up down demo-legacy demo-smart

test:
	./gradlew test

build:
	./gradlew clean check bootJar

run:
	./gradlew bootRun

infra:
	docker compose up -d postgres unleash-db unleash

up:
	docker compose up --build

down:
	docker compose down -v

demo-legacy:
	FEATURE_FLAGS_PROVIDER=local FEATURE_SMART_RANKING_V2=false ./gradlew bootRun

demo-smart:
	FEATURE_FLAGS_PROVIDER=local FEATURE_SMART_RANKING_V2=true ./gradlew bootRun
