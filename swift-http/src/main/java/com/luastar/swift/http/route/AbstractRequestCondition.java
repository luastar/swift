package com.luastar.swift.http.route;

import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractRequestCondition<T extends AbstractRequestCondition<T>> implements RequestCondition<T> {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass().equals(obj.getClass())) {
            AbstractRequestCondition<?> other = (AbstractRequestCondition<?>) obj;
            return getContent().equals(other.getContent());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getContent().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (Iterator<?> iterator = getContent().iterator(); iterator.hasNext(); ) {
            Object expression = iterator.next();
            builder.append(expression.toString());
            if (iterator.hasNext()) {
                builder.append(getToStringInfix());
            }
        }
        builder.append("]");
        return builder.toString();
    }


    /**
     * Return the discrete items a request condition is composed of.
     * For example URL patterns, HTTP request methods, param expressions, etc.
     *
     * @return a collection of objects, never {@code null}
     */
    protected abstract Collection<?> getContent();

    /**
     * The notation to use when printing discrete items of content.
     * For example " || " for URL patterns or " && " for param expressions.
     */
    protected abstract String getToStringInfix();

}
