/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jumpnote.javashared;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class Util {
    private static SimpleDateFormat sDateFormatISO8601 = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssZ");

    public static final String formatDateISO8601(Date d) {
        return sDateFormatISO8601.format(d);
    }

    public static final Date parseDateISO8601(String s) throws ParseException {
        return sDateFormatISO8601.parse(s);
    }

    // http://snippets.dzone.com/posts/show/91
    public static <T> String join(Collection<T> s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<T> iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
}
