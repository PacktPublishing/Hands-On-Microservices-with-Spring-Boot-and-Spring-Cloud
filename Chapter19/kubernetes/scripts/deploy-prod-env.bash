#!/usr/bin/env bash -ex

function waitForPods() {

    set +x
    local expectedPodCount=$1
    local labelSelection=$2
    local sleepSec=10

    n=0
    echo "Do we have $expectedPodCount pods with the label '$labelSelection' yet?"
    actualPodCount=$(kubectl get pod -l $labelSelection -o json | jq ".items | length")
    until [[ $actualPodCount == $expectedPodCount ]]
    do
        n=$((n + 1))
        if [[ $n == 40 ]]
        then
            echo " Give up"
            exit 1
        else
            echo -n "${actualPodCount}!=${expectedPodCount}, sleep $sleepSec..."
            sleep $sleepSec
            echo -n ", retry #$n, "
            actualPodCount=$(kubectl get pod -l $labelSelection -o json | jq ".items | length")
        fi
    done
    echo "OK! ($actualPodCount=$expectedPodCount)"

    set -x
}

if kubectl -n istio-system get secret istio-ingressgateway-certs > /dev/null ; then
    echo "Secret istio-ingressgateway-certs found, skip creating it..."
else
    echo "Secret istio-ingressgateway-certs not found, creating it..."
    kubectl create -n istio-system secret tls istio-ingressgateway-certs \
        --key kubernetes/cert/tls.key \
        --cert kubernetes/cert/tls.crt
fi

kubectl create configmap config-repo-auth-server       --from-file=config-repo/application.yml --from-file=config-repo/auth-server.yml --save-config
kubectl create configmap config-repo-gateway           --from-file=config-repo/application.yml --from-file=config-repo/gateway.yml --save-config
kubectl create configmap config-repo-product-composite --from-file=config-repo/application.yml --from-file=config-repo/product-composite.yml --save-config
kubectl create configmap config-repo-product           --from-file=config-repo/application.yml --from-file=config-repo/product.yml --save-config
kubectl create configmap config-repo-recommendation    --from-file=config-repo/application.yml --from-file=config-repo/recommendation.yml --save-config
kubectl create configmap config-repo-review            --from-file=config-repo/application.yml --from-file=config-repo/review.yml --save-config

kubectl create secret generic rabbitmq-credentials \
    --from-literal=SPRING_RABBITMQ_USERNAME=rabbit-user-prod \
    --from-literal=SPRING_RABBITMQ_PASSWORD=rabbit-pwd-prod \
    --save-config

kubectl create secret generic mongodb-credentials \
    --from-literal=SPRING_DATA_MONGODB_AUTHENTICATION_DATABASE=admin \
    --from-literal=SPRING_DATA_MONGODB_USERNAME=mongodb-user-prod \
    --from-literal=SPRING_DATA_MONGODB_PASSWORD=mongodb-pwd-prod \
    --save-config

kubectl create secret generic mysql-credentials \
    --from-literal=SPRING_DATASOURCE_USERNAME=mysql-user-prod \
    --from-literal=SPRING_DATASOURCE_PASSWORD=mysql-pwd-prod \
    --save-config

kubectl create secret tls tls-certificate --key kubernetes/cert/tls.key --cert kubernetes/cert/tls.crt

eval $(minikube docker-env)
docker-compose up -d mongodb mysql rabbitmq


# Deploy v1 services
docker tag hands-on/auth-server               hands-on/auth-server:v1
docker tag hands-on/product-composite-service hands-on/product-composite-service:v1 
docker tag hands-on/product-service           hands-on/product-service:v1
docker tag hands-on/recommendation-service    hands-on/recommendation-service:v1
docker tag hands-on/review-service            hands-on/review-service:v1

kubectl apply -k kubernetes/services/base/services
kubectl apply -k kubernetes/services/overlays/prod/v1
kubectl apply -k kubernetes/services/overlays/prod/istio

kubectl wait --timeout=600s --for=condition=available deployment --all

kubectl get deployment auth-server-v1 product-v1 product-composite-v1 recommendation-v1 review-v1 -o yaml | istioctl kube-inject -f - | kubectl apply -f -

waitForPods 5 'version=v1'


# Deploy v2 services
docker tag hands-on/auth-server               hands-on/auth-server:v2
docker tag hands-on/product-composite-service hands-on/product-composite-service:v2 
docker tag hands-on/product-service           hands-on/product-service:v2
docker tag hands-on/recommendation-service    hands-on/recommendation-service:v2
docker tag hands-on/review-service            hands-on/review-service:v2

kubectl apply -k kubernetes/services/overlays/prod/v2

kubectl wait --timeout=600s --for=condition=available deployment --all

kubectl get deployment auth-server-v2 product-v2 product-composite-v2 recommendation-v2 review-v2 -o yaml | istioctl kube-inject -f - | kubectl apply -f -

waitForPods 5 'version=v2'


# Ensure that alls pos are ready
kubectl wait --timeout=120s --for=condition=Ready pod --all