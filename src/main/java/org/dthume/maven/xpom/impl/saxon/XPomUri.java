package org.dthume.maven.xpom.impl.saxon;

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

public class XPomUri {
    private final static Pattern ARTIFACT_PATTERN = Pattern.compile(
            "^/([^/]+)/([^/]+)/([^/]+)/([^/]+)(?:/(.*))?$");

    private final String coords;
    private final String resource;
    private final Map<String, String> params;

    private XPomUri(final String coords, final String resource,
            final Map<String, String> params) {
        this.coords = coords;
        this.resource = resource;
        this.params =
                unmodifiableMap(new LinkedHashMap<String, String>(params));
    }

    public String getCoords() { return coords; }

    public String getResource() { return resource; }

    public boolean isResourceURI() { return !StringUtils.isBlank(resource); }

    public Map<String, String> getParams() { return params; }

    public static XPomUri parseURI(final String uri) throws URISyntaxException {
        if (null == uri)
            throw new URISyntaxException(uri, "Cannot parse null URI");

        final URI jUri = new URI(uri);

        if (!"xpom".equals(jUri.getScheme())) {
            throw new URISyntaxException(uri,
                    "Not in xpom://<coords>[/<path>] format");
        }

        final String groupId = jUri.getAuthority();

        final Matcher matcher = ARTIFACT_PATTERN.matcher(jUri.getPath());

        if (!matcher.matches()) {
            throw new URISyntaxException(uri,
                    "XPOM scheme but not in xpom://<coords>[/<path>] format");
        }

        final String coords = toCoords(matcher, groupId);
        final String resource = toResource(matcher);
        final Map<String, String> params = toParams(jUri.getQuery());
        
        return new XPomUri(coords, resource, params);
    }

    public static XPomUri parseURIOrNull(final String uri) {
        try {
            return XPomUri.parseURI(uri);
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
