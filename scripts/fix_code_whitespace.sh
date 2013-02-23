#!/bin/sh
# Copyright 2010 Google Inc.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#!/bin/sh
find -E . -iregex ".*\.(java|xml|java\.example)" -exec sh -c '
  if [[ "$0" =~ src/org/json ]]; then
    exit
  fi
  echo $0
  sed -E "s@[[:space:]]@ @g" $0 > $0.j2
  mv $0.j2 $0
  sed -E "s@ +\$@@g" $0 > $0.j2
  mv $0.j2 $0
' {} \;
