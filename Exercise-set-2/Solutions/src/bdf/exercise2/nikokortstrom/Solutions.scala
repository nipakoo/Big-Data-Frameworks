package bdf.exercise2.nikokortstrom

// Name: Niko Kortström
// Student number: 014154573

import java.io._
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.apache.spark.rdd.RDD
import scala.math

/* Definitions of all other objects, classes, global functions etc go here */

/* Finally we have the Solutions object */
object Solutions {
  
  case class Rating(user_id : String, location : String, age : Int, rating : Int)
  case class Book(isbn : String, title : String, author : String, year : Int, publisher : String, ratings : Iterable[Rating])
  
  def remove_parantheses(value : String) : String = {
    return value.substring(1, value.length()-1)
  }
  
  // parse the values to correct types and remove the parantheses
  def parse_rating(rating : String, user : String) : Rating = {
    val user_id = remove_parantheses(rating.split(";")(0))
    val location = user.split("\"")(3)
    
    var age = -1
    if (user.split(";")(user.split(";").length-1) != "NULL") {
      age = remove_parantheses(user.split(";")(user.split(";").length-1)).toInt
    }
    val rating_number = remove_parantheses(rating.split(";")(2)).toInt
    
    return Rating(user_id, location, age, rating_number)
  }
  
  // parse the values to correct types and remove the parantheses
  def parse_book(book_info : String, ratings : Array[Iterable[Rating]]) : Book = {
    val isbn = remove_parantheses(book_info.split(";")(0))
    val title = book_info.split("\";\"")(1)
    val author = book_info.split("\";\"")(2)
    val year = book_info.split("\";\"")(3).toInt
    val publisher = book_info.split("\";\"")(4)
    
    var rating_iter = Iterable[Rating]()
    if (!ratings.isEmpty) {
      rating_iter = ratings(0)
    }
    
    return Book(isbn, title, author, year, publisher, rating_iter);
  }
  
  def exercise2(rdd : RDD[Book], start_year : Int, end_year : Int) : Long = {
    // get an rdd with only the publications in the correct publication window
    val correct_publications = rdd.filter(book => (book.year >= start_year && book.year <= end_year))
    // get an rdd with only the reviews of these books
    val only_reviews = correct_publications.flatMap(book => book.ratings)
    
    return only_reviews.count
  }
  
  def exercise3(rdd : RDD[Book]) : Array[String] = {
    // get an rdd with only author name as key and iterable of ages of reviewers as value
    val author_reviews = rdd.map(book => (book.author, book.ratings.map(rating => rating.age)))

    // filter out the reviews that don't contain age of the reviewer and filter out authors that have no ratings after this
    val no_ageless_reviews = author_reviews.map(author_review => (author_review._1, author_review._2.filter(age => age != -1))).filter(author_review => !author_review._2.isEmpty)
    
    // format the rdd to contain (author, average_reviewer_age) per element
    val average_ages = no_ageless_reviews.map(no_ageless_review => (no_ageless_review._1, no_ageless_review._2.reduce((age_1, age_2) => age_1 + age_2) / no_ageless_review._2.size))
    
    // return the 20 authors that have highest average reviewer age
    return average_ages.takeOrdered(100)(Ordering[Int].reverse.on(author_rating => author_rating._2)).map(author_rating => author_rating._1)
  }
  
  def calculate_mean(rdd : RDD[Double]) : Double = {
    // sum all values and then divide by their count
    return rdd.reduce((value_1, value_2) => value_1 + value_2) / rdd.count()
  }
  
  def calculate_covariance(x_rdd : RDD[Double], y_rdd : RDD[Double]) : Double = {
    val x_mean = calculate_mean(x_rdd)
    val y_mean = calculate_mean(y_rdd)
    // combine the rdds pair wise in a single rdd
    val x_y_rdd = x_rdd.zip(y_rdd)
    // calculate the covariance of x and y
    // substract mean of each list from its current value and then multiply it with the maching value of other list
    // finally calculate the mean of this list to get covariance
    return calculate_mean(x_y_rdd.map(x_y => (x_y._1-x_mean)*(x_y._2-y_mean)))
  }
  
  def calculate_std_dev(rdd : RDD[Double]) : Double = {
    val rdd_mean = calculate_mean(rdd)
    // map (value-mean)^2 to each index, then calculate mean of this list and finally take a square root of it to get standard deviation
    val deviations = rdd.map(value => math.pow((value - rdd_mean), 2))
    return math.sqrt(calculate_mean(deviations))
  }
  
