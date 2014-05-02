package ua.kpi.teacherjournal

import android.app.FragmentManager
import android.content.Context
import android.graphics.Color._
import android.os.Bundle
import android.view.{View, Gravity, ViewGroup, LayoutInflater}
import android.widget.AdapterView
import org.scaloid.common._
import scala.language.postfixOps
import ua.kpi.teacherjournal.Journal._

object TableFragment {
  val headerColor = rgb(0xe7, 0xe7, 0xe7)
  val cellColor = WHITE
  val bossColor = rgb(0x99, 0x32, 0x31)
  val bgColor = rgb(0xcc, 0xcc, 0xcc)
  val absentBackgroundColor = rgb(0xFF, 0xE5, 0xE6)

  def marginRight(implicit context: Context) = 1 dip
  def marginBottom(implicit context: Context) = 1 dip

  def update(fragmentManager: FragmentManager, selectedSheet: Sheet) =
    fragmentManager.beginTransaction()
      .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
      .replace(R.id.table_fragment, TableFragment(selectedSheet))
      .commit()

  def apply(sheet: Sheet) = {
    (new TableFragment).setArguments("sheet" -> sheet)
  }
}

class TableFragment extends RichFragment {
  import TableFragment._
  import RandData._

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    require(ctx != null)

    val sheet = arg[Sheet]("sheet")

    new SScrollView {
      backgroundColor = bgColor
      this += new SLinearLayout {
        style {
          case t: STextView => t.textColor(BLACK)
            .textSize(18 dip)
            .maxLines(1)
            .padding(20 dip, 10 dip, 20 dip, 10 dip)
        }

        this += new STableLayout {
          style {
            case t: TraitView[_] => t.backgroundColor(headerColor).<<
              .marginRight(marginRight)
              .marginBottom(marginBottom)
              .>>
          }

          SSpinner().adapter(SArrayAdapter(selectedCourse.sheets.map(_.name).toArray)
            .dropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            .style(_.textColor(BLACK)
              .textSize(18 dip)
              .maxLines(1)
              .padding(20 dip, 2 dip, 20 dip, 3 dip))
            ).selection(sheetId).onItemSelected((_: AdapterView[_], _: View, pos: Int, _: Long) => {
              if (sheetId != pos) {
                sheetId = pos
                update(getFragmentManager, selectedSheet)
              }
            })
          for ((student, studentIndex) <- sheet.students.zipWithIndex) {
            val tv = STextView(s"${studentIndex + 1}. ${student.name}")
            if (student.isBoss) tv.textColor = bossColor
          }
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
