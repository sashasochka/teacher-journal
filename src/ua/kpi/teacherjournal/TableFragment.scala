package ua.kpi.teacherjournal

import android.app.Fragment
import android.content.Context
import android.graphics.Color._
import android.os.Bundle
import android.view.{Gravity, ViewGroup, LayoutInflater}
import org.scaloid.common._
import scala.language.postfixOps
import ua.kpi.teacherjournal.Journal._

object TableFragment {
  val headerColor = rgb(0xe7, 0xe7, 0xe7)
  val cellColor = WHITE
  val backgroundColor = rgb(0xcc, 0xcc, 0xcc)
  val absentBackgroundColor = rgb(0xFF, 0xE5, 0xE6)

  def marginRight(implicit context: Context) = 1 dip
  def marginBottom(implicit context: Context) = 1 dip
}

class TableFragment(sheet: Sheet) extends Fragment {
  import TableFragment._

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    implicit val ctx = getActivity
    require(ctx != null)

    new SScrollView {
      this += new SLinearLayout {
        style {
          case t: STextView => t.textColor(BLACK)
            .textSize(18 dip)
            .maxLines(1)
            .padding(20 dip, 10 dip, 20 dip, 10 dip)
        }

        this += new STableLayout {
          style {
            case t: STextView => t.backgroundColor(headerColor).<<
              .marginRight(marginRight)
              .marginBottom(marginBottom)
              .>>
          }

          STextView(sheet.name).gravity(Gravity.CENTER_HORIZONTAL)
          for ((student, studentIndex) <- sheet.students.zipWithIndex)
            STextView(s"${studentIndex + 1}. ${student.name}")
        }

        this += new SHorizontalScrollView {
          this += new STableLayout {

            // Header row
            this += new STableRow {

              style {
                case t: STextView => t.gravity(Gravity.CENTER_HORIZONTAL)
                  .backgroundColor(headerColor).<<
                  .marginRight(marginRight).marginBottom(marginBottom)
                  .>>
              }
              for (column <- sheet.columns)
                STextView(column)
            }

            // Student rows
            for ((student, studentIndex) <- sheet.students.zipWithIndex)
              this += new STableRow {
                style {
                  case t: STextView => t.gravity(Gravity.CENTER_HORIZONTAL)
                    .backgroundColor(cellColor).<<
                    .marginRight(marginRight)
                    .marginBottom(marginBottom)
                    .>>
                }

                for (record <- student.records) {
                  record match {
                    case GradeRecord(grade) => STextView(grade.toString)
                    case AbsentRecord => STextView().backgroundColor(absentBackgroundColor)
                    case EmptyRecord => STextView()
                  }
                }
              }
          }
        }
      }
    }
  }
}
