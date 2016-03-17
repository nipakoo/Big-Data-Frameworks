// Name: Niko Kortström
// Student number 014154573

import java.util.Random

object HelloWorld {
  def main(args: Array[String]): Unit = {
    // Print exercise 4 results
    print_sentence
    
    // Print exercise 5 results
    mean_and_std_dev
    
    // Print Exercise 6 results
    val student = Student("John Smith")
    Student.print_info(student)
  }
  
  // Exercise 4
  def print_sentence {
    var sentence : String = "Hello Scala!"
    println(sentence)
    
    var i = 0
    for (i <- 0 to 9) {
      println(sentence.replaceFirst(sentence.charAt(i).toString, "X"))
    }
  }
  
  // Exercise 5
  def mean_and_std_dev {
    val random : Random = new Random();
    var numbers : Array[Double] = new Array[Double](100)
    
    var i = 0
    for (i <- 0 to 99) {
      numbers(i) = random.nextGaussian()
    }
    
    val mean = numbers.foldLeft(0.0)((x, y) => x + y / numbers.length)
    println("Mean = " + mean)
    
    val std_dev = numbers.foldLeft(0.0)((x, y) => x + (y - mean) / numbers.length)
    println("Standard deviation = " + std_dev)
  }
}

// Exercise 6
class Student(val name: String,
              val student_id: Int,
              var address: String,
              val year_class: Int,
              var passed_courses: List[String],
              var course_grades: List[Int]) {
}

object Student {
  def apply(name: String) = {
    new Student(name, 11111, "Scala Avenue 8", 2012, List("Some course", "Another course"), List(4, 2))
  }
  def print_info(student: Student) {
    val grades_mean : Double = student.course_grades.sum / student.course_grades.length
    
    println("Name: " + student.name)
    println("Year class: " + student.year_class)
    println("Mean of grades: " + grades_mean)
    println("Passed courses: " + student.passed_courses.mkString(", "))
  }
}