package uk.ac.ebi.pride.toolsuite.chart.dataset;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.ebi.pride.toolsuite.chart.PrideChartType;
import uk.ac.ebi.pride.toolsuite.chart.io.PrideSpectrumHistogramDataSource;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.SortedMap;

/**
 * @author qingwei
 * Date: 13/06/13
 */
public class PrideDatasetFactory {

    private PrideDatasetFactory() {}

    private static XYSeries getSeries(PrideXYDataSource dataSource) {
        XYSeries series = new XYSeries(dataSource.getDataType().toString());
        Double[] domainValues = dataSource.getDomainData();
        PrideData[] rangeValues = dataSource.getRangeData();

        for (int i = 0; i < domainValues.length; i++) {
            series.add(domainValues[i], rangeValues[i].getData());
        }

        return series;
    }

    private static XYSeries getQuantSeries(PrideXYDataSource dataSource) {
        String key = dataSource.getCategoryDataType().iterator().next();
        XYSeries series = new XYSeries(key);
        Double[] domainValues = dataSource.getDomainData();
        PrideData[] rangeValues = dataSource.getRangeData();
        for (int i = 0; i < domainValues.length; i++) {
            series.add(domainValues[i], rangeValues[i].getData());
        }

        return series;
    }


    public static XYSeriesCollection getXYDataset(PrideXYDataSource dataSource) {

        XYSeries series = getSeries(dataSource.filter(dataSource.getDataType()));

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        Collection<PrideDataType> subTypes = dataSource.getDataType().getChildren();
        for (PrideDataType subType : subTypes) {
            PrideXYDataSource filterDataSource = dataSource.filter(subType);
            if (filterDataSource != null) {
                series = getSeries(filterDataSource);
                dataset.addSeries(series);
            }
        }

        return dataset;
    }

    public static XYSeriesCollection getQuantXYDataset(PrideXYDataSource dataSource) {

        XYSeriesCollection dataset = new XYSeriesCollection();

        Collection<String> subTypes = dataSource.getCategoryDataType();
        for (String category : subTypes) {
            PrideXYDataSource filterDataSource = dataSource.filterCategory(category);
            if (filterDataSource != null) {
                XYSeries series = getQuantSeries(filterDataSource);
                dataset.addSeries(series);
            }
        }

        return dataset;
    }

    public static XYSeriesCollection getXYDataset(PrideSpectrumHistogramDataSource dataSource) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Double>> histMap = dataSource.getIntensityMap();
        XYSeries series;
        SortedMap<PrideHistogramBin, Double> histogram;
        for (PrideDataType dataType : histMap.keySet()) {
            series = new XYSeries(dataType.getTitle());
            histogram = histMap.get(dataType);
            for (PrideHistogramBin bin : histogram.keySet()) {
                series.add(bin.getStartBoundary(), histogram.get(bin));
            }
            dataset.addSeries(series);
        }

        return dataset;
    }

    public static CategoryDataset getHistogramDataset(PrideHistogramDataSource dataSource, PrideChartType prideChartType) {
        SortedMap<PrideDataType, SortedMap<PrideHistogramBin, Integer>> histogramMap = dataSource.getHistogramMap();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String category;
        String seriesKey;
        SortedMap<PrideHistogramBin, Integer> histogram;
        for (PrideDataType dataType : histogramMap.keySet()) {
            histogram = histogramMap.get(dataType);
            seriesKey = dataType.getTitle();
            for (PrideHistogramBin bin : histogram.keySet()) {
                if(prideChartType == PrideChartType.MISSED_CLEAVAGES)
                    category = bin.toString(true);
                 else
                    category = bin.getEndBoundary() == Integer.MAX_VALUE ? ">" + bin.getStartBoundary() : bin.toString(new DecimalFormat("#"));
                dataset.addValue(histogram.get(bin), seriesKey, category);
            }
        }

        return dataset;
    }

    public static CategoryDataset getCategorryHistogramDataset(CategorySetHistogramDataSource dataSource){

        SortedMap<String, SortedMap<PrideHistogramBin, Integer>> histogramMap = dataSource.getHistogramMap();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String category;
        String seriesKey;
        SortedMap<PrideHistogramBin, Integer> histogram;
        for (String dataType : histogramMap.keySet()) {
            histogram = histogramMap.get(dataType);
            seriesKey = dataType;
            for (PrideHistogramBin bin : histogram.keySet()) {
                category = bin.getEndBoundary() == Integer.MAX_VALUE ? ">" + bin.getStartBoundary() : bin.toString(new DecimalFormat("#"));
                dataset.addValue(histogram.get(bin), seriesKey, category);
            }
        }

        return dataset;
    }
}