  def exercise4(x_rdd : RDD[Double], y_rdd : RDD[Double]) : Double = {
    // calculate the pearson product by dividing the covariance of x and y lists with the multiplication of std dev of x and y
    return calculate_covariance(x_rdd, y_rdd) / (calculate_std_dev(x_rdd) * calculate_std_dev(y_rdd))
  }
  
  def write_doubles_to_file(filename : String, data : Array[Double]) {
    val pw = new PrintWriter(new File(filename))
    
    var i = 0
    for (i <- 0 to data.length-1) {
      val value = data(i)
      if (i < data.length-1) {
        pw.write(i + ", " + value.doubleValue() + ", ")
      } else {
        pw.write(i + ", "+ value.doubleValue())
      }
    }
    
    pw.close
  }
  
  def exercise5(sc : SparkContext) {
    val data = sc.textFile("carat-context-factors-percom.csv")
    val energy_rate = data.map(line => line.split(";")(0).toDouble)
    val cpu_load = data.map(line => line.split(";")(4).toDouble)
    val screen_brightness = data.map(line => line.split(";")(11).toDouble)
    val wifi_link_speed = data.map(line => line.split(";")(12).toDouble)
    val wifi_signal_strength = data.map(line => line.split(";")(13).toDouble)
    
    val energy_cpu_correlation = exercise4(energy_rate, cpu_load)
    val energy_screen_correlation = exercise4(energy_rate, screen_brightness)
    val energy_wifi_speed_correlation = exercise4(energy_rate, wifi_link_speed)
    val energy_wifi_strength_correlation = exercise4(energy_rate, wifi_signal_strength)
    
    write_doubles_to_file("energy.txt", energy_rate.take(100))
    write_doubles_to_file("cpu.txt", cpu_load.take(100))
    write_doubles_to_file("screen.txt", screen_brightness.take(100))
    write_doubles_to_file("speed.txt", wifi_link_speed.take(100))
    write_doubles_to_file("strength.txt", wifi_signal_strength.take(100))

    val pw = new PrintWriter(new File("ex5_correlations.txt"))
    pw.write("Correlation between energy rate and cpu load: " + energy_cpu_correlation.doubleValue() + "\n")
    pw.write("Correlation between energy rate and screen brightness: " + energy_screen_correlation.doubleValue() + "\n")
    pw.write("Correlation between energy rate and wifi link speed: " + energy_wifi_speed_correlation.doubleValue() + "\n")
    pw.write("Correlation between energy rate and wifi signal strength: " + energy_wifi_strength_correlation.doubleValue() + "\n")
    pw.close
  }
  
  def multiple_correlation(z_rdd : RDD[Double], x_rdd : RDD[Double], y_rdd : RDD[Double]) : Double = {
    val x_z_correlation = exercise4(x_rdd, z_rdd)
    val y_z_correlation = exercise4(y_rdd, z_rdd)
    val x_y_correlation = exercise4(x_rdd, y_rdd)
    
    // calculate numerator and denominator used in the calculation of multiple correlation coefficient
    val numerator = math.pow(x_z_correlation, 2) + math.pow(y_z_correlation, 2) - (2*x_z_correlation*y_z_correlation*x_y_correlation) 
    val denominator = 1 - x_y_correlation
    
    // calculate the square root of this division to get the final result
    return math.sqrt(numerator / denominator)
  }
  
  def partial_correlation(z_rdd : RDD[Double], x_rdd : RDD[Double], y_rdd : RDD[Double]) : Double = {
    val z_x_correlation = exercise4(z_rdd, x_rdd)
    val z_y_correlation = exercise4(z_rdd, x_rdd)
    val x_y_correlation = exercise4(x_rdd, y_rdd)
    
    // calculate numerator and denominator used in the calculation of partial correlation
    val numerator = z_x_correlation - (z_y_correlation*x_y_correlation)
    val denominator = math.sqrt(1 - math.pow(z_y_correlation, 2)) * math.sqrt(1 - math.pow(x_y_correlation, 2))
    
    // calculate the division to get the final result
    return numerator / denominator
  }
  
