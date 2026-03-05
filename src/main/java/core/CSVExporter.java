package core;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVExporter {

    public static boolean exportTableToCSV(TableModel model, File file) {
        try (FileWriter excel = new FileWriter(file)) {

            for (int i = 0; i < model.getColumnCount(); i++) {
                excel.write(model.getColumnName(i) + ",");
            }
            excel.write("\n");

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {

                    String data = model.getValueAt(i, j) != null ? model.getValueAt(i, j).toString() : "";
                    excel.write("\"" + data + "\",");
                }
                excel.write("\n");
            }
            return true;

        } catch (IOException e) {
            System.err.println("Failed to write CSV file.");
            e.printStackTrace();
            return false;
        }
    }
}