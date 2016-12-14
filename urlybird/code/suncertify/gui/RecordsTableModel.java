package suncertify.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import suncertify.db.Record;
import suncertify.db.Record.RecordField;

/**
 * The table model that includes the record entries.
 */
public class RecordsTableModel extends AbstractTableModel {

    /** serial id. */
    private static final long serialVersionUID = 1L;

    /** The list of entries in the table. */
    private List<Record> records;

    /**
     * Constructor.
     * 
     * @param records
     *            The inital list of records.
     */
    public RecordsTableModel(List<Record> records) {
	this.records = records;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int row, int column) {
	Record rowValues = records.get(row);
	return rowValues.getData()[column];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
	return records.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
	return RecordField.values().length;
    }

    /**
     * Returns the name of the column that is displayed in the header of the
     * table. The name displayed is the field desciptive name defined in the
     * {@link RecordField} enum.
     */
    @Override
    public String getColumnName(int column) {
	return RecordField.getByIndex(column).toString();
    }

    /**
     * Returns the record in the row.
     * 
     * @param row
     *            The row number
     */
    public Record getRecordAtRow(int row) {
	return records.get(row);
    }

}
