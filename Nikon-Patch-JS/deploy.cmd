set AWS_PROFILE=simeon-site

echo "Uploading files to simeonpilgrim-static-site"
aws s3 sync build s3://simeonpilgrim-static-site/nikon-patch/ --exclude index.html --cache-control max-age=31536000

rem Upload nikon-patch.html 
echo "Uploading nikon-patch.html"
aws s3 cp build/index.html s3://simeonpilgrim-static-site/nikon-patch/nikon-patch.html --cache-control max-age=3600 --content-type text/html

rem Purge the cloudfront cache
echo "Purging the cache for CloudFront"
aws cloudfront create-invalidation --distribution-id E11YP6U64XYI10 --paths "/nikon-patch/nikon-patch.html"