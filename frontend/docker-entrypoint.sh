#!/bin/sh

envsubst '$BACKEND_URL' \
< /etc/nginx/templates/default.conf.template \
> /etc/nginx/conf.d/default.conf


nginx -g "daemon off;"