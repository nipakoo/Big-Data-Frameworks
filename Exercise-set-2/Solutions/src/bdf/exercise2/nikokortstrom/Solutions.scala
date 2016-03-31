package bdf.exercise2.nikokortstrom

// Name: Niko Kortström
// Student number: 014154573

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.apache.spark.rdd.RDD

/* Definitions of all other objects, classes, global functions etc go here */

/* Finally we have the Solutions object */
object Solutions {
  
  case class Rating(user_id : String, location : String, age : Integer, rating : Integer)
  case class Book(isbn : String, title : String, author : String, year : Integer, publisher : String, ratings : Iterable[Rating])
  
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
  
  def exercise2(rdd : RDD[Book], start_year : Integer, end_year : Integer) : Long = {
    val correct_publications = rdd.filter(book => (book.year >= start_year && book.year <= end_year))
    val only_reviews = correct_publications.flatMap(book => book.ratings)
    
    return only_reviews.count
  }
  
  def exercise3(sc : SparkContext) {
    
  }
  
  def exercise4(sc : SparkContext) {
    
  }
  
  def exercise5(sc : SparkContext) {
    
  }
  
  def exercise6(sc : SparkContext) {
    
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
    exercise3(sc)
    /* Run exercise4 code */
    exercise4(sc)
    /* Run exercise5 code */
    exercise5(sc)
    /* Run exercise6 code */
    exercise6(sc)
  }
}