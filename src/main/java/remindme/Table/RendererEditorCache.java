package remindme.Table;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class RendererEditorCache {
    private static final Map<Integer, TableCellRenderer> rendererCache = new HashMap<>();
    private static final Map<Integer, TableCellEditor> editorCache = new HashMap<>();

    public static void store(int columnIndex, TableCellRenderer renderer, TableCellEditor editor) {
        rendererCache.put(columnIndex, renderer);
        editorCache.put(columnIndex, editor);
    }

    public static TableCellRenderer getOriginalRenderer(int columnIndex) {
        return rendererCache.get(columnIndex);
    }

    public static TableCellEditor getOriginalEditor(int columnIndex) {
        return editorCache.get(columnIndex);
    }

    public static void clear(int columnIndex) {
        rendererCache.remove(columnIndex);
        editorCache.remove(columnIndex);
    }
}
