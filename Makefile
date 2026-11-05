IMAGE_TAG ?= 0.1.0
MODULES := order-service inventory-service payment-service api-gateway

.PHONY: test package up down images demo

test:
	mvn -q test

package:
	mvn -q -DskipTests package

up:
	docker compose up -d

down:
	docker compose down

images:
	@for module in $(MODULES); do \
		docker build -f deploy/Dockerfile --build-arg MODULE=$$module -t minicommerce/$$module:$(IMAGE_TAG) . ; \
	done

demo:
	./scripts/demo.sh
