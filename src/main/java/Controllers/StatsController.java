package Controllers;

import Services.ContenuMultiMediaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.util.StringConverter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
public class StatsController {
    @FXML private PieChart pieChart;
    @FXML private LineChart<Number, Number> lineChart;
    @FXML private NumberAxis xAxis;

    private final ContenuMultiMediaService service = new ContenuMultiMediaService();

    @FXML
    public void initialize() {
        loadCategoryStats();
        loadCreationTrend();
    }

    private void loadCategoryStats() {
        try {
            Map<String, Integer> stats = service.getStatsByCategory();
            pieChart.setData(FXCollections.observableArrayList());
            stats.forEach((category, count) ->
                    pieChart.getData().add(new PieChart.Data(category + " (" + count + ")", count))
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCreationTrend() {
        try {
            Map<Date, Integer> trends = service.getCreationTrend();
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Évolution des créations");

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
            xAxis.setTickLabelFormatter(new StringConverter<Number>() {
                @Override
                public String toString(Number timestamp) {
                    return dateFormat.format(new Date(timestamp.longValue()));
                }

                @Override
                public Number fromString(String string) {
                    return null;
                }
            });

            trends.forEach((date, count) ->
                    series.getData().add(new XYChart.Data<>(date.getTime(), count))
            );

            lineChart.getData().clear();
            lineChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}