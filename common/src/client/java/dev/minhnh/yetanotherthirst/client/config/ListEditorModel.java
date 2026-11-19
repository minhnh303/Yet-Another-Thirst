package dev.minhnh.yetanotherthirst.client.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Editable string rows backing {@link ListEditorScreen}. Every edit is stored as typed so validation
 * always reflects what the user sees; rows whose id column is blank are ignored on save.
 */
final class ListEditorModel<T> {

    private final ListSchema<T> schema;
    private final List<String[]> rows = new ArrayList<>();
    /** Rows (by identity) that are non-blank and fail validation; updated per edited row. */
    private final Set<String[]> invalidRows = Collections.newSetFromMap(new IdentityHashMap<>());

    ListEditorModel(ListSchema<T> schema, List<T> values) {
        this.schema = schema;
        load(values);
    }

    void load(List<T> values) {
        rows.clear();
        for (T entry : schema.canonical(values)) {
            rows.add(schema.toRowStrings(entry));
        }
        invalidRows.clear();
        rows.forEach(this::revalidate);
    }

    List<String[]> rows() {
        return Collections.unmodifiableList(rows);
    }

    String[] addBlank() {
        String[] row = new String[schema.columns().size()];
        List<Object> blank = schema.blankRow();
        for (int i = 0; i < row.length; i++) {
            row[i] = ListSchema.text(schema.columns().get(i), blank.get(i));
        }
        rows.add(0, row);
        revalidate(row);
        return row;
    }

    void remove(String[] row) {
        rows.remove(row);
        invalidRows.remove(row);
    }

    /** Stores the cell text regardless of validity and returns whether it is valid for its column. */
    boolean set(String[] row, int column, String text) {
        row[column] = text;
        revalidate(row);
        return ListSchema.valid(schema.columns().get(column), text);
    }

    int invalidCount() {
        return invalidRows.size();
    }

    boolean blank(String[] row) {
        return !schema.columns().get(0).numeric() && row[0].trim().isEmpty();
    }

    List<T> result() {
        List<T> result = new ArrayList<>(rows.size());
        for (String[] row : rows) {
            if (blank(row)) continue;
            result.add(schema.fromRowStrings(row));
        }
        return List.copyOf(result);
    }

    private void revalidate(String[] row) {
        if (rowValid(row)) {
            invalidRows.remove(row);
        } else {
            invalidRows.add(row);
        }
    }

    private boolean rowValid(String[] row) {
        if (blank(row)) return true;
        for (int i = 0; i < row.length; i++) {
            if (!ListSchema.valid(schema.columns().get(i), row[i])) {
                return false;
            }
        }
        return true;
    }
}
