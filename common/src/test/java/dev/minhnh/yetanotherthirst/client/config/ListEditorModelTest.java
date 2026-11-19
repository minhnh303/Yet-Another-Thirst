package dev.minhnh.yetanotherthirst.client.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListEditorModelTest {

    @Test
    void invalidIdEditIsStoredAndBlocksSave() {
        ListEditorModel<String> model = new ListEditorModel<>(ListSchema.items(), List.of("minecraft:apple"));
        String[] row = model.rows().get(0);

        assertFalse(model.set(row, 0, "not an id"));
        assertEquals("not an id", row[0]);
        assertEquals(1, model.invalidCount());

        assertTrue(model.set(row, 0, "minecraft:bread"));
        assertEquals(0, model.invalidCount());
        assertEquals(List.of("minecraft:bread"), model.result());
    }

    @Test
    void clearedIdIsStoredAndRowIsDroppedInsteadOfKeepingOldValue() {
        ListEditorModel<ItemValue> model = new ListEditorModel<>(
                ListSchema.itemValues(), List.of(new ItemValue("minecraft:apple", 3, 4)));
        String[] row = model.rows().get(0);

        model.set(row, 0, "");

        assertEquals("", row[0]);
        assertEquals(0, model.invalidCount());
        assertEquals(List.of(), model.result());
    }

    @Test
    void invalidNumericEditBlocksSaveUntilFixed() {
        ListEditorModel<ItemValue> model = new ListEditorModel<>(
                ListSchema.itemValues(), List.of(new ItemValue("minecraft:apple", 3, 4)));
        String[] row = model.rows().get(0);

        assertFalse(model.set(row, 1, "-"));
        assertEquals(1, model.invalidCount());

        assertTrue(model.set(row, 1, "5"));
        assertEquals(List.of(new ItemValue("minecraft:apple", 5, 4)), model.result());
    }

    @Test
    void blankAddedRowIsIgnoredUntilFilledAndRemovalRecountsInvalid() {
        ListEditorModel<String> model = new ListEditorModel<>(ListSchema.items(), List.of());
        String[] row = model.addBlank();

        assertEquals(0, model.invalidCount());
        assertEquals(List.of(), model.result());

        model.set(row, 0, "bad id");
        assertEquals(1, model.invalidCount());
        model.remove(row);
        assertEquals(0, model.invalidCount());
        assertTrue(model.rows().isEmpty());
    }
}
