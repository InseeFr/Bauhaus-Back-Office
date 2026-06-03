package fr.insee.rmes.colectica.client;

/**
 * Direction of a Colectica relationship query.
 *
 * <ul>
 *   <li>{@link #BY_SUBJECT} — items the target references (its direct children/descendants in the
 *       relationship graph).</li>
 *   <li>{@link #BY_OBJECT} — items that reference the target (its direct parents/ancestors).</li>
 * </ul>
 *
 * <p>The {@link #urlSegment()} maps to the corresponding path segment of
 * {@code _query/relationship/{segment}/descriptions}.
 */
public enum RelationshipDirection {
    BY_SUBJECT("bysubject"),
    BY_OBJECT("byobject");

    private final String urlSegment;

    RelationshipDirection(String urlSegment) {
        this.urlSegment = urlSegment;
    }

    public String urlSegment() {
        return urlSegment;
    }
}
