set AWS_PROFILE=simeon-site

echo "Uploading files to simeonpilgrim-static-site"
aws s3 sync build s3://simeonpilgrim-static-site/nikon-patch/ --exclude index.html --exclude nikon_patch.wasm --exclude nikon_patch.wasm.gz --cache-control max-age=31536000

rem Upload nikon-patch.html 
echo "Uploading nikon-patch.html"
aws s3 cp build/index.html s3://simeonpilgrim-static-site/nikon-patch/nikon-patch.html --cache-control max-age=864000 --content-type text/html

del /Q build\nikon_patch.wasm.gz
"C:\Program Files\7-Zip\7z.exe" a -aoa -tgzip build\nikon_patch.wasm.gz build\nikon_patch.wasm
aws s3 cp build/nikon_patch.wasm.gz s3://simeonpilgrim-static-site/nikon-patch/nikon_patch.wasm --cache-control max-age=31536000 --content-type application/wasm --content-encoding gzip

rem Purge the cloudfront cache
echo "Purging the cache for CloudFront"
aws cloudfront create-invalidation --distribution-id E11YP6U64XYI10 --paths "/nikon-patch/*"