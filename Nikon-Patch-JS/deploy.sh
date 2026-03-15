#!/bin/sh

echo "Uploading files to simeonpilgrim-static-site"
aws s3 sync dist s3://simeonpilgrim-static-site/nikon-patch/ --exclude nikon-patch.html --exclude nikon_patch.wasm --exclude nikon_patch.wasm.gz --cache-control max-age=31536000

echo "Uploading nikon-patch.html"
aws s3 cp dist/nikon-patch.html s3://simeonpilgrim-static-site/nikon-patch/nikon-patch.html --cache-control max-age=864000 --content-type text/html

rm --interactive=never dist/nikon_patch.wasm.gz
7z a -aoa -tgzip dist/nikon_patch.wasm.gz dist/nikon_patch.wasm
aws s3 cp dist/nikon_patch.wasm.gz s3://simeonpilgrim-static-site/nikon-patch/nikon_patch.wasm --cache-control max-age=31536000 --content-type application/wasm --content-encoding gzip

# Purge the cloudfront cache
echo "Purging the cache for CloudFront"
aws cloudfront create-invalidation --distribution-id E11YP6U64XYI10 --paths "/nikon-patch/*"