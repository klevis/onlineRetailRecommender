package ramo.klevis.ml;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import ramo.klevis.data.PrepareData;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static ramo.klevis.ml.IFCollaborativeFiltering.Rating.EMPTY;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class IFCollaborativeFiltering {

    private transient SparkSession sparkSession;
    private Double rmse;


    public List<Row> train(Integer trainSize, Integer testSize, Integer feature, Double reg) throws IOException {
        if (sparkSession == null) {
            sparkSession = SparkSession.builder()
                    .master("local[*]")
                    .appName("Online Retailer")
                    .getOrCreate();
        }
        JavaRDD<Rating> ratingsRDD = sparkSession
                .read().csv("src/main/resources/data/Online Retail.csv").javaRDD()
                .map(Rating::parseRating).filter(e -> e != EMPTY);

        Dataset<Row> ratings = sparkSession.createDataFrame(ratingsRDD, Rating.class);

        double v = trainSize / 100d;
        double v1 = testSize / 100d;
        Dataset<Row>[] splits = ratings.randomSplit(new double[]{v, v1});
        Dataset<Row> training = splits[0];
        Dataset<Row> test = splits[1];

        ALS als = new ALS()
                .setMaxIter(1)
                .setRegParam(reg)
                .setUserCol("userId")
                .setItemCol("movieId")
                .setRatingCol("rating")
                .setImplicitPrefs(true).
                        setRank(feature);
        ALSModel model = als.fit(training);
        model.setColdStartStrategy("drop");

        Dataset<Row> predictions = model.transform(test);

        RegressionEvaluator evaluator = new RegressionEvaluator()
                .setMetricName("rmse")
                .setLabelCol("rating")
                .setPredictionCol("prediction");
        rmse = evaluator.evaluate(predictions);

        return model.recommendForAllUsers(10).collectAsList();


    }

    public Double getRmse() {
        return rmse;
    }

    public static class Rating implements Serializable {
        private int userId;
        private int movieId;
        private float rating;

        public static final Rating EMPTY = new Rating();

        public Rating(int userId, int movieId, float rating) {
            this.userId = userId;
            this.movieId = movieId;
            this.rating = rating;
        }

        public Rating() {
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getMovieId() {
            return movieId;
        }

        public void setMovieId(int movieId) {
            this.movieId = movieId;
        }

        public float getRating() {
            return rating;
        }

        public void setRating(float rating) {
            this.rating = rating;
        }

        public static Rating parseRating(Row row) {
            int movieId;
            if (row.apply(1) == null) {
                return EMPTY;
            }
            try {
                movieId = PrepareData.prepareItemId(row.apply(1).toString());
            } catch (NumberFormatException e) {
                System.err.println("item " + row.apply(1));
                return EMPTY;
            }
            if (row.apply(6) == null) {
                return EMPTY;
            }
            int id;
            try {
                id = Integer.parseInt(row.apply(6).toString());
            } catch (NumberFormatException e) {
                System.err.println("User " + row.apply(6));
                return EMPTY;
            }
            return new Rating(id,
                    movieId,
                    Integer.parseInt(String.valueOf(row.apply(3))));
        }
    }


}
