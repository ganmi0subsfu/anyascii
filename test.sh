#!/bin/sh

set -eu

npm test

go test ./go

ruby ruby/lib/any_ascii_test.rb

cd rust
cargo test
cd ..

cd python
python -m pytest
cd ..

cd java
./mvnw test
cd ..