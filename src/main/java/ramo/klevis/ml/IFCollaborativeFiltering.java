package ramo.klevis.ml;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import ramo.klevis.data.User;

import java.io.Serializable;
import java.util.List;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class IFCollaborativeFiltering {

    private transient SparkSession sparkSession;


    public void train() {
        if (sparkSession == null) {
            sparkSession = SparkSession.builder()
                    .master("local[*]")
                    .appName("Online Retailer")
                    .getOrCreate();
        }
        JavaRDD<Rating> ratingsRDD = sparkSession
                .read().csv("src/main/resources/data/Online Retail.csv").javaRDD()
                .map(Rating::parseRating).filter(e -> e.getUserId() > 0);

        Dataset<Row> ratings = sparkSession.createDataFrame(ratingsRDD, Rating.class);

        Dataset<Row>[] splits = ratings.randomSplit(new double[]{0.8, 0.2});
        Dataset<Row> training = splits[0];
        Dataset<Row> test = splits[1];

// Build the recommendation model using ALS on the training data
        ALS als = new ALS()
                .setMaxIter(1)
                .setRegParam(0.01)
                .setUserCol("userId")
                .setItemCol("movieId")
                .setRatingCol("rating")
                .setImplicitPrefs(true);
        ALSModel model = als.fit(training);
        model.setColdStartStrategy("drop");
// Evaluate the model by computing the RMSE on the test data
// Note we set cold start strategy to 'drop' to ensure we don't get NaN evaluation metrics
        Dataset<Row> predictions = model.transform(test);

        RegressionEvaluator evaluator = new RegressionEvaluator()
                .setMetricName("rmse")
                .setLabelCol("rating")
                .setPredictionCol("prediction");
        Double rmse = evaluator.evaluate(predictions);
        System.out.println("Root-mean-square error = " + rmse);

// Generate top 10 movie recommendations for each user
        Dataset<Row> userRecs = model.recommendForAllUsers(10);

        userRecs.show();

    }

    public static class Rating implements Serializable {
        private int userId;
        private int movieId;
        private float rating;

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
                return new Rating();
            }
            try {
                movieId = Integer.parseInt(row.apply(1).toString().toLowerCase()
                        .replaceAll("a", "1")
                        .replaceAll("b", "2")
                        .replaceAll("c", "3")
                        .replaceAll("l", "4")
                        .replaceAll("j", "5")
                        .replaceAll("p", "6")
                        .replaceAll("f", "8")
                        .replaceAll("t", "9")
                        .replaceAll("e", "10")
                        .replaceAll("o", "11")
                        .replaceAll("s", "12")
                        .replaceAll("m", "13")
                        .replaceAll("g", "14")
                        .replaceAll("w", "15")
                        .replaceAll("k", "16")
                        .replaceAll("d", "17")
                        .replaceAll("h", "18")
                        .replaceAll("u", "19")
                        .replaceAll("r", "20")
                        .replaceAll("v", "21")
                        .replaceAll("i", "22")
                        .replaceAll("y", "23")
                        .replaceAll("z", "24")
                        .replaceAll("n", "7"));
            } catch (NumberFormatException e) {
                System.err.println("item " + row.apply(1));
                return new Rating();
            }
            if (row.apply(6) == null) {
                return new Rating();
            }
            int id;
            try {
                id = Integer.parseInt(row.apply(6).toString());
            } catch (NumberFormatException e) {
                System.err.println("User " + row.apply(6));
                return new Rating();
            }
            return new Rating(id,
                    movieId,
                    Integer.parseInt(String.valueOf(row.apply(3))));
        }
    }

}
