package edu.umkc.fv

import edu.umkc.fv.NLPUtils._
import edu.umkc.fv.Utils._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by Rakesh Reddy on 02-Mar-16.
  */
object FeatureVector1 {

   def main(args: Array[String]) {

     // Set the system properties so that Twitter4j library used by twitter stream
     // can use them to generate OAuth credentials

     System.setProperty("hadoop.home.dir","F:\\winutils")
     val filters = args

     // Set the system properties so that Twitter4j library used by twitter stream
     // can use them to generate OAuth credentials

     System.setProperty("twitter4j.oauth.consumerKey", "wnXT3XsVE9ly8AI242KLcP5q5")
     System.setProperty("twitter4j.oauth.consumerSecret", "tx5eccUG3IbfRUASQTexg9StF6Ksz0ztRyooudP3n0o2eQLHOD")
     System.setProperty("twitter4j.oauth.accessToken", "901480446-H76RYrhaqKgQwx3J9jESyHNYh7G9TgvIopeh3QAV")
     System.setProperty("twitter4j.oauth.accessTokenSecret", "wxXl2KCmy0wW0LvUp5ivSDt3tOsNuJ6OZ5onw9HPFQdwE")

     //Create a spark configuration with a custom name and master
     // For more master configuration see  https://spark.apache.org/docs/1.2.0/submitting-applications.html#master-urls
     val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Spark-Machine_Learning-Text-1").set("spark.driver.memory", "3g").set("spark.executor.memory", "3g")
     val ssc = new StreamingContext(sparkConf, Seconds(2))
     //Using the streaming context, open a twitter stream (By the way you can also use filters)
     //Stream generates a series of random tweets
     val stream = TwitterUtils.createStream(ssc, None, filters)

       val test = stream.flatMap(status => status.getText.split(" "))
     test.saveAsTextFiles("data/testing/test.txt")
     val Restaurants = stream.flatMap(status => status.getText.split(" ").filter(_.contains("Restaurants")))
     Restaurants.saveAsTextFiles("data/training/restaurants.txt")
     val Movies = stream.flatMap(status => status.getText.split(" ").filter(_.contains("Movies")))
     Movies.saveAsTextFiles("data/training/movies.txt")
     val Sports = stream.flatMap(status => status.getText.split(" ").filter(_.contains("Sports")))
     Sports.saveAsTextFiles("data/training/sports.txt")
     ssc.start()
     ssc.awaitTermination(10000)
     val sc = ssc.sparkContext
     val stopWords = sc.broadcast(loadStopWords("/stopwords.txt")).value
     val labelToNumeric = createLabelMap("data/training/")
     var model: NaiveBayesModel = null
     // Training the data
     val training = sc.wholeTextFiles("data/training/*")
       .map(rawText => createLabeledDocument(rawText, labelToNumeric, stopWords))
     val X_train = tfidfTransformer(training)
     X_train.foreach(vv => println(vv))

     model = NaiveBayes.train(X_train, lambda = 1.0)

     val lines=sc.wholeTextFiles("data/testing/*")
     val data = lines.map(line => {

         val test = createLabeledDocumentTest(line._2, labelToNumeric, stopWords)
         println(test.body)
         test


     })

          val X_test = tfidfTransformerTest(sc, data)

            val predictionAndLabel = model.predict(X_test)
            println("PREDICTION")
            predictionAndLabel.foreach(x => {
              labelToNumeric.foreach { y => if (y._2 == x) {
                println(y._1)
              }
              }
            })





   }


 }
