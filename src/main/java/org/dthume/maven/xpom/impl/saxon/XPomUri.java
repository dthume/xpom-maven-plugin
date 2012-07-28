package org.dthume.maven.xpom.impl.saxon;/*
 * #%L
 * XPOM Maven Plugin
 * %%
 * Copyright (C) 2012 David Thomas Hume
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public final class XPomUri {
    private final static Pattern ARTIFACT_PATTERN = Pattern.compile(
            "^/([^/]+)/([^/]+)/([^/]+)/([^/]+)(?:/(.*))?$");

    private final String coords;
    private final String resource;
    private final Map<String, String> params;

    public XPomUri(final String uriString) throws URISyntaxException {
        if (null == uriString)
            throw new URISyntaxException(uriString, "Cannot parse null URI");

        final URI jUri = new URI(uriString);

        if (!"xpom".equals(jUri.getScheme())) {
            throw new URISyntaxException(uriString,
                    "Not in xpom://<coords>[/<path>] format");
        }

        final Matcher matcher = ARTIFACT_PATTERN.matcher(jUri.getPath());
        if (!matcher.matches()) {
            throw new URISyntaxException(uriString,
                    "XPOM scheme but not in xpom://<coords>[/<path>] format");
        }

        final String groupId = jUri.getAuthority();

        coords = toCoords(matcher, groupId);
        resource = toResource(matcher);
        params = unmodifiableMap(toParams(jUri.getQuery()));
    }
    
    public String getCoords() { return coords; }

    public String getResource() { return resource; }

    public boolean isResourceURI() { return !StringUtils.isBlank(resource); }

    public Map<String, String> getParams() { return params; }

    public static XPomUri parseURIOrNull(final String uri) {
        try {
            return new XPomUri(uri);
        } catch (final URISyntaxException e) {
            return null;
        }
    }

    private static String toCoords(final Matcher matcher,
            final String groupId) {
        final List<String> coordList = new LinkedList<String>(asList(
                groupId,
                matcher.group(1),
                matcher.group(2)));
        if (!"no;classifier".equals(matcher.group(3)))
            coordList.add(matcher.group(3));
        coordList.add(matcher.group(4));
        
        return StringUtils.join(coordList, ":");
    }

    private static String toResource(final Matcher matcher) {
        return matcher.group(5);
    }

    private static Map<String, String> toParams(final String query) {
        final Map<String, String> params = new LinkedHashMap<String, String>();

        if (!StringUtils.isBlank(query)) {
            for (final String param : query.split("&")) {
                final Map.Entry<String, String> kvp = toParam(param);
                params.put(kvp.getKey(), kvp.getValue());
            }
        }

        return params;
    }

    private static Map.Entry<String, String> toParam(final String param) {
        final String[] kvp = param.split("=", 2);
        final String key = kvp[0];
        final String value = 1 == kvp.length ? null : kvp[1];
        return new Map.Entry<String, String>() {
            public String getKey() { return key; }
            public String getValue() { return value; }
            public String setValue(final String value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
