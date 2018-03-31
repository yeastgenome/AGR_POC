build: pull
	docker build -t agrdocker/agr_loader_run:latest .

buildenv: build

startdb:
	docker-compose up -d neo4j.nqc

stopdb:
	docker-compose stop neo4j.nqc

pull:
	docker pull agrdocker/agr_neo4j_env:latest

removedb:
	docker-compose down -v

run: build
	docker-compose up agr_loader
	docker-compose run agr_loader_test pytest --verbose

run_test: build
	docker-compose up agr_loader_test
	docker-compose run agr_loader_test pytest --verbose

unit_tests:
	docker-compose run agr_loader_test pytest --verbose

bash:
	docker-compose up agr_loader bash

reload: 
	docker-compose up -d neo4j.nqc
	docker-compose down -v
	docker-compose up -d neo4j.nqc
	sleep 10
	docker build -t agrdocker/agr_loader_run:latest .
	docker-compose up agr_loader

reload_test: 
	docker-compose up -d neo4j.nqc
	docker-compose down -v
	docker-compose up -d neo4j.nqc
	sleep 10
	docker build -t agrdocker/agr_loader_run:latest .
	docker-compose up agr_loader_test

start_agr_box:
				docker-compose up -d loader_app
reload_agr_box: rm_agr_box
				docker-compose up loader_app
rm_agr_box:
				docker-compose kill loader_app && docker-compose rm --force all loader_app
stop_agr_box:
				docker-compose stop --timeout 3
run_agr_box_fetch:
				python src/fetch_index.py


