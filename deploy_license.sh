cd `dirname "$0"`

LICENSE_FILE=$1
if [ -z "$LICENSE_FILE" ]; then
    echo "License file is not set! Please set it as first argument!"
    exit 1
fi

if [ ! -f $LICENSE_FILE ]; then
    echo "License file $LICENSE_FILE cannot be found. Please set it as first argument!"
    exit 1
fi

cp "$LICENSE_FILE" sample-verify/src/main/res/raw/
cp "$LICENSE_FILE" sample-video/src/main/res/raw/