package com.luastar.swift.http.route.condition;

import com.luastar.swift.http.server.HttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.*;

public final class PatternsRequestCondition extends AbstractRequestCondition<PatternsRequestCondition> {

    private final Set<String> patterns;

    private final PathMatcher pathMatcher;

    private final boolean useSuffixPatternMatch;

    private final boolean useTrailingSlashMatch;

    private final List<String> fileExtensions = new ArrayList<String>();

    public PatternsRequestCondition(String... patterns) {
        this(asList(patterns), null, true, true, null);
    }

    public PatternsRequestCondition(String[] patterns,
                                    PathMatcher pathMatcher,
                                    boolean useSuffixPatternMatch,
                                    boolean useTrailingSlashMatch) {

        this(asList(patterns), pathMatcher, useSuffixPatternMatch, useTrailingSlashMatch, null);
    }

    /**
     * Creates a new instance with the given URL patterns.
     * Each pattern that is not empty and does not start with "/" is pre-pended with "/".
     *
     * @param patterns              the URL patterns to use; if 0, the condition will match to every request.
     * @param pathMatcher           a {@link PathMatcher} for pattern path matching
     * @param useSuffixPatternMatch whether to enable matching by suffix (".*")
     * @param useTrailingSlashMatch whether to match irrespective of a trailing slash
     * @param fileExtensions        a list of file extensions to consider for path matching
     */
    public PatternsRequestCondition(String[] patterns,
                                    PathMatcher pathMatcher,
                                    boolean useSuffixPatternMatch,
                                    boolean useTrailingSlashMatch,
                                    List<String> fileExtensions) {

        this(asList(patterns), pathMatcher, useSuffixPatternMatch, useTrailingSlashMatch, fileExtensions);
    }

    /**
     * Private constructor accepting a collection of patterns.
     */
    private PatternsRequestCondition(Collection<String> patterns,
                                     PathMatcher pathMatcher,
                                     boolean useSuffixPatternMatch,
                                     boolean useTrailingSlashMatch,
                                     List<String> fileExtensions) {

        this.patterns = Collections.unmodifiableSet(prependLeadingSlash(patterns));
        this.pathMatcher = pathMatcher != null ? pathMatcher : new AntPathMatcher();
        this.useSuffixPatternMatch = useSuffixPatternMatch;
        this.useTrailingSlashMatch = useTrailingSlashMatch;
        if (fileExtensions != null) {
            for (String fileExtension : fileExtensions) {
                if (fileExtension.charAt(0) != '.') {
                    fileExtension = "." + fileExtension;
                }
                this.fileExtensions.add(fileExtension);
            }
        }
    }

    private static List<String> asList(String... patterns) {
        return (patterns != null ? Arrays.asList(patterns) : Collections.<String>emptyList());
    }

    private static Set<String> prependLeadingSlash(Collection<String> patterns) {
        if (patterns == null) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<String>(patterns.size());
        for (String pattern : patterns) {
            if (StringUtils.hasLength(pattern) && !pattern.startsWith("/")) {
                pattern = "/" + pattern;
            }
            result.add(pattern);
        }
        return result;
    }

    public Set<String> getPatterns() {
        return this.patterns;
    }

    @Override
    protected Collection<String> getContent() {
        return this.patterns;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    public PatternsRequestCondition combine(PatternsRequestCondition other) {
        Set<String> result = new LinkedHashSet<String>();
        if (!this.patterns.isEmpty() && !other.patterns.isEmpty()) {
            for (String pattern1 : this.patterns) {
                for (String pattern2 : other.patterns) {
                    result.add(this.pathMatcher.combine(pattern1, pattern2));
                }
            }
        } else if (!this.patterns.isEmpty()) {
            result.addAll(this.patterns);
        } else if (!other.patterns.isEmpty()) {
            result.addAll(other.patterns);
        } else {
            result.add("");
        }
        return new PatternsRequestCondition(result, this.pathMatcher, this.useSuffixPatternMatch, this.useTrailingSlashMatch, this.fileExtensions);
    }

    public PatternsRequestCondition getMatchingCondition(HttpRequest request) {
        if (this.patterns.isEmpty()) {
            return this;
        }
        String lookupPath = request.getLookupPath();
        List<String> matches = new ArrayList<String>();
        for (String pattern : this.patterns) {
            String match = getMatchingPattern(pattern, lookupPath);
            if (match != null) {
                matches.add(match);
            }
        }
        Collections.sort(matches, this.pathMatcher.getPatternComparator(lookupPath));
        return matches.isEmpty() ? null : new PatternsRequestCondition(matches, this.pathMatcher, this.useSuffixPatternMatch, this.useTrailingSlashMatch, this.fileExtensions);
    }

    private String getMatchingPattern(String pattern, String lookupPath) {
        if (pattern.equals(lookupPath)) {
            return pattern;
        }
        if (this.useSuffixPatternMatch) {
            if (!this.fileExtensions.isEmpty() && lookupPath.indexOf('.') != -1) {
                for (String extension : this.fileExtensions) {
                    if (this.pathMatcher.match(pattern + extension, lookupPath)) {
                        return pattern + extension;
                    }
                }
            } else {
                boolean hasSuffix = pattern.indexOf('.') != -1;
                if (!hasSuffix && this.pathMatcher.match(pattern + ".*", lookupPath)) {
                    return pattern + ".*";
                }
            }
        }
        if (this.pathMatcher.match(pattern, lookupPath)) {
            return pattern;
        }
        if (this.useTrailingSlashMatch) {
            if (!pattern.endsWith("/") && this.pathMatcher.match(pattern + "/", lookupPath)) {
                return pattern + "/";
            }
        }
        return null;
    }

    public int compareTo(PatternsRequestCondition other, HttpRequest request) {
        String lookupPath = request.getLookupPath();
        Comparator<String> patternComparator = this.pathMatcher.getPatternComparator(lookupPath);
        Iterator<String> iterator = this.patterns.iterator();
        Iterator<String> iteratorOther = other.patterns.iterator();
        while (iterator.hasNext() && iteratorOther.hasNext()) {
            int result = patternComparator.compare(iterator.next(), iteratorOther.next());
            if (result != 0) {
                return result;
            }
        }
        if (iterator.hasNext()) {
            return -1;
        } else if (iteratorOther.hasNext()) {
            return 1;
        } else {
            return 0;
        }
    }

}
