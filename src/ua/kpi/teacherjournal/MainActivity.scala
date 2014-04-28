package ua.kpi.teacherjournal

import org.scaloid.common._
import android.graphics.Color
import android.widget.ArrayAdapter
import android.app.ActionBar
import android.app.ActionBar.OnNavigationListener

case class Student(name: String, marks: Map[String, String])
object StudentDAO {
  val students = for (i <- 1 to 50) yield Student(s"Student $i", Map("17.02" -> "3"))
  val dates = Array("17.02", "24.02", "1.03", "8.03", "15.03", "Lab 1", "Lab 2", "Lab 3", "Lab 4", "Lab 5", "Lab 6",
    "Lab 7", "Lab 8", "Lab 9", "Lab 10", "Lab 11")
  val groups = Array("IO-25", "IO-24")
  var group_id = 0
}

class MainActivity extends SActivity {

  private val headerColor = Color.rgb(0xe7, 0xe7, 0xe7)
  private val cellColor = Color.WHITE
  private val borderColor = Color.rgb(0xcc, 0xcc, 0xcc)

  onCreate {

    import StudentDAO._

    val actionBar = getActionBar()
    val adapter = new ArrayAdapter[String](this, android.R.layout.simple_spinner_dropdown_item, groups)
    val activity = this
    val mNavListener = new OnNavigationListener {
      def onNavigationItemSelected(position: Int, id: Long) = {
        if (group_id != position) {
          group_id = position
          activity.recreate()
        }
        true
      }
    }
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST)
    actionBar.setListNavigationCallbacks(adapter, mNavListener)
    actionBar.setDisplayShowTitleEnabled(false)
    actionBar.setSelectedNavigationItem(group_id)

    contentView = new SVerticalLayout {

      style {
        case t: STextView => t.textColor(Color.BLACK).maxLines(1).padding(2.dip)
      }

      this += new SScrollView {
        this += new SHorizontalScrollView {
          this += new STableLayout {
            // Header row
            this += new STableRow {
              def textStyle(view: STextView) = view.backgroundColor(headerColor).<<.marginRight(1).marginBottom(1).>>
              textStyle(STextView(groups(group_id)))
              for (date <- dates)
                textStyle(STextView(date))
            }
            // Student rows
            for ((student, studentIndex) <- students.zipWithIndex)
              this += new STableRow {
                STextView(s"${studentIndex + 1}. ${student.name}").backgroundColor(headerColor).<<.
                  marginRight(1).marginBottom(1).>>
                for (date <- dates)
                  STextView(student.marks.getOrElse(date, "")).backgroundColor(cellColor).<<.marginRight(1).
                    marginBottom(1).>>
              }
          }
        }
      }
    }.backgroundColor(borderColor)
  }
}
