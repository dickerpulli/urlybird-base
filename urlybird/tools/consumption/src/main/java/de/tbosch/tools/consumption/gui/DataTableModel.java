package de.tbosch.tools.consumption.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.joda.time.format.DateTimeFormat;

import de.tbosch.tools.consumption.model.Entry;

public class DataTableModel extends AbstractTableModel {

	private final List<Entry> entries;

	public DataTableModel(List<Entry> entries) {
		this.entries = entries;
	}

	@Override
	public int getRowCount() {
		return entries.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Entry entry = entries.get(rowIndex);
		if (columnIndex == 0) {
			return DateTimeFormat.forPattern("dd.MM.yyyy").print(entry.getDate().getTime());
		} else {
			return Integer.toString(entry.getValue());
		}
	}

}
