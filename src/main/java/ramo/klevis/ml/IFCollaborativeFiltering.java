package ramo.klevis.ml;

import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.In;
import ramo.klevis.data.Item;
import ramo.klevis.data.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class IFCollaborativeFiltering {

    private SparkSession sparkSession;

    public List<Rating> convertToRating(List<User> userList) {
        return userList.stream().parallel().flatMap(user -> user.getItems().stream().map(item -> new Rating(Integer.parseInt(user.getId()),
                Integer.parseInt(item.getId().toLowerCase()
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
                        .replaceAll("n", "7")),
                user.getItems().size(),
                LocalDateTime.now().getSecond())))
                .collect(Collectors.toList());
    }

    public void train(List<User> userList) {
        if (sparkSession == null) {
            sparkSession = SparkSession.builder()
                    .master("local[*]")
                    .appName("Online Retailer")
                    .getOrCreate();
        }

        Dataset<Row> ratings = sparkSession.createDataFrame(convertToRating(userList), Rating.class);

        Dataset<Row>[] splits = ratings.randomSplit(new double[]{0.8, 0.2});
        Dataset<Row> training = splits[0];
        Dataset<Row> test = splits[1];

// Build the recommendation model using ALS on the training data
        ALS als = new ALS()
                .setMaxIter(5)
                .setRegParam(0.01)
                .setUserCol("userId")
                .setItemCol("movieId")
                .setRatingCol("rating");
        ALSModel model = als.fit(training);

// Evaluate the model by computing the RMSE on the test data
// Note we set cold start strategy to 'drop' to ensure we don't get NaN evaluation metrics
        model.setColdStartStrategy("drop");
        Dataset<Row> predictions = model.transform(test);

        RegressionEvaluator evaluator = new RegressionEvaluator()
                .setMetricName("rmse")
                .setLabelCol("rating")
                .setPredictionCol("prediction");
        Double rmse = evaluator.evaluate(predictions);
        System.out.println("Root-mean-square error = " + rmse);

// Generate top 10 movie recommendations for each user
        Dataset<Row> userRecs = model.recommendForAllUsers(10);

    }

    public static class Rating implements Serializable {
        private int userId;
        private int movieId;
        private float rating;
        private long timestamp;

        public Rating() {
        }

        public Rating(int userId, int movieId, float rating, long timestamp) {
            this.userId = userId;
            this.movieId = movieId;
            this.rating = rating;
            this.timestamp = timestamp;
        }

        public int getUserId() {
            return userId;
        }

        public int getMovieId() {
            return movieId;
        }

        public float getRating() {
            return rating;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