  def semi_partial_correlation(z_rdd : RDD[Double], x_rdd : RDD[Double], y_rdd : RDD[Double]) : Double = {
    val z_x_correlation = exercise4(z_rdd, x_rdd)
    val z_y_correlation = exercise4(z_rdd, x_rdd)
    val x_y_correlation = exercise4(x_rdd, y_rdd)
    
    // calculate numerator and denominator used in the calculation of semi-partial correlation
    val numerator = z_x_correlation - (z_y_correlation * x_y_correlation)
    val denominator = math.sqrt(1 - math.pow(x_y_correlation, 2))
    
    // calculate the division to get the final result
    return numerator / denominator
  }
  
  def exercise6(sc : SparkContext) {
    val data = sc.textFile("carat-context-factors-percom.csv")
    val energy_rate = data.map(line => line.split(";")(0).toDouble)
    val wifi_link_speed = data.map(line => line.split(";")(12).toDouble)
    val wifi_signal_strength = data.map(line => line.split(";")(13).toDouble)
    
    val multiple_corr = multiple_correlation(energy_rate, wifi_link_speed, wifi_signal_strength)
    val partial_corr = partial_correlation(energy_rate, wifi_link_speed, wifi_signal_strength)
    val semi_partial_corr = semi_partial_correlation(energy_rate, wifi_link_speed, wifi_signal_strength)
    
    val pw = new PrintWriter(new File("ex6_correlations.txt"))
    pw.write("Multiple correlation between energy rate, wifi link speed and wifi signal strength: " + multiple_corr.doubleValue() + "\n")
    pw.write("Partial correlation between energy rate, wifi link speed and wifi signal strength: " + partial_corr.doubleValue() + "\n")
    pw.write("Semi partial correlation between energy rate, wifi link speed and wifi signal strength: " + semi_partial_corr.doubleValue() + "\n")
    pw.close
  }
  
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName(getClass.getName).setMaster("local[2]")
    val sc = new SparkContext(conf)
    
    val books = sc.textFile("BX-Books.csv")
    val users = sc.textFile("BX-Users.csv")
    val ratings = sc.textFile("BX-Book-Ratings.csv")
    
    // prepare user rdd for joining
    val user_info = users.keyBy(user => user.split(";")(0))
    // drop the header
    val user_header = user_info.first
    val user_info_without_header = user_info.filter(line => line != user_header)
    
    // prepare rating rdd for joining
    val rating_info = ratings.keyBy(rating => rating.split(";")(0))
    // drop the header
    val rating_header = rating_info.first
    val rating_info_without_header = rating_info.filter(line => line != rating_header)
    
    val user_ratings = rating_info_without_header.join(user_info_without_header)
    // format the tuples to place values in correct places (ISBN, [userID, location, age, rating])
    val formatted_ratings = user_ratings.map(user_rating => (user_rating._2._1.split(";")(1), parse_rating(user_rating._2._1, user_rating._2._2)))
    
    // place the ratings of same book into a list that is in a tuple with isbn as the key
    val book_ratings = formatted_ratings.groupByKey()
    
    // prepare book rdd for joining
    val book_info = books.keyBy(book => book.split(";")(0))
    // drop the header
    val book_header = book_info.first
    val book_info_without_header = book_info.filter(line => line != book_header)
    
    val book_rating_data = book_info_without_header.leftOuterJoin(book_ratings)
    // format the tuples to place values in correct places (isbn, title, author, year, publisher, [Rating])
    val formatted_books = book_rating_data.map(book_rating_info => parse_book(book_rating_info._2._1, book_rating_info._2._2.toArray))
    
    /* Run exercise2 code */
    val anwer2 = exercise2(formatted_books, 1992, 1998)
    println("Number of reviews for books published between 1992 and 1998: " + anwer2);
    
    /* Run exercise3 code */
    val answer_3 = exercise3(formatted_books)
    // why on earth is there reviewers in the data with 239 and 228 as age??
    println("Top 20 authors with highest average reviewer age: " + answer_3.mkString(", "))
    
    /* Run exercise4 code */
    val x = sc.parallelize(Array[Double](56,56,65,65,50,25,87,44,35)).map(value => value.toDouble)
    val y = sc.parallelize(Array[Double](87,91,85,91,75,28,122,66,58)).map(value => value.toDouble)
    val answer_4 = exercise4(x, y)
    // example data set taken from http://learntech.uwe.ac.uk/da/Default.aspx?pageid=1442
    println("Pearson correlation of test data sets x and y: " + answer_4)
    
    /* Run exercise5 code */
    exercise5(sc)
    /* Run exercise6 code */
    exercise6(sc)
  }
}