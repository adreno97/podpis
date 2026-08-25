#!/usr/bin/env bash
set -euo pipefail

SDK=/home/adreno/android-sdk
BT=$SDK/build-tools/34.0.0
PLAT=$SDK/platforms/android-34/android.jar
AAPT2=$BT/aapt2
PRJ=/home/adreno/podpis
BUNDLETOOL_VERSION=1.18.3
cd "$PRJ"

if [ ! -f build/tools/bundletool-all.jar ]; then
  echo "== скачиваю bundletool =="
  mkdir -p build/tools
  curl -s -L -o build/tools/bundletool-all.jar \
      "https://github.com/google/bundletool/releases/download/$BUNDLETOOL_VERSION/bundletool-all-$BUNDLETOOL_VERSION.jar"
fi

if [ ! -f build/dex/classes.dex ]; then
  echo "Нет build/dex/classes.dex — сначала запустите build.sh"
  exit 1
fi

rm -rf build/aab
mkdir -p build/aab

echo "== aapt2 compile =="
"$AAPT2" compile --dir res -o build/aab/compiled.zip

echo "== aapt2 link (proto) =="
"$AAPT2" link --proto-format -o build/aab/proto.apk -I "$PLAT" \
    --manifest AndroidManifest.xml -A assets \
    --min-sdk-version 21 --target-sdk-version 34 \
    build/aab/compiled.zip

echo "== module base =="
mkdir -p build/aab/proto build/aab/base/manifest build/aab/base/dex
unzip -o -q build/aab/proto.apk -d build/aab/proto
cp build/aab/proto/AndroidManifest.xml build/aab/base/manifest/
cp build/aab/proto/resources.pb build/aab/base/
cp -r build/aab/proto/res build/aab/base/
cp -r build/aab/proto/assets build/aab/base/
cp build/dex/classes.dex build/aab/base/dex/
( cd build/aab/base && zip -q -r ../base.zip . )

echo "== bundletool build-bundle =="
/usr/bin/java -jar build/tools/bundletool-all.jar build-bundle \
    --modules=build/aab/base.zip --output=build/aab/ПодписьPDF.aab

echo "== sign =="
"$BT/apksigner" sign --min-sdk-version 21 --ks key.jks --ks-key-alias podpis \
    --ks-pass pass:trucker123 --key-pass pass:trucker123 \
    --v1-signing-enabled true --v2-signing-enabled false --v3-signing-enabled false \
    --out build/aab/ПодписьPDF-signed.aab build/aab/ПодписьPDF.aab

/usr/bin/java -jar build/tools/bundletool-all.jar validate --bundle=build/aab/ПодписьPDF-signed.aab >/dev/null 2>&1 && echo "VALID"

ls -la build/aab/ПодписьPDF-signed.aab
echo "DONE"
