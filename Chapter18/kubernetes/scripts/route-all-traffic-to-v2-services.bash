#!/usr/bin/env bash -e

kubectl patch virtualservice auth-server-vs --type=json -p='[
  {"op": "add", "path": "/spec/http/0/route/0/weight", "value": "0"},
  {"op": "add", "path": "/spec/http/0/route/1/weight", "value": "100"}
]'

kubectl patch virtualservice product-composite-vs --type=json -p='[
  {"op": "add", "path": "/spec/http/0/route/0/weight", "value": "0"},
  {"op": "add", "path": "/spec/http/0/route/1/weight", "value": "100"}
]'

kubectl patch virtualservice product-vs --type=json -p='[
  {"op": "add", "path": "/spec/http/1/route/0/weight", "value": "0"},
  {"op": "add", "path": "/spec/http/1/route/1/weight", "value": "100"}
]'

kubectl patch virtualservice recommendation-vs --type=json -p='[
  {"op": "add", "path": "/spec/http/1/route/0/weight", "value": "0"},
  {"op": "add", "path": "/spec/http/1/route/1/weight", "value": "100"}
]'

kubectl patch virtualservice review-vs --type=json -p='[
  {"op": "add", "path": "/spec/http/1/route/0/weight", "value": "0"},
  {"op": "add", "path": "/spec/http/1/route/1/weight", "value": "100"}
]'
