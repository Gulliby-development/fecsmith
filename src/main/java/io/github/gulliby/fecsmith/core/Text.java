package io.github.gulliby.fecsmith.core;

import java.util.ArrayList;
import java.util.List;

/** Deterministic formatting helpers for user-facing text. */
public final class Text {
    private Text() {
    }

    public static String show(String text) {
        StringBuilder shown = new StringBuilder(text.length() + 2);
        shown.append('"');
        text.codePoints().forEach(codePoint ->
                shown.appendCodePoint(codePoint >= '!' && codePoint <= '~' ? codePoint : '?'));
        shown.append('"');
        return shown.toString();
    }

    public static String listOr(List<String> items) {
        if (items.isEmpty()) {
            return "";
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                result.append(i == items.size() - 1 ? " or " : ", ");
            }
            result.append(items.get(i));
        }
        return result.toString();
    }

    public static String plural(int n, String one, String many) {
        return n + " " + (n == 1 ? one : many);
    }

    public static List<String> renderTable(List<String> header, List<List<String>> rows) {
        int columns = header.size();
        int[] widths = new int[columns];
        for (int column = 0; column < columns; column++) {
            widths[column] = header.get(column).length();
        }
        for (List<String> row : rows) {
            for (int column = 0; column < columns; column++) {
                widths[column] = Math.max(widths[column], row.get(column).length());
            }
        }

        List<String> rendered = new ArrayList<>(rows.size() + 1);
        rendered.add(renderRow(header, widths));
        for (List<String> row : rows) {
            rendered.add(renderRow(row, widths));
        }
        return List.copyOf(rendered);
    }

    private static String renderRow(List<String> row, int[] widths) {
        StringBuilder rendered = new StringBuilder();
        for (int column = 0; column < widths.length; column++) {
            if (column > 0) {
                rendered.append("  ");
            }
            String cell = row.get(column);
            rendered.append(cell);
            if (column < widths.length - 1) {
                rendered.append(" ".repeat(widths[column] - cell.length()));
            }
        }
        while (!rendered.isEmpty() && rendered.charAt(rendered.length() - 1) == ' ') {
            rendered.setLength(rendered.length() - 1);
        }
        return rendered.toString();
    }

    public static String line(String key, String value) {
        return key + " ".repeat(Math.max(0, 8 - key.length())) + "  " + value;
    }
}
